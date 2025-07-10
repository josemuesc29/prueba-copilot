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
	// defaultHitsPerPage            = 10 // Not used directly, maxItemsLimit is used for hitsPerPage
	maxItemsLimit                 = 24 // Este es el límite de la HU original, lo mantenemos.
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

	// Crear un hash de los parámetros para la clave de caché
	paramsKey := fmt.Sprintf("nearby:%s;city:%s;query:%s;index:%s;params:%s", nearbyStores, city, queryAlgolia, indexName, algoliaParamsStr)
	hasher := sha256.New()
	hasher.Write([]byte(paramsKey))
	paramsHash := hex.EncodeToString(hasher.Sum(nil))

	cacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, paramsHash)

	// Intentar obtener de caché
	cachedData, err := p.outPortCache.Get(ctx, cacheKey)
	if err == nil && cachedData != "" {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Cache hit for key: %s", cacheKey))
		err = json.Unmarshal([]byte(cachedData), &response)
		if err == nil {
			return response, nil
		}
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Error unmarshalling cached data: %v", err))
	} else if err != nil {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Error getting from cache (key: %s): %v", cacheKey, err))
	} else {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Cache miss for key: %s", cacheKey))
	}

	// Construir los parámetros para la consulta a Algolia
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

	if !strings.Contains(effectiveAlgoliaParams, "hitsPerPage") {
		if effectiveAlgoliaParams != "" {
			effectiveAlgoliaParams = fmt.Sprintf("%s&hitsPerPage=%d", effectiveAlgoliaParams, maxItemsLimit)
		} else {
			effectiveAlgoliaParams = fmt.Sprintf("hitsPerPage=%d", maxItemsLimit)
		}
	}

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
		// maxItemsLimit es manejado por hitsPerPage en la query a Algolia
	}
	log.Printf(enums.LogFormat, correlationID, GetRelatedItemsLog, fmt.Sprintf("Filtered products to %d items after shouldIncludeProduct", len(filteredProducts)))

	currentPage := 0
	currentHitsPerPage := maxItemsLimit
	// Para obtener page y hitsPerPage reales, necesitaríamos parsear `finalAlgoliaQuery`
    // o que `GetProductsInformationByQuery` los devuelva.

	response = mappers.MapToAlgoliaRelatedProductsResponse(filteredProducts, queryAlgolia, currentPage, currentHitsPerPage)

	jsonData, err := json.Marshal(response)
	if err == nil {
		errCacheSet := p.outPortCache.Set(ctx, cacheKey, string(jsonData), time.Duration(config.Enviroments.RedisSameBrandDepartmentTTL)*time.Minute) // Usando TTL de same-brand por ahora
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

// Modificado para aceptar itemID y asegurarse de no incluir el mismo producto
func (p *productsRelated) shouldIncludeProduct(product sharedModel.ProductInformation, originalItemID string) bool {
	// No incluir el producto original si Algolia lo devuelve por alguna razón
	if product.ObjectID == originalItemID {
		return false
	}
	// Implement business rules:
	// - Check stock
	// - Apply simple offers rules
	// - Apply prime offers rules
	// - Sort by stock
	return product.HasStock == true && product.Status == "A"
}

// findRelatedInCache y saveRelatedInCache ya no son necesarios como funciones separadas,
// su lógica está integrada en GetRelatedItems. Se pueden eliminar si no se usan en otro lugar.
// Por ahora las comento por si fueran necesarias para alguna refactorización futura o prueba.

/*
func findRelatedInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache,
	countryID, itemID string, paramsHash string, response *model.AlgoliaRelatedProductsResponse) bool { // Modificado para nueva key y response type
	cacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, paramsHash)
	cachedData, err := outPortCache.Get(ctx, cacheKey)

	correlationID := ""
	if ctx != nil {
		value, exists := ctx.Get(string(enums.HeaderCorrelationID))
		if exists {
			if id, ok := value.(string); ok {
				correlationID = id
			}
		}
	}

	if err != nil {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Error getting from cache (key: %s): %v", cacheKey, err))
		return false
	}

	if cachedData == "" {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Cache miss for key: %s", cacheKey))
		return false
	}

	err = json.Unmarshal([]byte(cachedData), response)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Error unmarshalling cached data (key: %s): %v", cacheKey, err))
		return false // Tratar como cache miss si no se puede deserealizar
	}

	log.Printf(enums.LogFormat, correlationID, findRelatedInCacheLog, fmt.Sprintf("Cache hit for key: %s", cacheKey))
	return true
}

func saveRelatedInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache, countryID, itemID, paramsHash string, rs model.AlgoliaRelatedProductsResponse) { // Modificado para nueva key y type
	correlationID := ""
	if ctx != nil {
		value, exists := ctx.Get(string(enums.HeaderCorrelationID))
		if exists {
			if id, ok := value.(string); ok {
				correlationID = id
			}
		}
	}
	// Solo guardar en cache si hay resultados en la respuesta de Algolia
	if len(rs.Results) > 0 && len(rs.Results[0].Hits) > 0 {
		cacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, paramsHash)
		dataToCache, err := json.Marshal(rs)
		if err == nil {
			// Usar TTL de config, ejemplo config.Enviroments.RedisProductsRelatedTTL, si no existe usar uno genérico o el de same-brand
			ttl := time.Duration(config.Enviroments.RedisSameBrandDepartmentTTL) * time.Minute // TODO: Considerar un TTL específico para related items
			errSet := outPortCache.Set(ctx, cacheKey, string(dataToCache), ttl)
			if errSet != nil {
				log.Printf(enums.LogFormat, correlationID, saveRelatedInCacheLog, fmt.Sprintf("Error saving to cache (key: %s): %v", cacheKey, errSet))
			} else {
				log.Printf(enums.LogFormat, correlationID, saveRelatedInCacheLog, fmt.Sprintf("Successfully saved to cache (key: %s)", cacheKey))
			}
		} else {
			log.Printf(enums.LogFormat, correlationID, saveRelatedInCacheLog, fmt.Sprintf("Error marshalling response for cache (key: %s): %v", cacheKey, err))
		}
	} else {
		log.Printf(enums.LogFormat, correlationID, saveRelatedInCacheLog, fmt.Sprintf("Skipping cache save, no items to cache (key: %s)", fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, paramsHash)))
	}
}
*/