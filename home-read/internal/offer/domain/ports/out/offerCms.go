package out

//go:generate mockgen -source=offerCms.go -destination=../../../../../test/mocks/offer/domain/ports/out/offer_mock.go

import (
	"ftd-td-home-read-services/internal/offer/domain/model"
	"github.com/gin-gonic/gin"
)

type OfferCmsOutPort interface {
	GetFlashOffer(c *gin.Context, countryId string) ([]model.FlashOffer, error)
}
