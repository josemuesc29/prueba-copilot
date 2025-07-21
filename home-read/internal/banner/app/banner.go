package app

import (
	"encoding/json"
	"fmt"
	"ftd-td-home-read-services/cmd/config"
	"ftd-td-home-read-services/internal/banner/domain/model"
	"ftd-td-home-read-services/internal/banner/domain/ports/in"
	"ftd-td-home-read-services/internal/banner/domain/ports/out"
	enums2 "ftd-td-home-read-services/internal/shared/domain/model/enums"
	sharedOutPorts "ftd-td-home-read-services/internal/shared/domain/ports/out"
	"ftd-td-home-read-services/internal/shared/infra/api/handler/dto/response"
	log "github.com/sirupsen/logrus"
	"sort"
	"strings"
	"time"

	"github.com/gin-gonic/gin"
)

const (
	getBannerLog            = "BannerService.GetBanner"
	findBannerInCacheLog    = "BannerService.findBannerInCache"
	repositoryProxyCMS      = "repository proxy cms"
	repositoryCache         = "repository cache"
	BannerServices          = "services"
	keyBannerMainCache      = "banner_main_%s"
	keyBannerSecondaryCache = "banner_secondary_%s"
	categorySecondaryBanner = "secondary"
	categoryMainBanner      = "main"
)

type banner struct {
	bannerCmsOutPort out.BannerCmsOutPort
	outPortCache     sharedOutPorts.Cache
}

func NewBannerService(bannerCmsOutPort out.BannerCmsOutPort, outPortCache sharedOutPorts.Cache) in.BannerInPort {
	return &banner{
		bannerCmsOutPort: bannerCmsOutPort,
		outPortCache:     outPortCache,
	}
}

func (b *banner) GetMainBanners(c *gin.Context) ([]model.Banner, error) {
	var rs []model.Banner

	b.findBannerInCache(c, c.Param("countryId"), keyBannerMainCache, &rs, c.GetHeader("SOURCE"), "MAIN_BANNER")
	if len(rs) > 0 {
		return rs, nil
	}

	countryId := c.Param("countryId")
	if countryId == "" {
		response.BadRequest(c, "countryId is required")
	}

	banners, err := b.bannerCmsOutPort.GetBanners(c, countryId)
	if err != nil {
		log.Printf(enums2.LogFormat, c.Value(enums2.HeaderCorrelationID), getBannerLog, fmt.Sprintf(enums2.GetData, "error", repositoryProxyCMS))
		return nil, err
	}

	log.Printf(enums2.LogFormat, c.Value(enums2.HeaderCorrelationID), getBannerLog, fmt.Sprintf(enums2.GetData, "Success", repositoryProxyCMS))

	mainBanners := filterAndSortBannersByCategory(banners, categoryMainBanner)

	rs = mainBanners

	b.saveBannerInCache(c, countryId, keyBannerMainCache, rs, c.GetHeader("SOURCE"), "MAIN_BANNER")
	log.Printf(enums2.LogFormat, c.Value(enums2.HeaderCorrelationID), getBannerLog, fmt.Sprintf(enums2.GetData, "Success", BannerServices))
	return rs, nil

}

func (b *banner) GetSecondaryBanners(c *gin.Context) ([]model.Banner, error) {
	var rs []model.Banner

	b.findBannerInCache(c, c.Param("countryId"), keyBannerSecondaryCache, &rs, c.GetHeader("SOURCE"), c.Param("bannerId"))
	if len(rs) > 0 {
		return rs, nil
	}

	countryId := c.Param("countryId")
	if countryId == "" {
		response.BadRequest(c, "countryId is required")
	}

	banners, err := b.bannerCmsOutPort.GetBanners(c, countryId)
	if err != nil {
		return nil, err
	}

	secondaryBanners := filterAndSortBannersByCategory(banners, categorySecondaryBanner)

	rs = secondaryBanners

	b.saveBannerInCache(c, countryId, keyBannerSecondaryCache, rs, c.GetHeader("SOURCE"), c.Param("BannerId"))
	log.Printf(enums2.LogFormat, c.Value(enums2.HeaderCorrelationID), getBannerLog, fmt.Sprintf(enums2.GetData, "Success", BannerServices))
	return rs, nil
}

func filterAndSortBannersByCategory(banners []model.Banner, category string) []model.Banner {
	var filtered []model.Banner
	for _, b := range banners {
		if strings.EqualFold(b.Category, category) {
			filtered = append(filtered, b)
		}
	}

	// Ordenar por Position ascendente, los que no tienen Position (0) van al final
	sort.SliceStable(filtered, func(i, j int) bool {
		if filtered[i].Position == 0 && filtered[j].Position != 0 {
			return false
		}
		if filtered[i].Position != 0 && filtered[j].Position == 0 {
			return true
		}
		return filtered[i].Position < filtered[j].Position
	})

	return filtered
}

func (b *banner) findBannerInCache(c *gin.Context, countryID string, keyCache string, response *[]model.Banner, source string, bannerID string) {
	cacheKey := fmt.Sprintf(keyCache, countryID) + "_" + source + "_" + bannerID
	bannerFromCache, err := b.outPortCache.Get(c, cacheKey)

	if err != nil {
		log.Printf(enums2.LogFormat, c.Value(enums2.HeaderCorrelationID), getBannerLog, fmt.Sprintf(enums2.GetData, "Error", repositoryCache))
		return
	} else if bannerFromCache == "" {
		log.Printf(enums2.LogFormat, c.Value(enums2.HeaderCorrelationID), getBannerLog, fmt.Sprintf(enums2.GetData, "Not found", repositoryCache))
		return
	} else {
		err = json.Unmarshal([]byte(bannerFromCache), response)
		if err != nil {
			log.Printf(enums2.LogFormat, c.Value(enums2.HeaderCorrelationID), findBannerInCacheLog, "Error decoding JSON:"+err.Error())
			return
		}
		log.Printf(enums2.LogFormat, c.Value(enums2.HeaderCorrelationID), getBannerLog, fmt.Sprintf(enums2.GetData, "Success", repositoryCache))
		return
	}
}

func (b *banner) saveBannerInCache(c *gin.Context, countryID string, keyCache string, rs []model.Banner, source string, bannerID string) {
	if len(rs) > 0 {
		bannerToCache, errMarshal := json.Marshal(rs)
		if errMarshal == nil {
			cacheKey := fmt.Sprintf(keyCache, countryID) + "_" + source + "_" + bannerID
			err := b.outPortCache.Set(c, cacheKey, string(bannerToCache),
				time.Duration(config.Enviroments.RedisBannersTTL)*time.Minute)

			if err != nil {
				log.Printf(enums2.LogFormat, c.Value(enums2.HeaderCorrelationID), getBannerLog, "error to save cache")
			}
		}
	}
}
