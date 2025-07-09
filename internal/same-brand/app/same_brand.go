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
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
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
	keySameBrandCache             = "same_brand_%s_%s"
	maxItemsLimit                 = 24
)

type sameBrand struct {
	outPortCatalogProduct sharedOutPorts.CatalogProduct
	outPortCache          sharedOutPorts.Cache
}

func NewSameBrand(outPortCatalogProduct sharedOutPorts.CatalogProduct, outPortCache sharedOutPorts.Cache) inPorts.SameBrand {
	return &sameBrand{
		outPortCatalogProduct: outPortCatalogProduct,
		outPortCache:          outPortCache,
	}
}

func (s *sameBrand) GetItemsBySameBrand(ctx *gin.Context, countryID, itemID string) ([]model.SameBrandItem, error) {
	var rs []model.SameBrandItem

	findSameBrandInCache(ctx, s.outPortCache, countryID, itemID, &rs)
	if len(rs) > 0 {
		return rs, nil
	}

	originalItem, err := s.getItemBrand(ctx, countryID, itemID)
	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetItemsBySameBrandLog,
			fmt.Sprintf(enums.GetData, "error", "getting original item"))
		return nil, err
	}

	if originalItem.Brand == "" {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetItemsBySameBrandLog,
			fmt.Sprintf(enums.GetData, "error", "brand not found for item"))
		return nil, fmt.Errorf("brand not found for item %s", itemID)
	}

	// Buscar items de la misma marca en Algolia
	rs, err = s.getBrandItemsFromAlgolia(ctx, countryID, originalItem.Brand, itemID)
	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetItemsBySameBrandLog,
			fmt.Sprintf(enums.GetData, "error", "getting brand items from Algolia"))
		return nil, err
	}

	// Guardar en caché
	saveSameBrandInCache(ctx, s.outPortCache, countryID, itemID, rs)

	log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetItemsBySameBrandLog,
		fmt.Sprintf(enums.GetData, "Success", "same brand items retrieved"))
	return rs, nil
}

func (s *sameBrand) getItemBrand(ctx *gin.Context, countryID, itemID string) (sharedModel.ProductInformation, error) {
	items, err := s.outPortCatalogProduct.GetProductsInformationByObjectID(ctx, []string{itemID}, countryID)
	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), getItemBrandLog,
			fmt.Sprintf(enums.GetData, "error", repositoryProxyCatalogProduct))
		return sharedModel.ProductInformation{}, err
	}

	if len(items) == 0 {
		return sharedModel.ProductInformation{}, fmt.Errorf("item not found: %s", itemID)
	}

	return items[0], nil
}

func (s *sameBrand) getBrandItemsFromAlgolia(ctx *gin.Context, countryID, brand, excludeItemID string) ([]model.SameBrandItem, error) {
	var rs []model.SameBrandItem

	query := fmt.Sprintf("brand:\"%s\"", brand)

	customCity := ctx.GetHeader("X-Custom-City")
	if customCity != "" {
		ctx.Header("X-Custom-City", customCity)
	}

	products, err := s.outPortCatalogProduct.GetProductsInformationByQuery(ctx, query, countryID)
	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), getBrandItemsFromAlgoliaLog,
			fmt.Sprintf(enums.GetData, "error", indexCatalogProducts))
		return nil, err
	}

	count := 0
	for _, product := range products {
		if product.ObjectID != excludeItemID && count < maxItemsLimit {
			// Aplicar reglas de negocio: stock, ofertas, etc.
			if s.shouldIncludeProduct(product) {
				rs = append(rs, mappers.MapProductInformationToSameBrandItem(product))
				count++
			}
		}
	}

	return rs, nil
}

func (s *sameBrand) shouldIncludeProduct(product sharedModel.ProductInformation) bool {
	// Implementar reglas de negocio:
	// - Verificar stock
	// - Aplicar reglas de ofertas simples
	// - Aplicar reglas de ofertas prime
	// - Ordenamiento por stock

	// Por ahora, incluir todos los productos que tengan stock
	return product.HasStock == true && product.Status == "A"
}

func findSameBrandInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache,
	countryID, itemID string, response *[]model.SameBrandItem) {
	cacheKey := fmt.Sprintf(keySameBrandCache, countryID, itemID)
	cachedData, err := outPortCache.Get(ctx, cacheKey)

	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetItemsBySameBrandLog,
			fmt.Sprintf(enums.GetData, "Error", repositoryCache))
		return
	}

	if cachedData == "" {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetItemsBySameBrandLog,
			fmt.Sprintf(enums.GetData, "Not found", repositoryCache))
		return
	}

	err = json.Unmarshal([]byte(cachedData), response)
	if err != nil {
		log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), findSameBrandInCacheLog,
			"Error decoding JSON:"+err.Error())
		return
	}

	log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetItemsBySameBrandLog,
		fmt.Sprintf(enums.GetData, "Success", repositoryCache))
}

func saveSameBrandInCache(ctx *gin.Context, outPortCache sharedOutPorts.Cache, countryID, itemID string, rs []model.SameBrandItem) {
	if len(rs) > 0 {
		cacheKey := fmt.Sprintf(keySameBrandCache, countryID, itemID)
		dataToCache, err := json.Marshal(rs)
		if err == nil {
			err := outPortCache.Set(ctx, cacheKey, string(dataToCache), time.Duration(config.Enviroments.RedisSameBrandDepartmentTTL)*time.Minute)
			if err != nil {
				log.Printf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), GetItemsBySameBrandLog,
					"error to save cache")
			}
		}
	}
}
