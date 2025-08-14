package handler

import (
	"fmt"
	"ftd-td-catalog-item-read-services/internal/seo/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
)

const (
	service = "handler.SeoHandler"
)

type handler struct {
	seoInPort in.SeoInPort
}

type Handler interface {
	GetProductSeo(c *gin.Context)
}

func NewSeoHandler(seoInPort in.SeoInPort) Handler {
	return &handler{
		seoInPort: seoInPort,
	}
}

func (h *handler) GetProductSeo(c *gin.Context) {

	var correlationID = utils.GetCorrelationID(c.Request.Header.Get(enums.HeaderCorrelationID))

	c.Set(enums.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums.HeaderCorrelationID, correlationID)
	log.Infof(enums.LogFormat, correlationID, service, enums.ReadDataFromContext)

	seoData, err := h.seoInPort.GetProductSeo(c)
	if err != nil {
		log.Infof(enums.LogFormat, correlationID, service, fmt.Sprintf("Error in GetProductSeo: %v", err))
		response.ConflictError(c, "No fue posible obtener el SEO del producto")
		return
	}

	response.Ok(c, seoData)
}
