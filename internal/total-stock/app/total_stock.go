package app

import (
	"encoding/json"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	inPorts "ftd-td-catalog-item-read-services/internal/total-stock/domain/ports/in"
	outPorts "ftd-td-catalog-item-read-services/internal/total-stock/domain/ports/out"
	sharedOutPorts "ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"sort"
	"strings"
	"time"
)

const (
	logService = "TotalStockService.GetTotalStockByItem"
	logRepo    = "repository total stock"
	logCache   = "repository cache"
	cacheKey   = "total_stock_%s_%s_%s"
)

type totalStockService struct {
	repo  outPorts.TotalStockOutPort
	cache sharedOutPorts.Cache
}

func NewTotalStockService(repo outPorts.TotalStockOutPort, cache sharedOutPorts.Cache) inPorts.TotalStock {
	return &totalStockService{
		repo:  repo,
		cache: cache,
	}
}

func (s *totalStockService) GetTotalStockByItem(c *gin.Context, countryID, itemID string, storeIDs []string) (int64, error) {
	correlationID := c.Value(enums.HeaderCorrelationID)

	// Sort storeIDs to ensure cache key consistency
	sort.Strings(storeIDs)
	cacheKeyFormatted := fmt.Sprintf(cacheKey, countryID, itemID, strings.Join(storeIDs, ","))

	// Try to get data from cache first
	cachedData, err := s.cache.Get(c, cacheKeyFormatted)
	if err == nil && cachedData != "" {
		var totalStock int64
		if err := json.Unmarshal([]byte(cachedData), &totalStock); err == nil {
			log.Printf(enums.LogFormat, correlationID, logService, fmt.Sprintf(enums.GetData, "Success", logCache))
			return totalStock, nil
		}
		log.Printf(enums.LogFormat, correlationID, logService, "Error decoding JSON from cache: "+err.Error())
	} else if err != nil {
		log.Printf(enums.LogFormat, correlationID, logService, fmt.Sprintf(enums.GetData, "Error", logCache))
	} else {
		log.Printf(enums.LogFormat, correlationID, logService, fmt.Sprintf(enums.GetData, "Not found", logCache))
	}

	// If not in cache, get from repository
	totalStock, err := s.repo.GetStockByItemAndStores(countryID, itemID, storeIDs)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, logService, fmt.Sprintf(enums.GetData, "error", logRepo))
		return 0, err
	}

	// Save result to cache
	stockToCache, errMarshal := json.Marshal(totalStock)
	if errMarshal == nil {
		ttl := time.Duration(config.Enviroments.RedisTotalStockTTL) * time.Minute
		errCache := s.cache.Set(c, cacheKeyFormatted, string(stockToCache), ttl)
		if errCache != nil {
			log.Printf(enums.LogFormat, correlationID, logService, "error to save cache")
		}
	} else {
		log.Printf(enums.LogFormat, correlationID, logService, "error marshalling stock for cache")
	}

	log.Printf(enums.LogFormat, correlationID, logService, fmt.Sprintf(enums.GetData, "Success", logRepo))
	return totalStock, nil
}
