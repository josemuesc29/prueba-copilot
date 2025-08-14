package handler

//go:generate mockgen -source=best_seller.go -destination=../../../../../test/mocks/best-seller/infra/api/handler/best_seller_mock.go

import (
	"fmt"
	inPorts "ftd-td-catalog-item-read-services/internal/best-seller/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/best-seller/infra/api/handler/dto/request"
	"ftd-td-catalog-item-read-services/internal/best-seller/infra/api/handler/mappers"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	log "github.com/sirupsen/logrus"

	"github.com/gin-gonic/gin"
)

const (
	getBestSellerDepartmentLog = "BestSellerHandler.GetBestSellerDepartment"
	getBestSellerDepartment    = "GetBestSellerDepartment"
	serviceBestSeller          = "service BestSeller"
)

type bestSeller struct {
	portBestSeller inPorts.BestSeller
}

type BestSeller interface {
	GetBestSellerDepartment(c *gin.Context)
}

func NewBestSeller(portBestSeller inPorts.BestSeller) BestSeller {
	return &bestSeller{portBestSeller: portBestSeller}
}

func (h bestSeller) GetBestSellerDepartment(c *gin.Context) {
	var requestDto request.DepartmentBestSellerDto
	var correlationID = utils.GetCorrelationID(c.Request.Header.Get(enums.HeaderCorrelationID))

	c.Set(enums.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums.HeaderCorrelationID, correlationID)

	log.Printf(enums.LogFormat, correlationID, getBestSellerDepartmentLog, enums.ReadDataFromContext)
	if err := c.ShouldBindUri(&requestDto); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	if err := c.ShouldBindQuery(&requestDto); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	if err := c.ShouldBindHeader(&requestDto); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	log.Printf(enums.LogFormat, correlationID, getBestSellerDepartmentLog, fmt.Sprintf(enums.CallService, getBestSellerDepartment))
	data, err := h.portBestSeller.GetBestSellerDepartment(c, requestDto.CountryID, requestDto.DepartmentID,
		requestDto.StoreID)

	if err != nil {
		log.Printf(enums.LogFormat, correlationID, getBestSellerDepartmentLog, fmt.Sprintf(enums.GetData, "error", serviceBestSeller))
		response.ConflictError(c, err.Error())
		return
	}

	log.Printf(enums.LogFormat, correlationID, getBestSellerDepartmentLog, fmt.Sprintf(enums.GetData, "Success", serviceBestSeller))
	response.Ok(c, mappers.ModelBestSellerDepartmentListToBestSellerDepartmentDtoList(data))
}
