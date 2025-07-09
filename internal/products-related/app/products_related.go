package app

import (
	"encoding/json"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model/mappers"
	inPorts "ftd-td-catalog-item-read-services/internal/products-related/domain/ports/in"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	sharedOutPorts "ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"time"
)

const (
	GetRelatedItemsLog        = "ProductsRelatedService.GetRelatedItems"
	findRelatedInCacheLog     = "ProductsRelatedService.findRelatedInCache"
	repositoryProxyCatalogProduct = "repository proxy catalog product"
	repositoryCache               = "repository cache"
	keyRelatedCache               = "related_%s_%s"
	maxItemsLimit                 = 24
)

type productsRelated struct {
	outPortCatalogProduct sharedOutPorts.CatalogProduct
	outPortCache          sharedOutPorts.Cache
}

func NewProductsRelated(outPortCatalogProduct sharedOutPorts.CatalogProduct, outPortCache sharedOutPorts.Cache) inPorts.ProductsRelated {
	return &productsRelated{
		outPortCatalogProduct: outPortCatalogProduct,
		outPortCache:          outPortCache,
	}
}

func (p *productsRelated) GetRelatedItems(ctx *gin.Context, countryID, itemID string, params string) ([]model.RelatedItem, error) {
	var rs []model.RelatedItem

	findRelatedInCache(ctx, p.outPortCache, countryID, itemID, &rs)
	if len(rs) > 0 {
		return rs, nil
	}

	// Use GetProductInformation from proxy_catalog_product.go
	productInfo, err := p.outPortCatalogProduct.GetProductInformation(ctx, itemID)
	if err != nil {
		log.Printf("Error getting product information: %v", err)
		return nil, err
	}

	// Use productInfo and params to query related items from Algolia
	// Here we assume params is a query string for Algolia
	products, err := p.outPortCatalogProduct.GetProductsInformationByQuery(ctx, params, countryID)
	if err != nil {
		log.Printf("Error getting related products from Algolia: %v", err)
		return nil, err
	}

	count := 0
	for _, product := range products {
		if product.ObjectID != itemID && count < maxItemsLimit {
			// Apply business rules: stock, offers, etc.
			if p.shouldIncludeProduct(product) {
				rs = append(rs, mappers.MapProductInformationToRelatedItem(product))
				count++
			}
		}
	}

	// Save to cache
	saveRelatedInCache(ctx, p.outPortCache, countryID, itemID, rs)

	log.Printf("Related items retrieved successfully")
	return rs, nil
}

func (p *productsRelated) shouldIncludeProduct(product sharedModel.ProductInformation) bool {
	// Implement business rules:
	// - Check stock
	// - Apply simple offers rules
	// - Apply prime offers rules
	// - Sort by stock

	// For now, include all products with stock and active status
	return product.HasStock == true && product.Status == "A"
}

func findRelatedInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache,
	countryID, itemID string, response *[]model.RelatedItem) {
	cacheKey := fmt.Sprintf(keyRelatedCache, countryID, itemID)
	cachedData, err := outPortCache.Get(ctx, cacheKey)

	if err != nil {
		log.Printf("Error getting cache: %v", err)
		return
	}

	if cachedData == "" {
		log.Printf("Cache not found")
		return
	}

	err = json.Unmarshal([]byte(cachedData), response)
	if err != nil {
		log.Printf("Error decoding JSON from cache: %v", err)
		return
	}

	log.Printf("Cache hit success")
}

func saveRelatedInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache, countryID, itemID string, rs []model.RelatedItem) {
	if len(rs) > 0 {
		cacheKey := fmt.Sprintf(keyRelatedCache, countryID, itemID)
		dataToCache, err := json.Marshal(rs)
		if err == nil {
			err := outPortCache.Set(ctx, cacheKey, string(dataToCache), time.Duration(config.Enviroments.RedisSameBrandDepartmentTTL)*time.Minute)
			if err != nil {
				log.Printf("Error saving cache: %v", err)
			}
		}
	}
}