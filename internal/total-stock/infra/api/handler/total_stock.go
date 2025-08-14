package handler

import (
	"fmt"
	"strings"
	inPorts "ftd-td-catalog-item-read-services/internal/total-stock/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/total-stock/infra/api/handler/dto/request"
	"ftd-td-catalog-item-read-services/internal/total-stock/infra/api/handler/dto/response"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedResponse "ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
)

const (
	logHandler       = "TotalStockHandler.GetTotalStock"
	logService       = "service TotalStock"
	logBindUriError  = "Error binding URI"
	logBindQueryError= "Error binding query"
	logStoreIdsEmpty = "storeIds query parameter is required and cannot be empty"
)

type TotalStockHandler interface {
	GetTotalStock(c *gin.Context)
}

type totalStockHandler struct {
	service inPorts.TotalStock
}

func NewTotalStockHandler(service inPorts.TotalStock) TotalStockHandler {
	return &totalStockHandler{service: service}
}

func (h *totalStockHandler) GetTotalStock(c *gin.Context) {
	var uriDto request.TotalStockUriDto
	var queryDto request.TotalStockQueryDto
	correlationID := utils.GetCorrelationID(c.Request.Header.Get(enums.HeaderCorrelationID))
	c.Set(enums.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums.HeaderCorrelationID, correlationID)

	log.Printf(enums.LogFormat, correlationID, logHandler, enums.ReadDataFromContext)
	if err := c.ShouldBindUri(&uriDto); err != nil {
		log.Printf(enums.LogFormat, correlationID, logHandler, logBindUriError)
		sharedResponse.BadRequest(c, err.Error())
		return
	}

	if err := c.ShouldBindQuery(&queryDto); err != nil {
		log.Printf(enums.LogFormat, correlationID, logHandler, logBindQueryError)
		sharedResponse.BadRequest(c, err.Error())
		return
	}

	storeIDs := strings.Split(queryDto.StoreIDs, ",")
	if len(storeIDs) == 0 || storeIDs[0] == "" {
		log.Printf(enums.LogFormat, correlationID, logHandler, logStoreIdsEmpty)
		sharedResponse.BadRequest(c, logStoreIdsEmpty)
		return
	}

	log.Printf(enums.LogFormat, correlationID, logHandler, fmt.Sprintf(enums.CallService, logService))
	totalStock, err := h.service.GetTotalStockByItem(c, uriDto.CountryID, uriDto.ItemID, storeIDs)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, logHandler, fmt.Sprintf(enums.GetData, "error", logService))
		sharedResponse.ServerError(c, err.Error())
		return
	}

	log.Printf(enums.LogFormat, correlationID, logHandler, fmt.Sprintf(enums.GetData, "Success", logService))
	sharedResponse.Ok(c, response.TotalStockDto{TotalStock: totalStock})
}
