package in

//go:generate mockgen -source=carousel.go -destination=../../../../../test/mocks/carousel/domain/ports/in/carousel_mock.go

import (
	"ftd-td-home-read-services/internal/carousel/domain/model"
	"github.com/gin-gonic/gin"
)

type Carousel interface {
	GetSuggested(c *gin.Context, countryID string, storeGroupID int64) ([]model.Suggested, error)
}
