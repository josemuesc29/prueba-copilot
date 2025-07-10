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
	GetRelatedItemsLog            = "ProductsRelatedService.GetRelatedItems"
	findRelatedInCacheLog         = "ProductsRelatedService.findRelatedInCache"
	saveRelatedInCacheLog         = "ProductsRelatedService.saveRelatedInCache"
	repositoryProxyCatalogProduct = "repository proxy catalog product"
	repositoryCache               = "repository cache"
	keyRelatedCacheFormat         = "related_%s_%s_%s" // countryID, itemID, paramsHash
	// maxItemsLimit ya no se usa aquí. El control de ítems es por 'hitsPerPage' en algoliaParamsStr.
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
	nearbyStores, city, queryAlgolia, indexName, algoliaParamsStr string,
) (model.AlgoliaRelatedProductsResponse, error) {
	var response model.AlgoliaRelatedProductsResponse
	correlationID := ""
	if ctx != nil {
		value, exists := ctx.Get(string(enums.HeaderCorrelationID))
		if exists {
			if id, ok := value.(string); ok {
				correlationID = id
			}
		}
	}

	paramsKey := fmt.Sprintf("nearby:%s;city:%s;query:%s;index:%s;params:%s", nearbyStores, city, queryAlgolia, indexName, algoliaParamsStr)
	hasher := sha256.New()
	hasher.Write([]byte(paramsKey))
	paramsHash := hex.EncodeToString(hasher.Sum(nil))
	cacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, paramsHash)

	cachedData, err := p.outPortCache.Get(ctx, cacheKey)
	if err == nil && cachedData != "" {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Cache hit for key: %s", cacheKey))
		if errUnmarshal := json.Unmarshal([]byte(cachedData), &response); errUnmarshal == nil {
			return response, nil
		} else {
			log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Error unmarshalling cached data: %v", errUnmarshal))
		}
	} else if err != nil {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Error getting from cache (key: %s): %v", cacheKey, err))
	} else {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Cache miss for key: %s", cacheKey))
	}

	var effectiveAlgoliaParams string
	if queryAlgolia != "" {
		effectiveAlgoliaParams = "query=" + queryAlgolia
	}

	if algoliaParamsStr != "" {
		if effectiveAlgoliaParams != "" {
			effectiveAlgoliaParams = effectiveAlgoliaParams + "&" + algoliaParamsStr
		} else {
			effectiveAlgoliaParams = algoliaParamsStr
		}
	}
	// Ya no se añade hitsPerPage por defecto. Debe venir en algoliaParamsStr.

	filterExclusion := fmt.Sprintf("NOT objectID:%s", itemID)
	if strings.Contains(effectiveAlgoliaParams, "filters=") {
		parts := strings.SplitN(effectiveAlgoliaParams, "filters=", 2)
		filterAndRest := ""
		if len(parts) > 1 {
			filterAndRest = parts[1]
		}
		currentFiltersValue := ""
		restOfParams := ""
		ampersandIndex := strings.Index(filterAndRest, "&")
		if ampersandIndex != -1 {
			currentFiltersValue = filterAndRest[:ampersandIndex]
			restOfParams = filterAndRest[ampersandIndex:]
		} else {
			currentFiltersValue = filterAndRest
		}
		newFilters := fmt.Sprintf("%s AND %s", currentFiltersValue, filterExclusion)
		effectiveAlgoliaParams = fmt.Sprintf("%sfilters=%s%s", parts[0], newFilters, restOfParams)
	} else {
		if effectiveAlgoliaParams != "" {
			effectiveAlgoliaParams = fmt.Sprintf("%s&filters=%s", effectiveAlgoliaParams, filterExclusion)
		} else {
			effectiveAlgoliaParams = fmt.Sprintf("filters=%s", filterExclusion)
		}
	}

	finalAlgoliaQuery := effectiveAlgoliaParams
	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Querying Algolia with params: [%s] for country: %s, itemID: %s", finalAlgoliaQuery, countryID, itemID))

	products, err := p.outPortCatalogProduct.GetProductsInformationByQuery(ctx, finalAlgoliaQuery, countryID)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Error getting related products from Algolia: %v", err))
		return model.AlgoliaRelatedProductsResponse{}, err
	}
	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Received %d products from Algolia", len(products)))

	var filteredProducts []sharedModel.ProductInformation
	for _, product := range products {
		if p.shouldIncludeProduct(product, itemID) {
			filteredProducts = append(filteredProducts, product)
		}
	}
	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Filtered products to %d items after shouldIncludeProduct", len(filteredProducts)))

	// Intentar extraer page y hitsPerPage de los parámetros para el mapper
	// Esto es una simplificación; una extracción robusta requeriría parsear `finalAlgoliaQuery`
	currentPage := 0 // Default
	currentHitsPerPage := len(filteredProducts) // Default al número de hits actuales si no se especifica

	queryParamsMap := parseQueryParams(finalAlgoliaQuery)
	if pageStr, ok := queryParamsMap["page"]; ok {
		if pageInt, errConv := strconv.Atoi(pageStr); errConv == nil {
			currentPage = pageInt
		}
	}
	if hppStr, ok := queryParamsMap["hitsPerPage"]; ok {
		if hppInt, errConv := strconv.Atoi(hppStr); errConv == nil {
			currentHitsPerPage = hppInt
		}
	}


	response = mappers.MapToAlgoliaRelatedProductsResponse(filteredProducts, queryAlgolia, currentPage, currentHitsPerPage)

	jsonData, err := json.Marshal(response)
	if err == nil {
		errCacheSet := p.outPortCache.Set(ctx, cacheKey, string(jsonData), time.Duration(config.Enviroments.RedisSameBrandDepartmentTTL)*time.Minute)
		if errCacheSet != nil {
			log.Printf(enums.LogFormat, correlationID, saveRelatedInCacheLog, fmt.Sprintf("Error saving to cache (key: %s): %v", cacheKey, errCacheSet))
		} else {
			log.Printf(enums.LogFormat, correlationID, saveRelatedInCacheLog, fmt.Sprintf("Successfully saved to cache (key: %s)", cacheKey))
		}
	} else {
		log.Printf(enums.LogFormat, correlationID, saveRelatedInCacheLog, fmt.Sprintf("Error marshalling response for cache: %v", err))
	}

	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, "Related items retrieved successfully")
	return response, nil
}

func (p *productsRelated) shouldIncludeProduct(product sharedModel.ProductInformation, originalItemID string) bool {
	if product.ObjectID == originalItemID {
		return false
	}
	return product.HasStock == true && product.Status == "A"
}

// parseQueryParams es una función helper simple para extraer page y hitsPerPage.
// Una implementación robusta consideraría múltiples valores, etc.
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

/*
Comentarios de funciones de caché eliminadas para brevedad, ya que su lógica
está integrada y no se usan directamente.
*/