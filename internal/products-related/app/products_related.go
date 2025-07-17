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

func (p *productsRelated) GetRelatedItems(
	ctx *gin.Context,
	countryID, itemID string,
	nearbyStores, city, category, indexName, algoliaParamsStr string,
) (model.AlgoliaRecommendResponse, error) {
	var response model.AlgoliaRecommendResponse
	correlationID := ""
	if ctx != nil {
		value, exists := ctx.Get(string(enums.HeaderCorrelationID))
		if exists {
			if id, ok := value.(string); ok {
				correlationID = id
			}
		}
	}

	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Getting related products for itemID: %s, category: %s, country: %s", itemID, category, countryID))

	// For now, we are skipping the cache logic to focus on the new implementation.

	recommendResponse, err := p.outPortCatalogProduct.GetRelatedProducts(ctx, itemID, category, countryID)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Error getting related products from Algolia: %v", err))
		return response, err
	}

	// The new response structure is different, so we adapt.
	// The response from GetRelatedProducts already contains the hits.
	if len(recommendResponse.Results) > 0 {
		// We assume the first result is the one we want, as per the request structure.
		hits := recommendResponse.Results[0].Hits
		log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Received %d related products from Algolia Recommend", len(hits)))

		var filteredProducts []sharedModel.ProductInformation
		for _, product := range hits {
			if p.shouldIncludeProduct(product, itemID) {
				filteredProducts = append(filteredProducts, product)
			}
		}
		log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Filtered products to %d items after shouldIncludeProduct", len(filteredProducts)))

		// We need to decide how to present this. For now, let's just use the hits.
		// The old response model `AlgoliaRelatedProductsResponse` is not compatible.
		// We will return the new `AlgoliaRecommendResponse` directly.
		recommendResponse.Results[0].Hits = filteredProducts
		response = recommendResponse
	} else {
		log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, "Received no results from Algolia Recommend")
	}

	// We can re-add caching logic here later if needed.

	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, "Related items retrieved successfully")
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
