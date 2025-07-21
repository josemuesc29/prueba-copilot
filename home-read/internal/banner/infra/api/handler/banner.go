package handler

//go:generate mockgen -source=banner.go -destination=../../../../../test/mocks/banner/infra/api/handler/banner_mock.go

import (
	"fmt"
	"ftd-td-home-read-services/internal/banner/domain/ports/in"
	"ftd-td-home-read-services/internal/banner/infra/api/handler/mapper"
	enums2 "ftd-td-home-read-services/internal/shared/domain/model/enums"
	"ftd-td-home-read-services/internal/shared/infra/api/handler/dto/response"
	"ftd-td-home-read-services/internal/shared/utils"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
)

const (
	serviceGetMain      = "handler.GetMainBanners"
	serviceGetSecondary = "handler.GetSecondaryBanners"
)

type handler struct {
	bannerInPort in.BannerInPort
}

type Handler interface {
	GetMainBanners(c *gin.Context)
	GetSecondaryBanners(c *gin.Context)
}

func NewBannerHandler(bannerInPort in.BannerInPort) Handler {
	return &handler{
		bannerInPort: bannerInPort,
	}
}

func (h *handler) GetMainBanners(c *gin.Context) {
	var correlationID = utils.GetCorrelationID(c.Request.Header.Get(enums2.HeaderCorrelationID))

	c.Set(enums2.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums2.HeaderCorrelationID, correlationID)
	log.Printf(enums2.LogFormat, correlationID, serviceGetMain, enums2.ReadDataFromContext)

	banners, err := h.bannerInPort.GetMainBanners(c)
	if err != nil {
		log.Printf(enums2.LogFormat, correlationID, serviceGetMain, fmt.Sprintf("Error in GetMainBanners: %v", err))
		response.ConflictError(c, "No fue posible obtener los banners principales")
		return
	}

	if len(banners) == 0 {
		log.Printf(enums2.LogFormat, correlationID, serviceGetMain, "No hay banners principales disponibles")
		response.OK(c, "No hay banners principales disponibles")
		return
	}

	source := c.GetHeader("source")
	bannerId := "MAIN_BANNER"
	response.Ok(c, mapper.MapToBannerResponse(banners, source, bannerId))
}

func (h *handler) GetSecondaryBanners(c *gin.Context) {
	var correlationID = utils.GetCorrelationID(c.Request.Header.Get(enums2.HeaderCorrelationID))

	c.Set(enums2.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums2.HeaderCorrelationID, correlationID)
	log.Printf(enums2.LogFormat, correlationID, serviceGetSecondary, enums2.ReadDataFromContext)

	banners, err := h.bannerInPort.GetSecondaryBanners(c)
	if err != nil {
		log.Printf(enums2.LogFormat, correlationID, serviceGetSecondary, fmt.Sprintf("Error in GetSecondaryBanners: %v", err))
		response.ConflictError(c, "No fue posible obtener los banners secundarios")
		return
	}

	if len(banners) == 0 {
		log.Printf(enums2.LogFormat, correlationID, serviceGetSecondary, "No hay banners secundarios disponibles")
		response.OK(c, "No hay banners secundarios disponibles")
		return
	}

	source := c.GetHeader("SOURCE")
	bannerId := c.Param("bannerId")
	response.Ok(c, mapper.MapToBannerResponse(banners, source, bannerId))
}
