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

func (s *sameBrand) GetItemsBySameBrand(ctx *gin.Context, countryID, itemID, source, nearbyStores, storeId, city string) ([]model.SameBrandItem, error) {
	var sameBrandItem model.SameBrandItem
	var correlationID string
	if id, ok := ctx.Get(enums.HeaderCorrelationID); ok {
		if idStr, typeOk := id.(string); typeOk {
			correlationID = idStr
		}
	}
	if correlationID == "" {
		correlationID = utils.GetCorrelationID(ctx.GetHeader(enums.HeaderCorrelationID))
	}
	var rs []model.SameBrandItem

	// 1. Intenta obtener de la caché
	err := findSameBrandInCache(ctx, s.outPortCache, countryID, itemID, &rs)
	if err == nil && len(rs) > 0 {
		log.Infof(enums.LogFormat, correlationID, GetItemsBySameBrandLog, "Successfully retrieved from cache")
		return rs, nil
	}
	if err != nil {
		log.Warnf(enums.LogFormat, correlationID, GetItemsBySameBrandLog, fmt.Sprintf("Cache find error: %v", err))
	}

	configSameBrand, err := s.outPortConfig.GetConfigBestSeller(ctx, countryID, configSameBrandKey)
	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetItemsBySameBrandLog,
			fmt.Sprintf(enums.GetData, "error", "repositoryConfig"))
		return nil, err
	}

	log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetItemsBySameBrandLog,
		fmt.Sprintf(enums.GetData, "Success", fmt.Sprintf("%+v", configSameBrand)))
	originalItem, err := s.getItemBrand(ctx, countryID, itemID)
	if err != nil {
		log.Errorf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Error getting original item's brand: %v", err))
		return nil, err
	}

	if originalItem.Marca == "" {
		log.Warnf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Brand not found for item %s", itemID))

		return []model.SameBrandItem{}, nil
	}

	productsFromAlgolia, err := s.getBrandItemsFromAlgolia(ctx, countryID, originalItem.Marca, itemID, source, nearbyStores, storeId, city, configSameBrand)
	if err != nil {
		log.Errorf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Error getting brand items from Algolia: %v", err))
		return nil, err
	}

	var processedItems []sharedModel.ProductInformation
	for _, product := range productsFromAlgolia {
		if s.shouldIncludeProduct(product) {
			processedItems = append(processedItems, product)
		}
	}

	sort.SliceStable(processedItems, func(i, j int) bool {
		return len(processedItems[i].StoresWithStock) > len(processedItems[j].StoresWithStock)
	})

	for _, productInfo := range processedItems {
		mappedItem := mappers.MapProductInformationToSameBrandItem(&sameBrandItem, &productInfo)
		rs = append(rs, mappedItem)
	}

	if len(rs) > 0 {
		err = saveSameBrandInCache(ctx, s.outPortCache, countryID, itemID, rs)
		if err != nil {
			log.Warnf(enums.LogFormat, correlationID, GetItemsBySameBrandLog, fmt.Sprintf("Cache save error: %v", err))
		}
	}

	log.Infof(enums.LogFormat, correlationID, GetItemsBySameBrandLog, "Successfully retrieved and processed same brand items")
	return rs, nil
}

func (s *sameBrand) getItemBrand(ctx *gin.Context, countryID, itemID string) (sharedModel.ProductInformation, error) {
	var correlationID string
	if id, ok := ctx.Get(enums.HeaderCorrelationID); ok {
		if idStr, typeOk := id.(string); typeOk {
			correlationID = idStr
		}
	}
	if correlationID == "" {
		correlationID = utils.GetCorrelationID(ctx.GetHeader(enums.HeaderCorrelationID))
	}
	// Asegurarse de que el header X-Custom-City se propaga si está presente
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

func (s *sameBrand) getBrandItemsFromAlgolia(ctx *gin.Context, countryID, brand, excludeItemID, source, nearbyStores, storeId, city string, configSameBrand sharedModel.ConfigBestSeller) ([]sharedModel.ProductInformation, error) {
	var correlationID string
	if id, ok := ctx.Get(enums.HeaderCorrelationID); ok {
		if idStr, typeOk := id.(string); typeOk {
			correlationID = idStr
		}
	}
	if correlationID == "" {
		correlationID = utils.GetCorrelationID(ctx.GetHeader(enums.HeaderCorrelationID))
	}

	query := fmt.Sprintf(configSameBrand.QueryProducts, strconv.Itoa(configSameBrand.CountItems), brand)
	// Asegurarse de que el header X-Custom-City se propaga
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
	if len(product.StoresWithStock) == 0 || product.Status != "A" {
		return false
	}

	return true
}

func findSameBrandInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache,
	countryID, itemID string, response *[]model.SameBrandItem) error {
	var correlationID string
	if id, ok := ctx.Get(enums.HeaderCorrelationID); ok {
		if idStr, typeOk := id.(string); typeOk {
			correlationID = idStr
		}
	}
	if correlationID == "" {
		correlationID = utils.GetCorrelationID(ctx.GetHeader(enums.HeaderCorrelationID))
	}
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
	var correlationID string
	if id, ok := ctx.Get(enums.HeaderCorrelationID); ok {
		if idStr, typeOk := id.(string); typeOk {
			correlationID = idStr
		}
	}
	if correlationID == "" {
		correlationID = utils.GetCorrelationID(ctx.GetHeader(enums.HeaderCorrelationID))
	}
	if len(rs) == 0 { // No guardar en caché si no hay resultados
		return nil
	}

	cacheKey := fmt.Sprintf(keySameBrandCache, countryID, itemID)
	dataToCache, errMarshal := json.Marshal(rs)
	if errMarshal != nil {
		log.Warnf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Error marshalling data for cache. Key: %s, Error: %v", cacheKey, errMarshal))
		return errMarshal
	}

	// Usar el TTL correcto de las variables de entorno
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
