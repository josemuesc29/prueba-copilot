package app

import (
	"encoding/json"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/detail/domain/mapper"
	"ftd-td-catalog-item-read-services/internal/detail/domain/model"
	"ftd-td-catalog-item-read-services/internal/detail/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedOutPorts "ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"time"
)

const (
	keyItemDetailInCache = "item_detail_%s_%s"
	serviceName          = "app.GetItemDetail"
)

type itemDetail struct {
	outPortCache          sharedOutPorts.Cache
	outPortCatalogProduct sharedOutPorts.CatalogProduct
}

func NewItemDetail(outPortCache sharedOutPorts.Cache, outPortCatalogProduct sharedOutPorts.CatalogProduct) in.DetailInPort {
	return &itemDetail{
		outPortCache:          outPortCache,
		outPortCatalogProduct: outPortCatalogProduct,
	}
}

func (i *itemDetail) GetDetailProduct(c *gin.Context) (model.ItemDetail, error) {
	var itemDetail model.ItemDetail

	countryId := c.Param("countryId")
	if countryId == "" {
		return model.ItemDetail{}, fmt.Errorf("countryId is required")
	}

	itemId := c.Param("id")
	if itemId == "" {
		return model.ItemDetail{}, fmt.Errorf("itemId is required")
	}

	i.findItemDetailInCache(c, countryId, itemId, &itemDetail)
	if itemDetail.ID != "" {
		return itemDetail, nil
	}

	productInformation, err := i.outPortCatalogProduct.GetProductInformation(c, itemId)
	if err != nil {
		return model.ItemDetail{}, err
	}

	if productInformation.ID == "" {
		return model.ItemDetail{}, fmt.Errorf("item detail not found for itemId: %s", itemId)
	}

	err = mapper.ItemDetailFromProductInformation(&itemDetail, &productInformation)

	if err != nil {
		return model.ItemDetail{}, fmt.Errorf("error al mapear los datos del producto: %w", err)
	}

	i.saveItemDetailInCache(c, countryId, itemId, &itemDetail)
	return itemDetail, nil
}

func (i *itemDetail) findItemDetailInCache(c *gin.Context, countryID string, itemId string, resp *model.ItemDetail) {
	flashOfferCache, err := i.outPortCache.Get(c.Request.Context(), fmt.Sprintf(keyItemDetailInCache, countryID, itemId))

	if err != nil {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, fmt.Sprintf("Error getting item detail from cache: %v", err.Error()))
		return
	} else if flashOfferCache == "" {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, "Error getting item detail from cache: Cache is empty")
		return
	} else {
		err = json.Unmarshal([]byte(flashOfferCache), &resp)
		if err != nil {
			log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, fmt.Sprintf("Error decoding JSON: %v", err.Error()))
			return
		}
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, "Success getting item detail from cache")
		return
	}
}

func (i *itemDetail) saveItemDetailInCache(c *gin.Context, countryID string, itemId string, resp *model.ItemDetail) {

	if resp == nil {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, "No item detail to save in cache")
		return
	}

	respByte, errMarshal := json.Marshal(resp)
	if errMarshal != nil {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName,
			fmt.Sprintf("Error marshaling flash offers for cache: %v", errMarshal))
		return
	} else {
		err := i.outPortCache.Set(c, fmt.Sprintf(keyItemDetailInCache, countryID, itemId), string(respByte),
			time.Duration(config.Enviroments.RedisTextSeoTTL)*time.Minute)

		if err != nil {
			log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), serviceName, fmt.Sprintf("Error saving flash offers in cache: %v", err.Error()))
		}
	}
}
