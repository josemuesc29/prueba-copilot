package app

import (
	"encoding/json"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model/mappers"
	inPorts "ftd-td-catalog-item-read-services/internal/same-brand/domain/ports/in"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedOutPorts "ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"sort"
	"time"
)

const (
	GetItemsBySameBrandLog        = "SameBrandService.GetItemsBySameBrand"
	getItemBrandLog               = "SameBrandService.GetItemBrand"
	getBrandItemsFromAlgoliaLog   = "SameBrandService.GetBrandItemsFromAlgolia"
	findSameBrandInCacheLog       = "SameBrandService.findSameBrandInCache"
	repositoryProxyCatalogProduct = "repository proxy catalog product"
	repositoryCache               = "repository cache"
	indexCatalogProducts          = "index catalog products"
	keySameBrandCache             = "same_brand_%s_%s" // countryID, itemID
	maxItemsLimit                 = 24
)

type sameBrand struct {
	outPortCatalogProduct sharedOutPorts.CatalogProduct
	outPortCache          sharedOutPorts.Cache
	// outPortOffer          sharedOutPorts.OfferOutPort // Eliminado temporalmente
}

func NewSameBrand(
	outPortCatalogProduct sharedOutPorts.CatalogProduct,
	outPortCache sharedOutPorts.Cache,
	// outPortOffer sharedOutPorts.OfferOutPort, // Eliminado temporalmente
) inPorts.SameBrand {
	return &sameBrand{
		outPortCatalogProduct: outPortCatalogProduct,
		outPortCache:          outPortCache,
		// outPortOffer:          outPortOffer, // Eliminado temporalmente
	}
}

func (s *sameBrand) GetItemsBySameBrand(ctx *gin.Context, countryID, itemID string) ([]model.SameBrandItem, error) {
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
		log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog, "Successfully retrieved from cache")
		return rs, nil
	}
	if err != nil { // Log cache error but continue
		log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog, fmt.Sprintf("Cache find error: %v", err))
	}

	// 2. Obtener la marca del ítem original
	originalItem, err := s.getItemBrand(ctx, countryID, itemID)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Error getting original item's brand: %v", err))
		return nil, err
	}

	if originalItem.Brand == "" {
		log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Brand not found for item %s", itemID))
		// Devolver lista vacía en lugar de error si la marca no se encuentra, según el comportamiento típico de "relacionados"
		return []model.SameBrandItem{}, nil
	}

	// 3. Buscar ítems de la misma marca en Algolia
	productsFromAlgolia, err := s.getBrandItemsFromAlgolia(ctx, countryID, originalItem.Brand, itemID)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Error getting brand items from Algolia: %v", err))
		return nil, err
	}

	// 4. Aplicar reglas de negocio y transformar
	var processedItems []sharedModel.ProductInformation
	for _, product := range productsFromAlgolia {
		if s.shouldIncludeProduct(product) {
			processedItems = append(processedItems, product)
		}
	}

	// 4.1. Aplicar ofertas (simples y prime)
	// Esto podría necesitar una llamada a un servicio de ofertas si la información no está en ProductInformation
	// Por ahora, asumimos que la información de oferta ya está o se mapea directamente.
	// Si se requiere una llamada externa, se debe inyectar el puerto correspondiente.
	// Ejemplo: itemsWithOffers, err := s.outPortOffer.ApplyOffers(ctx, processedItems, countryID)

	// 4.2. Ordenamiento por stock (descendente) - Usar len(StoresWithStock) como proxy de cantidad de stock
	sort.SliceStable(processedItems, func(i, j int) bool {
		return len(processedItems[i].StoresWithStock) > len(processedItems[j].StoresWithStock)
	})

	// 4.3. Limitar a maxItemsLimit
	if len(processedItems) > maxItemsLimit {
		processedItems = processedItems[:maxItemsLimit]
	}

	// 5. Mapear a la estructura de respuesta
	for _, productInfo := range processedItems {
		// Aquí es donde el mapeo de sharedModel.ProductInformation a model.SameBrandItem ocurre.
		// El mapper debe ser capaz de manejar la transformación de ofertas.
		rs = append(rs, mappers.MapProductInformationToSameBrandItem(productInfo))
	}

	// 6. Guardar en caché
	if len(rs) > 0 {
		err = saveSameBrandInCache(ctx, s.outPortCache, countryID, itemID, rs)
		if err != nil {
			log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog, fmt.Sprintf("Cache save error: %v", err))
			// No devolver error al cliente si falla el guardado en caché
		}
	}

	log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog, "Successfully retrieved and processed same brand items")
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
		log.Printf(enums.LogFormat, correlationID, getItemBrandLog,
			fmt.Sprintf("Error from CatalogProduct port: %v. Repo: %s", err, repositoryProxyCatalogProduct))
		return sharedModel.ProductInformation{}, err
	}

	if len(items) == 0 {
		log.Printf(enums.LogFormat, correlationID, getItemBrandLog, fmt.Sprintf("Item not found: %s", itemID))
		return sharedModel.ProductInformation{}, fmt.Errorf("item not found: %s", itemID)
	}

	return items[0], nil
}

func (s *sameBrand) getBrandItemsFromAlgolia(ctx *gin.Context, countryID, brand, excludeItemID string) ([]sharedModel.ProductInformation, error) {
	var correlationID string
	if id, ok := ctx.Get(enums.HeaderCorrelationID); ok {
		if idStr, typeOk := id.(string); typeOk {
			correlationID = idStr
		}
	}
	if correlationID == "" {
		correlationID = utils.GetCorrelationID(ctx.GetHeader(enums.HeaderCorrelationID))
	}
	// Construir la query para Algolia
	// La query debe buscar por marca y excluir el itemID original.
	// Algolia soporta filtros negativos, ej: "brand:MiMarca AND NOT objectID:originalItemID"
	// Sin embargo, es más simple filtrar el excludeItemID después si la cantidad de items no es excesiva.
	// Por ahora, solo filtramos por marca y luego excluimos.
	query := fmt.Sprintf("brand:\"%s\"", brand)
	// query := fmt.Sprintf("brand:\"%s\" AND NOT objectID:\"%s\"", brand, excludeItemID) // Alternativa

	// Asegurarse de que el header X-Custom-City se propaga
	utils.PropagateHeader(ctx, enums.HeaderXCustomCity)

	// La llamada a GetProductsInformationByQuery ya debería manejar el header X-Custom-City si está en el contexto.
	products, err := s.outPortCatalogProduct.GetProductsInformationByQuery(ctx, query, countryID)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, getBrandItemsFromAlgoliaLog,
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

// shouldIncludeProduct aplica las reglas de negocio para determinar si un producto debe ser incluido.
// Por ahora: activo y con stock.
func (s *sameBrand) shouldIncludeProduct(product sharedModel.ProductInformation) bool {
	// CRITERIOS DE ACEPTACIÓN: Transformar la respuesta de Algolia según las reglas de negocio aplicadas:
	// stock, ofertas simples, ofertas prime, ordenamiento por stock

	// 1. Stock:
	//    - `product.HasStock` (booleano)
	//    - `len(product.StoresWithStock) > 0` (debe tener al menos una tienda con stock)
	//    - `product.Status == "A"` (Activo)
	if !product.HasStock || len(product.StoresWithStock) == 0 || product.Status != "A" {
		return false
	}

	// 2. Ofertas Simples y Prime:
	//    La transformación de ofertas (cómo se muestran OfferText, OfferPrice, etc.)
	//    debería ocurrir en el mapper o después de esta selección inicial.
	//    Esta función decide si el *producto base* es elegible.
	//    Si hay reglas de negocio que EXCLUYEN productos basados en el tipo de oferta (ej. no mostrar si solo tiene oferta X),
	//    se agregarían aquí. Por ahora, asumimos que todas las ofertas son "buenas" para inclusión.

	// Ejemplo de una regla de oferta (si fuera necesario):
	// if product.IsExclusivePrimeOffer && !userIsPrime { return false }

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
		log.Printf(enums.LogFormat, correlationID, findSameBrandInCacheLog,
			fmt.Sprintf("Error getting from cache. Key: %s, Error: %v", cacheKey, err))
		return err // Devolver error para que el flujo principal sepa que la caché falló
	}

	if cachedData == "" {
		log.Printf(enums.LogFormat, correlationID, findSameBrandInCacheLog,
			fmt.Sprintf("Cache miss. Key: %s", cacheKey))
		return nil // No es un error, solo un cache miss
	}

	err = json.Unmarshal([]byte(cachedData), response)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, findSameBrandInCacheLog,
			fmt.Sprintf("Error unmarshalling cached data. Key: %s, Error: %v", cacheKey, err))
		return err // Error al decodificar, tratar como cache miss problemático
	}

	log.Printf(enums.LogFormat, correlationID, findSameBrandInCacheLog,
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
		log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Error marshalling data for cache. Key: %s, Error: %v", cacheKey, errMarshal))
		return errMarshal
	}

	// Usar el TTL correcto de las variables de entorno
	ttl := time.Duration(config.Enviroments.RedisSameBrandTTL) * time.Minute
	if ttl <= 0 { // Fallback a un TTL por defecto si no está configurado o es inválido
		ttl = 60 * time.Minute // Ejemplo: 1 hora
		log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Invalid or missing RedisSameBrandTTL, using default: %v. Key: %s", ttl, cacheKey))
	}

	err := outPortCache.Set(ctx, cacheKey, string(dataToCache), ttl)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
			fmt.Sprintf("Error saving to cache. Key: %s, Error: %v", cacheKey, err))
		return err
	}

	log.Printf(enums.LogFormat, correlationID, GetItemsBySameBrandLog,
		fmt.Sprintf("Successfully saved to cache. Key: %s, TTL: %v", cacheKey, ttl))
	return nil
}
