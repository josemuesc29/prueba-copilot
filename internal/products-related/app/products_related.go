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
	"strings"
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
	keyRelatedProductsCache               = "related_%s_%s_%s" // countryID, itemID, paramsHash
	configProductsRelatedKey              = "PRODUCTS-RELATED.CONFIG"
)

type productsRelated struct {
	outPortCatalogProduct sharedOutPorts.CatalogProduct
	outPortCache          sharedOutPorts.Cache
	outPortConfig         sharedOutPorts.ConfigOutPort
}

func NewProductsRelated(outPortCatalogProduct sharedOutPorts.CatalogProduct,
	outPortCache sharedOutPorts.Cache,
	outPortConfig sharedOutPorts.ConfigOutPort) inPorts.ProductsRelated {
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
	var productsRelatedItem model.ProductsRelatedItem
	var correlationID string
	if id, ok := ctx.Get(enums.HeaderCorrelationID); ok {
		if idStr, typeOk := id.(string); typeOk {
			correlationID = idStr
		}
	}
	if correlationID == "" {
		correlationID = utils.GetCorrelationID(ctx.GetHeader(enums.HeaderCorrelationID))
	}
	var rs []model.ProductsRelatedItem

	// 1. Intenta obtener de la caché
	err := findProductsRelatedInCache(ctx, p.outPortCache, countryID, itemID, &rs)
	if err == nil && len(rs) > 0 {
		log.Infof(enums.LogFormat, correlationID, GetRelatedItemsLog, "Successfully retrieved from cache")
		return rs, nil
	}
	if err != nil {
		log.Warnf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Cache find error: %v", err))
	}

	configProductsRelated, err := p.outPortConfig.GetConfigBestSeller(ctx, countryID, configProductsRelatedKey)
	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetRelatedItemsLog,
			fmt.Sprintf(enums.GetData, "error", "repositoryConfig"))
		return nil, err
	}

	log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetRelatedItemsLog,
		fmt.Sprintf(enums.GetData, "Success", fmt.Sprintf("%+v", configProductsRelated)))
	originalItem, err := p.getProductsRelated(ctx, countryID, itemID)
	if err != nil {
		log.Errorf(enums.LogFormat, correlationID, GetRelatedItemsLog,
			fmt.Sprintf("Error getting original item's brand: %v", err))
		return nil, err
	}

	if len(originalItem.IDSuggested) == 0 {
		log.Warnf(enums.LogFormat, correlationID, GetRelatedItemsLog,
			fmt.Sprintf("id suggested not found for item %s", itemID))

		return []model.ProductsRelatedItem{}, nil
	}

	productsFromAlgolia, err := p.getProductsRelatedItemsFromAlgolia(
		ctx,
		countryID,
		strings.Trim(strings.Join(strings.Fields(fmt.Sprint(originalItem.IDSuggested)), ", "), "[]"),
		itemID,
		configProductsRelated,
	)
	if err != nil {
		log.Errorf(enums.LogFormat, correlationID, getProductsRelatedItemsFromAlgoliaLog,
			fmt.Sprintf("Error getting brand items from Algolia: %v", err))
		return nil, err
	}

	var processedItems []sharedModel.ProductInformation
	for _, product := range productsFromAlgolia {
		if p.shouldIncludeProduct(product) {
			processedItems = append(processedItems, product)
		}
	}

	sort.SliceStable(processedItems, func(i, j int) bool {
		return len(processedItems[i].StoresWithStock) > len(processedItems[j].StoresWithStock)
	})

	for _, productInfo := range processedItems {
		mappedItem := mappers.MapProductInformationToProductsRelatedItem(&productsRelatedItem, &productInfo)
		rs = append(rs, mappedItem)
	}

	if len(rs) > 0 {
		err = saveProductsRelatedInCache(ctx, p.outPortCache, countryID, itemID, rs)
		if err != nil {
			log.Warnf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Cache save error: %v", err))
		}
	}

	log.Infof(enums.LogFormat, correlationID, GetRelatedItemsLog, "Successfully retrieved and processed products related items")
	return rs, nil
}

func findProductsRelatedInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache,
	countryID, itemID string, response *[]model.ProductsRelatedItem) error {
	var correlationID string
	if id, ok := ctx.Get(enums.HeaderCorrelationID); ok {
		if idStr, typeOk := id.(string); typeOk {
			correlationID = idStr
		}
	}
	if correlationID == "" {
		correlationID = utils.GetCorrelationID(ctx.GetHeader(enums.HeaderCorrelationID))
	}
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

func (p *productsRelated) getProductsRelatedItemsFromAlgolia(ctx *gin.Context, countryID, idSuggest, excludeItemID string, configProductsRelated sharedModel.ConfigBestSeller) ([]sharedModel.ProductInformation, error) {
	var correlationID string
	if id, ok := ctx.Get(enums.HeaderCorrelationID); ok {
		if idStr, typeOk := id.(string); typeOk {
			correlationID = idStr
		}
	}
	if correlationID == "" {
		correlationID = utils.GetCorrelationID(ctx.GetHeader(enums.HeaderCorrelationID))
	}

	query := fmt.Sprintf(configProductsRelated.QueryProducts, idSuggest)
	// Asegurarse de que el header X-Custom-City se propaga
	utils.PropagateHeader(ctx, enums.HeaderXCustomCity)

	products, err := p.outPortCatalogProduct.GetProductsInformationByQuery(ctx, query, countryID)
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

	cacheKey := fmt.Sprintf(keyRelatedProductsCache, countryID, itemID, "")
	dataToCache, errMarshal := json.Marshal(rs)
	if errMarshal != nil {
		log.Warnf(enums.LogFormat, correlationID, GetRelatedItemsLog,
			fmt.Sprintf("Error marshalling data for cache. Key: %s, Error: %v", cacheKey, errMarshal))
		return errMarshal
	}

	// Usar el TTL correcto de las variables de entorno
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

func (p *productsRelated) shouldIncludeProduct(product sharedModel.ProductInformation) bool {
	if len(product.StoresWithStock) == 0 || product.Status != "A" {
		return false
	}

	return true
}
