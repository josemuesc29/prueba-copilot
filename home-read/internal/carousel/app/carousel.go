package app

import (
	"encoding/json"
	"fmt"
	"ftd-td-home-read-services/cmd/config"
	"ftd-td-home-read-services/internal/carousel/domain/model"
	"ftd-td-home-read-services/internal/carousel/domain/model/mappers"
	inPorts "ftd-td-home-read-services/internal/carousel/domain/ports/in"
	outPorts "ftd-td-home-read-services/internal/carousel/domain/ports/out"
	sharedModel "ftd-td-home-read-services/internal/shared/domain/model"
	"ftd-td-home-read-services/internal/shared/domain/model/enums"
	sharedOutPorts "ftd-td-home-read-services/internal/shared/domain/ports/out"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"time"
)

const (
	getSuggestedLog               = "CarouselService.GetSuggested"
	executeGetAllProductsInfoLog  = "CarouselService.executeGetAllProductsInfo"
	findSuggestInCacheLog         = "CarouselService.findSuggestInCache"
	repositoryProxyCMS            = "repository proxy cms"
	repositoryCache               = "repository cache"
	repositoryProxyCatalogProduct = "repository proxy catalog product"
	suggestedServices             = "services"
	indexCatalogProducts          = "index catalog products"
	keySuggestedCache             = "suggested_%s"
)

type carousel struct {
	outPortProxyCMS       outPorts.ProxyCMS
	outPortCatalogProduct sharedOutPorts.CatalogProduct
	outPortCache          sharedOutPorts.Cache
}

func NewCarousel(outPortProxyCMS outPorts.ProxyCMS,
	outPortCatalogProduct sharedOutPorts.CatalogProduct, outPortCache sharedOutPorts.Cache) inPorts.Carousel {
	return &carousel{outPortProxyCMS: outPortProxyCMS, outPortCatalogProduct: outPortCatalogProduct, outPortCache: outPortCache}
}

func (t *carousel) GetSuggested(c *gin.Context, countryID string, storeGroupID int64) ([]model.Suggested, error) {
	var rs []model.Suggested

	findSuggestedInCache(c, t.outPortCache, countryID, &rs)
	if len(rs) > 0 {
		return rs, nil
	}

	suggestedCMs, err := t.outPortProxyCMS.GetSuggested(c, countryID)
	if err != nil {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), getSuggestedLog, fmt.Sprintf(enums.GetData, "error", repositoryProxyCMS))
		return nil, err
		
	}

	log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), getSuggestedLog, fmt.Sprintf(enums.GetData, "Success", repositoryProxyCMS))

	executeGetAllProductsInfo(c, suggestedCMs, t.outPortCatalogProduct, countryID, &rs, storeGroupID)
	log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), getSuggestedLog, fmt.Sprintf(enums.GetData, "Success", indexCatalogProducts))

	saveSuggestedInCache(c, t.outPortCache, countryID, rs)
	log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), getSuggestedLog, fmt.Sprintf(enums.GetData, "Success", suggestedServices))
	return rs, nil
}

func executeGetAllProductsInfo(c *gin.Context, suggestedCMs model.SuggestedCMS,
	outPortProduct sharedOutPorts.CatalogProduct, countryID string, response *[]model.Suggested,
	storeGroupID int64) {
	products, err := outPortProduct.GetProductsInformationByObjectID(c,
		getProductsList(suggestedCMs), countryID)

	if err != nil {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), executeGetAllProductsInfoLog,
			fmt.Sprintf(enums.GetData, "error", repositoryProxyCatalogProduct))
		return
	}

	for _, suggetsCMS := range suggestedCMs.Data {
		product := findProductInfo(products, suggetsCMS.Sku)

		if product.ObjectID == suggetsCMS.Sku {
			*response = append(*response, mappers.SuggestedCMSMapToSuggested(suggetsCMS, product, storeGroupID))
		} else {
			log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), executeGetAllProductsInfoLog,
				fmt.Sprintf(enums.NotFound, "Product", suggetsCMS.Sku))
		}
	}
}

func findProductInfo(products []sharedModel.ProductInformation, objectID string) sharedModel.ProductInformation {
	for _, product := range products {
		if product.ObjectID == objectID {
			return product
		}
	}

	return sharedModel.ProductInformation{}
}

func getProductsList(suggestedCMS model.SuggestedCMS) []string {
	var products []string
	for _, suggestedData := range suggestedCMS.Data {
		products = append(products, suggestedData.Sku)
	}
	return products
}

func findSuggestedInCache(c *gin.Context, outPortCache sharedOutPorts.Cache, countryID string, response *[]model.Suggested) {
	suggestedFromCache, err := outPortCache.Get(c, fmt.Sprintf(keySuggestedCache, countryID))

	if err != nil {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), getSuggestedLog, fmt.Sprintf(enums.GetData, "Error", repositoryCache))
		return
	} else if suggestedFromCache == "" {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), getSuggestedLog, fmt.Sprintf(enums.GetData, "Not found", repositoryCache))
		return
	} else {
		err = json.Unmarshal([]byte(suggestedFromCache), &response)
		if err != nil {
			log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), findSuggestInCacheLog, "Error decoding JSON:"+err.Error())
			return
		}
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), getSuggestedLog, fmt.Sprintf(enums.GetData, "Success", repositoryCache))
		return
	}
}

func saveSuggestedInCache(c *gin.Context, outPortCache sharedOutPorts.Cache, countryID string, rs []model.Suggested) {
	if len(rs) > 0 {
		suggestedToCache, errMarshal := json.Marshal(rs)
		if errMarshal == nil {
			err := outPortCache.Set(c, fmt.Sprintf(keySuggestedCache, countryID), string(suggestedToCache),
				time.Duration(config.Enviroments.RedisSuggestedTTL)*time.Minute)

			if err != nil {
				log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), getSuggestedLog, "error to save cache")
			}
		}
	}
}
