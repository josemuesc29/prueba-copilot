package handler

//go:generate mockgen -source=offer.go -destination=../../../../../test/mocks/offer/infra/api/handler/offer_mock.go

import (
	"fmt"
	"ftd-td-home-read-services/internal/offer/domain/ports/in"
	"ftd-td-home-read-services/internal/offer/infra/api/handler/mapper"
	enums2 "ftd-td-home-read-services/internal/shared/domain/model/enums"
	"ftd-td-home-read-services/internal/shared/infra/api/handler/dto/response"
	"ftd-td-home-read-services/internal/shared/utils"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
)

const (
	service = "handler.GetFlashOffer"
)

type handler struct {
	offerInPort in.OfferInPort
}

type Handler interface {
	GetFlashOffer(c *gin.Context)
}

func NewOfferHandler(offerInPort in.OfferInPort) Handler {
	return &handler{
		offerInPort: offerInPort,
	}
}

func (h *handler) GetFlashOffer(c *gin.Context) {
	var correlationID = utils.GetCorrelationID(c.Request.Header.Get(enums2.HeaderCorrelationID))

	c.Set(enums2.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums2.HeaderCorrelationID, correlationID)
	log.Printf(enums2.LogFormat, correlationID, service, enums2.ReadDataFromContext)

	flashOffers, err := h.offerInPort.GetFlashOffer(c)
	if err != nil {
		log.Printf(enums2.LogFormat, correlationID, service, fmt.Sprintf("Error in GetFlashOffer: %v", err))
		response.ConflictError(c, "No fue posible obtener las oferta flash")
		return
	}
	if len(flashOffers) == 0 {
		log.Printf(enums2.LogFormat, correlationID, service, "No hay ofertas flash disponibles")
		response.OK(c, "No hay ofertas flash disponibles")
		return
	}

	response.Ok(c, mapper.GetFlashOfferResponseFromFlashOffer(flashOffers))
}
