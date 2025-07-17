package app

import (
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model/mappers"
	inPorts "ftd-td-catalog-item-read-services/internal/products-related/domain/ports/in"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedOutPorts "ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	"strconv"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
)

const (
	GetRelatedItemsLog    = "ProductsRelatedService.GetRelatedItems"
	findRelatedInCacheLog = "ProductsRelatedService.findRelatedInCache"
	saveRelatedInCacheLog = "ProductsRelatedService.saveRelatedInCache"
	keyRelatedCacheFormat = "related_%s_%s_%s" // countryID, itemID, paramsHash
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

const (
	// ... (existing constants)
	findRelatedInCacheLog = "ProductsRelatedService.findRelatedInCache"
	saveRelatedInCacheLog = "ProductsRelatedService.saveRelatedInCache"
	keyRelatedCacheFormat = "related_products_%s_%s" // countryID, itemID
)

func (p *productsRelated) GetRelatedItems(
	ctx *gin.Context,
	countryID, itemID string,
) ([]model.RelatedProductItem, error) {
	var response []model.RelatedProductItem
	correlationID := ""
	if ctx != nil {
		value, exists := ctx.Get(string(enums.HeaderCorrelationID))
		if exists {
			if id, ok := value.(string); ok {
				correlationID = id
			}
		}
	}

	// Attempt to retrieve from cache first
	cacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID)
	cachedData, errCache := p.outPortCache.Get(ctx, cacheKey)
	if errCache == nil && cachedData != "" {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Cache hit for key: %s", cacheKey))
		if errUnmarshal := json.Unmarshal([]byte(cachedData), &response); errUnmarshal == nil {
			return response, nil
		} else {
			log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Error unmarshalling cached data: %v", errUnmarshal))
		}
	} else if errCache != nil {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Error getting from cache (key: %s): %v", cacheKey, errCache))
	} else {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Cache miss for key: %s", cacheKey))
	}

	// Step 1: Get the main product's information
	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Getting main product info for itemID: %s", itemID))
	productsInfo, err := p.outPortCatalogProduct.GetProductsInformationByObjectID(ctx, []string{itemID}, countryID)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Error getting main product info: %v", err))
		return nil, err
	}
	if len(productsInfo) == 0 {
		log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Main product with itemID: %s not found.", itemID))
		return nil, fmt.Errorf("main product with itemID: %s not found", itemID)
	}
	mainProductInfo := productsInfo[0]

	// Step 2: Extract id_suggested
	if len(mainProductInfo.SuggestedID) == 0 {
		log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("No suggested ID found for itemID: %s. Returning empty list.", itemID))
		return response, nil // Return empty list as per decision
	}
	// Assuming we use the first suggested ID
	suggestedID := mainProductInfo.SuggestedID[0]
	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Found suggested ID: %d for itemID: %s", suggestedID, itemID))

	// Step 3: Get the configuration for the query
	configKey := "RELATED-PRODUCTS.CONFIG" // The key for our new configuration
	config, err := p.outPortConfig.GetConfigRelatedProducts(ctx, countryID, configKey)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Error getting related products config: %v", err))
		return nil, err
	}

	// Step 4: Build and execute the query using the fetched config
	finalQuery := fmt.Sprintf(config.QueryProducts, suggestedID)

	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Querying Algolia with params: [%s]", finalQuery))
	products, err := p.outPortCatalogProduct.GetProductsInformationByQuery(ctx, finalQuery, countryID)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Error getting related products from Algolia: %v", err))
		return nil, err
	}
	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Received %d products from Algolia", len(products)))

	// Step 5: Filter and map the response
	for _, product := range products {
		if p.shouldIncludeProduct(product, itemID) {
			mappedItem := mappers.MapProductInformationToRelatedItem(&product)
			response = append(response, mappedItem)
		}
	}

	// Save to cache before returning
	if len(response) > 0 {
		jsonData, errMarshal := json.Marshal(response)
		if errMarshal == nil {
			ttl := time.Duration(config.Enviroments.RedisSameBrandDepartmentTTL) * time.Minute
			errCacheSet := p.outPortCache.Set(ctx, cacheKey, string(jsonData), ttl)
			if errCacheSet != nil {
				log.Printf(enums.LogFormat, correlationID, saveRelatedInCacheLog, fmt.Sprintf("Error saving to cache (key: %s): %v", cacheKey, errCacheSet))
			} else {
				log.Printf(enums.LogFormat, correlationID, saveRelatedInCacheLog, fmt.Sprintf("Successfully saved to cache (key: %s) with TTL %v", cacheKey, ttl))
			}
		} else {
			log.Printf(enums.LogFormat, correlationID, saveRelatedInCacheLog, fmt.Sprintf("Error marshalling response for cache: %v", errMarshal))
		}
	}

	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, "Related items retrieved and mapped successfully")
	return response, nil
}

func (p *productsRelated) shouldIncludeProduct(product sharedModel.ProductInformation, originalItemID string) bool {
	if product.ObjectID == originalItemID {
		return false
	}
	return product.HasStock && product.Status == "A"
}

func parseQueryParams(queryStr string) map[string]string {
	params := make(map[string]string)
	pairs := strings.Split(queryStr, "&")
	for _, pair := range pairs {
		kv := strings.SplitN(pair, "=", 2)
		if len(kv) == 2 {
			params[kv[0]] = kv[1]
		}
	}
	return params
}
