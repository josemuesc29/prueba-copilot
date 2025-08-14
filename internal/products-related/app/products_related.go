package app

import (
	"encoding/json"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model/mappers"
	inPorts "ftd-td-catalog-item-read-services/internal/products-related/domain/ports/in"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedOutPorts "ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"sort"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
)

const (
	GetRelatedItemsLog                    = "ProductsRelatedService.GetRelatedItems"
	getItemProductRelatedLog              = "ProductsRelatedService.GetItemProductRelated"
	getProductsRelatedItemsFromAlgoliaLog = "ProductsRelatedService.GetProductsRelatedItemsFromAlgolia"
	findRelatedInCacheLog                 = "ProductsRelatedService.findRelatedInCache"
	repositoryProxyCatalogProduct         = "repository proxy catalog product"
	indexCatalogProducts                  = "index catalog products"
	keyRelatedProductsCache               = "related_%s_%s" // countryID, itemID
	configProductsRelatedKey              = "PRODUCTS-RELATED.CONFIG"
)

type productsRelated struct {
	outPortCatalogProduct sharedOutPorts.CatalogProduct
	outPortCache          sharedOutPorts.Cache
	outPortConfig         sharedOutPorts.ConfigOutPort
}

func NewProductsRelated(
	outPortCatalogProduct sharedOutPorts.CatalogProduct,
	outPortCache sharedOutPorts.Cache,
	outPortConfig sharedOutPorts.ConfigOutPort,
) inPorts.ProductsRelated {
	return &productsRelated{
		outPortCatalogProduct: outPortCatalogProduct,
		outPortCache:          outPortCache,
		outPortConfig:         outPortConfig,
	}
}

func (p *productsRelated) GetRelatedItems(
	ctx *gin.Context,
	countryID, itemID string,
) ([]model.ProductsRelatedItem, error) {
	correlationID := getCorrelationIDFromContext(ctx)

	// 1. Intentar desde caché
	if rs, ok := p.getFromCache(ctx, countryID, itemID, correlationID); ok {
		return rs, nil
	}

	// 2. Configuración
	configProductsRelated, err := p.outPortConfig.GetConfigBestSeller(ctx, countryID, configProductsRelatedKey)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog,
			fmt.Sprintf(enums.GetData, "error", "repositoryConfig"))
		return nil, err
	}

	// 3. Item original
	originalItem, err := p.getProductsRelated(ctx, countryID, itemID)
	if err != nil {
		log.Warnf(enums.LogFormat, correlationID, GetRelatedItemsLog,
			fmt.Sprintf("Error getting original item's brand: %v", err))
		return nil, nil
	}

	// 4. Obtener productos relacionados desde Algolia
	productsFromAlgolia := p.fetchFromAlgolia(ctx, countryID, itemID, originalItem, configProductsRelated, correlationID)

	// 5. Procesar y mapear resultados
	rs := p.processProducts(productsFromAlgolia)

	// 6. Guardar en caché si hay resultados
	p.saveInCacheIfNotEmpty(ctx, countryID, itemID, rs, correlationID)

	return rs, nil
}

func getCorrelationIDFromContext(ctx *gin.Context) string {
	if id, ok := ctx.Get(enums.HeaderCorrelationID); ok {
		if idStr, typeOk := id.(string); typeOk {
			return idStr
		}
	}
	return utils.GetCorrelationID(ctx.GetHeader(enums.HeaderCorrelationID))
}

func (p *productsRelated) getFromCache(ctx *gin.Context, countryID, itemID, correlationID string) ([]model.ProductsRelatedItem, bool) {
	var rs []model.ProductsRelatedItem
	err := findProductsRelatedInCache(ctx, p.outPortCache, countryID, itemID, &rs)
	if err == nil && len(rs) > 0 {
		log.Infof(enums.LogFormat, correlationID, GetRelatedItemsLog, "Successfully retrieved from cache")
		return rs, true
	}
	if err != nil {
		log.Warnf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Cache find error: %v", err))
	}
	return nil, false
}

func (p *productsRelated) fetchFromAlgolia(
	ctx *gin.Context, countryID, itemID string,
	originalItem sharedModel.ProductInformation,
	configProductsRelated sharedModel.ConfigBestSeller,
	correlationID string,
) []sharedModel.ProductInformation {
	mediaDescription := originalItem.MediaDescription
	words := utils.SplitWords(mediaDescription)
	seen := make(map[string]struct{})
	var productsFromAlgolia []sharedModel.ProductInformation

	for len(productsFromAlgolia) <= config.Enviroments.MaxItemsSubstitute && len(words) > 1 {
		words = words[:len(words)-1]
		queryDescription := utils.JoinWords(words)

		items, err := p.getProductsRelatedItemsFromAlgolia(ctx, countryID, queryDescription, itemID, configProductsRelated)
		if err != nil {
			log.Warnf(enums.LogFormat, correlationID, getProductsRelatedItemsFromAlgoliaLog,
				fmt.Sprintf("Failed for query='%s': %v", queryDescription, err))
			continue
		}

		for _, item := range items {
			if _, exists := seen[item.ObjectID]; !exists {
				productsFromAlgolia = append(productsFromAlgolia, item)
				seen[item.ObjectID] = struct{}{}
			}
		}
	}
	return productsFromAlgolia
}

func (p *productsRelated) processProducts(products []sharedModel.ProductInformation) []model.ProductsRelatedItem {
	sort.SliceStable(products, func(i, j int) bool {
		return len(products[i].StoresWithStock) > len(products[j].StoresWithStock)
	})

	var rs []model.ProductsRelatedItem
	var productsRelatedItem model.ProductsRelatedItem
	for _, productInfo := range products {
		rs = append(rs, mappers.MapProductInformationToProductsRelatedItem(&productsRelatedItem, &productInfo))
	}
	return rs
}

func (p *productsRelated) saveInCacheIfNotEmpty(ctx *gin.Context, countryID, itemID string, rs []model.ProductsRelatedItem, correlationID string) {
	if len(rs) > 0 {
		if err := saveProductsRelatedInCache(ctx, p.outPortCache, countryID, itemID, rs); err != nil {
			log.Warnf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Cache save error: %v", err))
		}
	}
}

func findProductsRelatedInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache,
	countryID, itemID string, response *[]model.ProductsRelatedItem) error {
	correlationID := getCorrelationIDFromContext(ctx)
	cacheKey := fmt.Sprintf(keyRelatedProductsCache, countryID, itemID)
	cachedData, err := outPortCache.Get(ctx, cacheKey)

	if err != nil {
		log.Warnf(enums.LogFormat, correlationID, findRelatedInCacheLog,
			fmt.Sprintf("Error getting from cache. Key: %s, Error: %v", cacheKey, err))
		return err
	}

	if cachedData == "" {
		log.Debugf(enums.LogFormat, correlationID, findRelatedInCacheLog,
			fmt.Sprintf("Cache miss. Key: %s", cacheKey))
		return nil
	}

	err = json.Unmarshal([]byte(cachedData), response)
	if err != nil {
		log.Warnf(enums.LogFormat, correlationID, findRelatedInCacheLog,
			fmt.Sprintf("Error unmarshalling cached data. Key: %s, Error: %v", cacheKey, err))
		return err
	}

	log.Debugf(enums.LogFormat, correlationID, findRelatedInCacheLog,
		fmt.Sprintf("Successfully retrieved from cache. Key: %s", cacheKey))
	return nil
}

func (p *productsRelated) getProductsRelated(ctx *gin.Context, countryID, itemID string) (sharedModel.ProductInformation, error) {
	correlationID := getCorrelationIDFromContext(ctx)
	utils.PropagateHeader(ctx, enums.HeaderXCustomCity)

	items, err := p.outPortCatalogProduct.GetProductsInformationByObjectID(ctx, []string{itemID}, countryID)
	if err != nil {
		log.Errorf(enums.LogFormat, correlationID, getItemProductRelatedLog,
			fmt.Sprintf("Error from CatalogProduct port: %v. Repo: %s", err, repositoryProxyCatalogProduct))
		return sharedModel.ProductInformation{}, err
	}

	if len(items) == 0 {
		log.Warnf(enums.LogFormat, correlationID, getItemProductRelatedLog, fmt.Sprintf("Item not found: %s", itemID))
		return sharedModel.ProductInformation{}, fmt.Errorf("item not found: %s", itemID)
	}

	return items[0], nil
}

func (p *productsRelated) getProductsRelatedItemsFromAlgolia(ctx *gin.Context, countryID string, queryDescription string, excludeItemID string, configProductsRelated sharedModel.ConfigBestSeller) ([]sharedModel.ProductInformation, error) {
	correlationID := getCorrelationIDFromContext(ctx)

	query := fmt.Sprintf(configProductsRelated.QueryProducts, strconv.Itoa(configProductsRelated.CountItems))
	utils.PropagateHeader(ctx, enums.HeaderXCustomCity)

	products, err := p.outPortCatalogProduct.GetProductsInformationByQueryRelated(ctx, query, countryID, queryDescription)
	if err != nil {
		log.Errorf(enums.LogFormat, correlationID, getProductsRelatedItemsFromAlgoliaLog,
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

func saveProductsRelatedInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache,
	countryID, itemID string, rs []model.ProductsRelatedItem) error {
	correlationID := getCorrelationIDFromContext(ctx)
	if len(rs) == 0 {
		return nil
	}

	cacheKey := fmt.Sprintf(keyRelatedProductsCache, countryID, itemID)
	dataToCache, errMarshal := json.Marshal(rs)
	if errMarshal != nil {
		log.Warnf(enums.LogFormat, correlationID, GetRelatedItemsLog,
			fmt.Sprintf("Error marshalling data for cache. Key: %s, Error: %v", cacheKey, errMarshal))
		return errMarshal
	}

	ttl := time.Duration(config.Enviroments.RedisProductsRelatedDepartmentTTL) * time.Minute
	if ttl <= 0 {
		ttl = 60 * time.Minute
		log.Warnf(enums.LogFormat, correlationID, GetRelatedItemsLog,
			fmt.Sprintf("Invalid or missing RedisSameBrandTTL, using default: %v. Key: %s", ttl, cacheKey))
	}

	err := outPortCache.Set(ctx, cacheKey, string(dataToCache), ttl)
	if err != nil {
		log.Warnf(enums.LogFormat, correlationID, GetRelatedItemsLog,
			fmt.Sprintf("Error saving to cache. Key: %s, Error: %v", cacheKey, err))
		return err
	}

	log.Debugf(enums.LogFormat, correlationID, GetRelatedItemsLog,
		fmt.Sprintf("Successfully saved to cache. Key: %s, TTL: %v", cacheKey, ttl))
	return nil
}
