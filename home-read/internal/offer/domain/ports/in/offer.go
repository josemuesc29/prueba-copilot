package in

//go:generate mockgen -source=offer.go -destination=../../../../../test/mocks/offer/domain/ports/in/offer_mock.go

import (
	"ftd-td-home-read-services/internal/offer/domain/model"
	"github.com/gin-gonic/gin"
)

type OfferInPort interface {
	GetFlashOffer(c *gin.Context) ([]model.FlashOffer, error)
}
