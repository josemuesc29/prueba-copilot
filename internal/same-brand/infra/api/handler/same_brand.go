package handler

//go:generate mockgen -source=same_brand.go -destination=../../../../../test/mocks/same-brand/infra/api/handler/same_brand_mock.go

import (
	"fmt"
	inPorts "ftd-td-catalog-item-read-services/internal/same-brand/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/same-brand/infra/api/handler/dto/request"
	"ftd-td-catalog-item-read-services/internal/same-brand/infra/api/handler/mappers"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
)

const (
	getItemsSameBrandLog = "SameBrandHandler.GetItemsSameBrand"
	getItemsSameBrand    = "GetItemsSameBrand"
	serviceSameBrand     = "service SameBrand"
)

type sameBrand struct {
	portSameBrand inPorts.SameBrand
}

type SameBrand interface {
	GetItemsSameBrand(c *gin.Context)
}

func NewSameBrand(portSameBrand inPorts.SameBrand) SameBrand {
	return &sameBrand{portSameBrand: portSameBrand}
}

func (h sameBrand) GetItemsSameBrand(c *gin.Context) {
	var requestDto request.SameBrandRequestDto
	var correlationID = utils.GetCorrelationID(c.Request.Header.Get(enums.HeaderCorrelationID))

	c.Set(enums.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums.HeaderCorrelationID, correlationID)

	log.Printf(enums.LogFormat, correlationID, getItemsSameBrandLog, enums.ReadDataFromContext)
	if err := c.ShouldBindUri(&requestDto); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	if err := c.ShouldBindHeader(&requestDto); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	if err := c.ShouldBindQuery(&requestDto); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	log.Printf(enums.LogFormat, correlationID, getItemsSameBrandLog, fmt.Sprintf(enums.CallService, getItemsSameBrand))
	data, err := h.portSameBrand.GetItemsBySameBrand(c, requestDto.CountryID, requestDto.ItemID, requestDto.Source, requestDto.NearbyStores, requestDto.StoreId, requestDto.City)

	if err != nil {
		log.Printf(enums.LogFormat, correlationID, getItemsSameBrandLog, fmt.Sprintf(enums.GetData, "error", serviceSameBrand))
		response.ServerError(c, err.Error())
		return
	}

	log.Printf(enums.LogFormat, correlationID, getItemsSameBrandLog, fmt.Sprintf(enums.GetData, "Success", serviceSameBrand))
	response.Ok(c, mappers.ModelSameBrandItemListToSameBrandItemDtoList(data))
}
