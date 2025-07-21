package handler

//go:generate mockgen -source=carousel.go -destination=../../../../../test/mocks/carousel/infra/api/handler/carousel_mock.go

import (
	"fmt"
	inPorts "ftd-td-home-read-services/internal/carousel/domain/ports/in"
	"ftd-td-home-read-services/internal/carousel/infra/api/handler/dto/request"
	"ftd-td-home-read-services/internal/carousel/infra/api/handler/mappers"
	enums2 "ftd-td-home-read-services/internal/shared/domain/model/enums"
	"ftd-td-home-read-services/internal/shared/infra/api/handler/dto/response"
	"ftd-td-home-read-services/internal/shared/utils"
	log "github.com/sirupsen/logrus"

	"github.com/gin-gonic/gin"
)

const (
	getSuggestedLog  = "CarouselHandler.GetSuggested"
	getSuggested     = "GetSuggested"
	serviceSuggested = "service suggested"
)

type carousel struct {
	portCarousel inPorts.Carousel
}

type Carousel interface {
	GetSuggested(c *gin.Context)
}

func NewCarousel(port inPorts.Carousel) Carousel {
	return &carousel{portCarousel: port}
}

func (h carousel) GetSuggested(c *gin.Context) {
	var suggestedDto request.SuggestedDto
	var correlationID = utils.GetCorrelationID(c.Request.Header.Get(enums2.HeaderCorrelationID))

	c.Set(enums2.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums2.HeaderCorrelationID, correlationID)

	log.Printf(enums2.LogFormat, correlationID, getSuggestedLog, enums2.ReadDataFromContext)
	if err := c.ShouldBindUri(&suggestedDto); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	if err := c.ShouldBindQuery(&suggestedDto); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	log.Printf(enums2.LogFormat, correlationID, getSuggestedLog, fmt.Sprintf(enums2.CallService, getSuggested))
	data, err := h.portCarousel.GetSuggested(c, suggestedDto.CountryID, suggestedDto.StoreGroupID)

	if err != nil {
		log.Printf(enums2.LogFormat, correlationID, getSuggestedLog, fmt.Sprintf(enums2.GetData, "error", serviceSuggested))
		response.ServerError(c, err.Error())
		return
	}

	log.Printf(enums2.LogFormat, correlationID, getSuggestedLog, fmt.Sprintf(enums2.GetData, "Success", serviceSuggested))
	response.Ok(c, mappers.ModelSuggestedListToSuggestedDtoList(data))
}
