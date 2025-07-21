package groups

//go:generate mockgen -source=offer.go -destination=../../../../../test/mocks/offer/infra/api/groups/offer_mock.go

import (
	"ftd-td-home-read-services/internal/offer/infra/api/handler"

	"github.com/gin-gonic/gin"
)

const pathGroup = "/offer"

type group struct {
	offerHandler handler.Handler
}

type Group interface {
	Source(rg *gin.RouterGroup)
}

func NewOfferGroup(offerHandler handler.Handler) Group {
	return &group{
		offerHandler: offerHandler,
	}
}

func (og *group) Source(rg *gin.RouterGroup) {
	offerGroup := rg.Group(pathGroup)
	offerGroup.GET("/flash", og.offerHandler.GetFlashOffer)
}
