package app

import (
	"errors"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/seo/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/seo/infra/api/handler/dto/request"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedOutPorts "ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"strconv"
	"time"
)

const (
	keySeoInCache = "item_seo_%s_%s"
	service       = "app.productseo"
	format        = "service: %s, correlationId: %s -> %s"
)

type productseo struct {
	ItemSeoRepository sharedOutPorts.ItemSeo
	outPortCache      sharedOutPorts.Cache
}

func NewProductSeo(ItemSeoRepository sharedOutPorts.ItemSeo, outPortCache sharedOutPorts.Cache) in.SeoInPort {
	return &productseo{
		ItemSeoRepository: ItemSeoRepository,
		outPortCache:      outPortCache,
	}
}

func (p *productseo) GetProductSeo(c *gin.Context) (string, error) {
	seoInCache := p.findSeoInCache(c, c.Param("countryId"), c.Param("id"))

	if seoInCache != "" {
		return seoInCache, nil
	}

	countryId := c.Param("countryId")
	itemIdStr := c.Param("id")

	var headers request.SeoHeaders

	if err := c.ShouldBindHeader(&headers); err != nil {
		log.Infof(format, service, c.Value(enums.HeaderCorrelationID), err.Error())
		return "", err
	}

	if countryId == "" {
		log.Infof(format, service, c.Value(enums.HeaderCorrelationID), "countryId is required")
		return "", errors.New("countryId is required")
	}

	if itemIdStr == "" {
		log.Infof(format, service, c.Value(enums.HeaderCorrelationID), "itemId is required")
		return "", errors.New("itemId is required")
	}

	itemId, err := strconv.ParseInt(itemIdStr, 10, 64)
	if err != nil {
		return "", err
	}

	itemSeo, err := p.ItemSeoRepository.GetItemSeo(itemId, countryId)
	if err != nil {
		return "", err
	}
	if itemSeo.ItemID == 0 {
		log.Infof(format, service, c.Value(enums.HeaderCorrelationID), "itemSeo not found")
		return "", errors.New("itemSeo not found")
	}
	seoText := p.getTextSeo(c, headers, itemSeo)
	p.saveSeoInCache(c, countryId, itemIdStr, seoText)

	return seoText, nil
}

func (p *productseo) getTextSeo(c *gin.Context, headers request.SeoHeaders, itemSeo *model.ItemSeo) string {
	PlatformType := enums.GetPlatformType(headers.Source)
	if PlatformType == enums.Desktop {
		log.Infof(format, service, c.Value(enums.HeaderCorrelationID), "PlatformType is Desktop")
		return *itemSeo.TextSeoWeb
	} else {
		log.Infof(format, service, c.Value(enums.HeaderCorrelationID), "PlatformType is Mobile")
		return *itemSeo.TextSeoApp
	}
}

func (p *productseo) findSeoInCache(c *gin.Context, countryID string, itemID string) string {
	seoInCache, err := p.outPortCache.Get(c.Request.Context(), fmt.Sprintf(keySeoInCache, countryID, itemID))

	if err != nil {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), service, fmt.Sprintf("Error getting SEO from cache: %v", err.Error()))
		return ""
	} else if seoInCache == "" {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), service, "Error getting SEO from cache: Cache is empty")
		return ""
	} else {
		return seoInCache
	}
}

func (p *productseo) saveSeoInCache(c *gin.Context, countryID string, itemID string, resp string) {

	if resp == "" {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), service, "No SEO text to save in cache")
		return
	}

	err := p.outPortCache.Set(c, fmt.Sprintf(keySeoInCache, countryID, itemID), resp,
		time.Duration(config.Enviroments.RedisItemDetailTTL)*time.Minute)

	if err != nil {
		log.Printf(enums.LogFormat, c.Value(enums.HeaderCorrelationID), service, fmt.Sprintf("Error saving flash offers in cache: %v", err.Error()))
	}
}
