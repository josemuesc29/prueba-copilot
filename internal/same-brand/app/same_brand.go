package app

import (
	"encoding/json"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/mappers"
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	inPorts "ftd-td-catalog-item-read-services/internal/same-brand/domain/ports/in"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedOutPorts "ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"sort"
	"strconv"
	"time"
)

const (
	GetItemsBySameBrandLog        = "SameBrandService.GetItemsBySameBrand"
	getItemBrandLog               = "SameBrandService.GetItemBrand"
	getBrandItemsFromAlgoliaLog   = "SameBrandService.GetBrandItemsFromAlgolia"
	findSameBrandInCacheLog       = "SameBrandService.findSameBrandInCache"
	repositoryProxyCatalogProduct = "repository proxy catalog product"
	indexCatalogProducts          = "index catalog products"
	keySameBrandCache             = "same_brands_%s_%s" // countryID, itemID
	configSameBrandKey            = "SAME-BRAND.CONFIG"
)

type sameBrand struct {
	outPortCatalogProduct sharedOutPorts.CatalogProduct
	outPortCache          sharedOutPorts.Cache
	outPortConfig         sharedOutPorts.ConfigOutPort
}

func NewSameBrand(
	outPortCatalogProduct sharedOutPorts.CatalogProduct,
	outPortCache sharedOutPorts.Cache,
	outPortConfig sharedOutPorts.ConfigOutPort,
) inPorts.SameBrand {
	return &sameBrand{
		outPortCatalogProduct: outPortCatalogProduct,
		outPortCache:          outPortCache,
		outPortConfig:         outPortConfig,
	}
}

func (s *sameBrand) GetItemsBySameBrand(ctx *gin.Context, countryID, itemID string) ([]model.SameBrandItem, error) {
	correlationID := getCorrelationIDFromContext(ctx)
	var rs []model.SameBrandItem

	// 1. Intentar obtener desde cache
	if s.tryGetFromCache(ctx, countryID, itemID, &rs, correlationID) {
		return rs, nil
	}

	// 2. Obtener configuración Same Brand
	configSameBrand, err := s.getSameBrandConfig(ctx, countryID, correlationID)
	if err != nil {
		return nil, err
	}

	// 3. Obtener el item original para identificar la marca
	originalItem, err := s.getItemBrand(ctx, countryID, itemID)
	if err != nil {
		return nil, err
	}
	if originalItem.Marca == "" {
		log.Warnf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Brand not found for item %s", itemID))
		return []model.SameBrandItem{}, nil
	}

	// 4. Consultar productos de la misma marca
	productsFromAlgolia, err := s.getBrandItemsFromAlgolia(ctx, countryID, originalItem.Marca, itemID, configSameBrand)
	if err != nil {
		return nil, err
	}

	// 5. Filtrar, ordenar y mapear
	rs = s.processSameBrandItems(productsFromAlgolia)

	// 6. Guardar en cache
	s.saveToCache(ctx, countryID, itemID, rs, correlationID)

	return rs, nil
}

func getCorrelationIDFromContext(ctx *gin.Context) string {
	if id, ok := ctx.Get(enums.HeaderCorrelationID); ok {
		if idStr, typeOk := id.(string); typeOk && idStr != "" {
			return idStr
		}
	}
	return utils.GetCorrelationID(ctx.GetHeader(enums.HeaderCorrelationID))
}

func (s *sameBrand) tryGetFromCache(ctx *gin.Context, countryID, itemID string, rs *[]model.SameBrandItem, correlationID string) bool {
	err := findSameBrandInCache(ctx, s.outPortCache, countryID, itemID, rs)
	if err == nil && len(*rs) > 0 {
		log.Infof(enums.LogFormat, correlationID, GetItemsBySameBrandLog, "Successfully retrieved from cache")
		return true
	}
	if err != nil {
		log.Warnf(enums.LogFormat, correlationID, GetItemsBySameBrandLog, fmt.Sprintf("Cache find error: %v", err))
	}
	return false
}

func (s *sameBrand) getSameBrandConfig(ctx *gin.Context, countryID, correlationID string) (sharedModel.ConfigBestSeller, error) {
	configSameBrand, err := s.outPortConfig.GetConfigBestSeller(ctx, countryID, configSameBrandKey)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf(enums.GetData, "error", "repositoryConfig"))
	}
	log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
		fmt.Sprintf(enums.GetData, "Success", fmt.Sprintf("%+v", configSameBrand)))
	return configSameBrand, err
}

func (s *sameBrand) processSameBrandItems(products []sharedModel.ProductInformation) []model.SameBrandItem {
	var rs []model.SameBrandItem
	var sameBrandItem model.SameBrandItem

	// Filtrar
	var processed []sharedModel.ProductInformation
	for _, product := range products {
		if s.shouldIncludeProduct(product) {
			processed = append(processed, product)
		}
	}

	// Ordenar
	sort.SliceStable(processed, func(i, j int) bool {
		return len(processed[i].StoresWithStock) > len(processed[j].StoresWithStock)
	})

	// Mapear
	for _, productInfo := range processed {
		mapped := mappers.MapProductInformationToSameBrandItem(&sameBrandItem, &productInfo)
		rs = append(rs, mapped)
	}
	return rs
}

func (s *sameBrand) saveToCache(ctx *gin.Context, countryID, itemID string, rs []model.SameBrandItem, correlationID string) {
	if len(rs) > 0 {
		if err := saveSameBrandInCache(ctx, s.outPortCache, countryID, itemID, rs); err != nil {
			log.Warnf(enums.LogFormat, correlationID, GetItemsBySameBrandLog, fmt.Sprintf("Cache save error: %v", err))
		}
	}
}

func (s *sameBrand) getItemBrand(ctx *gin.Context, countryID, itemID string) (sharedModel.ProductInformation, error) {
	correlationID := getCorrelationIDFromContext(ctx)
	utils.PropagateHeader(ctx, enums.HeaderXCustomCity)

	items, err := s.outPortCatalogProduct.GetProductsInformationByObjectID(ctx, []string{itemID}, countryID)
	if err != nil {
		log.Errorf(enums.LogFormat, correlationID, getItemBrandLog,
			fmt.Sprintf("Error from CatalogProduct port: %v. Repo: %s", err, repositoryProxyCatalogProduct))
		return sharedModel.ProductInformation{}, err
	}

	if len(items) == 0 {
		log.Warnf(enums.LogFormat, correlationID, getItemBrandLog, fmt.Sprintf("Item not found: %s", itemID))
		return sharedModel.ProductInformation{}, fmt.Errorf("item not found: %s", itemID)
	}

	return items[0], nil
}

func (s *sameBrand) getBrandItemsFromAlgolia(ctx *gin.Context, countryID, brand, excludeItemID string, configSameBrand sharedModel.ConfigBestSeller) ([]sharedModel.ProductInformation, error) {
	correlationID := getCorrelationIDFromContext(ctx)
	query := fmt.Sprintf(configSameBrand.QueryProducts, strconv.Itoa(configSameBrand.CountItems), brand)
	utils.PropagateHeader(ctx, enums.HeaderXCustomCity)

	products, err := s.outPortCatalogProduct.GetProductsInformationByQuery(ctx, query, countryID)
	if err != nil {
		log.Errorf(enums.LogFormat, correlationID, getBrandItemsFromAlgoliaLog,
			fmt.Sprintf("Error from CatalogProduct port: %v. Query: '%s', Repo: %s", err, query, indexCatalogProducts))
		return nil, err
	}

	var filteredProducts []sharedModel.ProductInformation
	for _, p := range products {
		if p.ObjectID != excludeItemID {
			filteredProducts = append(filteredProducts, p)
		}
	}

	return filteredProducts, nil
}

func (s *sameBrand) shouldIncludeProduct(product sharedModel.ProductInformation) bool {
	// Aquí podrías aplicar filtros adicionales si es necesario
	return true
}

func findSameBrandInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache,
	countryID, itemID string, response *[]model.SameBrandItem) error {
	correlationID := getCorrelationIDFromContext(ctx)
	cacheKey := fmt.Sprintf(keySameBrandCache, countryID, itemID)
	cachedData, err := outPortCache.Get(ctx, cacheKey)

	if err != nil {
		log.Warnf(enums.LogFormat, correlationID, findSameBrandInCacheLog,
			fmt.Sprintf("Error getting from cache. Key: %s, Error: %v", cacheKey, err))
		return err
	}

	if cachedData == "" {
		log.Debugf(enums.LogFormat, correlationID, findSameBrandInCacheLog,
			fmt.Sprintf("Cache miss. Key: %s", cacheKey))
		return nil
	}

	err = json.Unmarshal([]byte(cachedData), response)
	if err != nil {
		log.Warnf(enums.LogFormat, correlationID, findSameBrandInCacheLog,
			fmt.Sprintf("Error unmarshalling cached data. Key: %s, Error: %v", cacheKey, err))
		return err
	}

	log.Debugf(enums.LogFormat, correlationID, findSameBrandInCacheLog,
		fmt.Sprintf("Successfully retrieved from cache. Key: %s", cacheKey))
	return nil
}

func saveSameBrandInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache,
	countryID, itemID string, rs []model.SameBrandItem) error {
	correlationID := getCorrelationIDFromContext(ctx)
	if len(rs) == 0 { // No guardar si está vacío
		return nil
	}

	cacheKey := fmt.Sprintf(keySameBrandCache, countryID, itemID)
	dataToCache, errMarshal := json.Marshal(rs)
	if errMarshal != nil {
		log.Warnf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Error marshalling data for cache. Key: %s, Error: %v", cacheKey, errMarshal))
		return errMarshal
	}

	ttl := time.Duration(config.Enviroments.RedisSameBrandDepartmentTTL) * time.Minute
	if ttl <= 0 {
		ttl = 60 * time.Minute
		log.Warnf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Invalid or missing RedisSameBrandTTL, using default: %v. Key: %s", ttl, cacheKey))
	}

	err := outPortCache.Set(ctx, cacheKey, string(dataToCache), ttl)
	if err != nil {
		log.Warnf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Error saving to cache. Key: %s, Error: %v", cacheKey, err))
		return err
	}

	log.Debugf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
		fmt.Sprintf("Successfully saved to cache. Key: %s, TTL: %v", cacheKey, ttl))
	return nil
}
