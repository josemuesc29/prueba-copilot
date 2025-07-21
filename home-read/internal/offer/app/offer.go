package app

import (
	"encoding/json"
	"fmt"
	"ftd-td-home-read-services/cmd/config"
	"ftd-td-home-read-services/internal/offer/domain/mapper"
	"ftd-td-home-read-services/internal/offer/domain/model"
	"ftd-td-home-read-services/internal/offer/domain/ports/in"
	"ftd-td-home-read-services/internal/offer/domain/ports/out"
	sharedModel "ftd-td-home-read-services/internal/shared/domain/model"
	"ftd-td-home-read-services/internal/shared/domain/model/enums"
	sharedOutPorts "ftd-td-home-read-services/internal/shared/domain/ports/out"
	"ftd-td-home-read-services/internal/shared/infra/api/handler/dto/response"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"time"
)

const (
	keyFlashOffersCache = "flash_offers_%s"
	serviceName         = "app.GetFlashOffer"
)

type offer struct {
	offerCmcOutPort       out.OfferCmsOutPort
	outPortCatalogProduct sharedOutPorts.CatalogProduct
	outPortCache          sharedOutPorts.Cache
}

func NewOfferService(offerCmcOutPort out.OfferCmsOutPort,
	outPortCatalogProduct sharedOutPorts.CatalogProduct,
	outPortCache sharedOutPorts.Cache) in.OfferInPort {
	return &offer{
		offerCmcOutPort:       offerCmcOutPort,
		outPortCatalogProduct: outPortCatalogProduct,
		outPortCache:          outPortCache,
	}
}

func (o *offer) GetFlashOffer(c *gin.Context) ([]model.FlashOffer, error) {
	var flashOffers []model.FlashOffer

	o.findFlashOfferInCache(c, c.Param("countryId"), &flashOffers)
	if len(flashOffers) > 0 {
		return flashOffers, nil
	}

	countryId := c.Param("countryId")
	if countryId == "" {
		response.BadRequest(c, "countryId is required")
	}

	flashOffers, err := o.getFlashOfferCms(c, countryId)
	if err != nil {
		return nil, err
	}

	if len(flashOffers) == 0 {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, "No hay ofertas flash disponibles")
		return nil, nil
	}

	productIds := o.getProductIds(&flashOffers)
	log.Println("Product IDs search catalog products:", productIds)
	products, err := o.getProductInformationByIds(c, productIds, countryId)

	log.Println("Product information search catalog products:", productIds)
	if err != nil {
		return nil, err
	}
	err = mapper.FlashOfferFromProductInformation(&flashOffers, &products)
	if err != nil {
		return nil, err
	}

	o.saveFlashOfferInCache(c, countryId, flashOffers)
	return flashOffers, nil
}

func (o *offer) getProductInformationByIds(c *gin.Context, productIDs []string,
	countryId string) ([]sharedModel.ProductInformation, error) {
	products, err := o.outPortCatalogProduct.GetProductsInformationByObjectID(c, productIDs, countryId)
	return products, err
}

func (o *offer) getFlashOfferCms(c *gin.Context, countryId string) ([]model.FlashOffer, error) {
	flashOffers, err := o.offerCmcOutPort.GetFlashOffer(c, countryId)
	if err != nil {
		return nil, err
	}
	return flashOffers, nil
}

func (o *offer) getProductIds(flashOffers *[]model.FlashOffer) []string {
	var productsId []string
	for _, flashOffer := range *flashOffers {
		productsId = append(productsId, flashOffer.Id)
	}
	return productsId
}

func (o *offer) findFlashOfferInCache(c *gin.Context, countryID string, resp *[]model.FlashOffer) {
	flashOfferCahe, err := o.outPortCache.Get(c, fmt.Sprintf(keyFlashOffersCache, countryID))

	if err != nil {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, fmt.Sprintf("Error getting flash offers from cache: %v", err.Error()))
		return
	} else if flashOfferCahe == "" {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, "Error getting flash offers from cache: Cache is empty")
		return
	} else {
		err = json.Unmarshal([]byte(flashOfferCahe), &resp)
		if err != nil {
			log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, fmt.Sprintf("Error decoding JSON: %v", err.Error()))
			return
		}
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, "Success getting flash offers from cache")
		return
	}
}

func (o *offer) saveFlashOfferInCache(c *gin.Context, countryID string, resp []model.FlashOffer) {
	if len(resp) > 0 {
		flashOffersByte, errMarshal := json.Marshal(resp)
		if errMarshal == nil {
			err := o.outPortCache.Set(c, fmt.Sprintf(keyFlashOffersCache, countryID), string(flashOffersByte),
				time.Duration(config.Enviroments.RedisFlashOffersTTL)*time.Minute)

			if err != nil {
				log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, fmt.Sprintf("Error saving flash offers in cache: %v", err.Error()))
			}
		}
	}
}
