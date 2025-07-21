package groups

//go:generate mockgen -source=carousel.go -destination=../../../../../test/mocks/carousel/infra/api/groups/carousel_mock.go

import (
	carouselH "ftd-td-home-read-services/internal/carousel/infra/api/handler"
	"github.com/gin-gonic/gin"
)

const carouselsPath = "/carousel"

type carousel struct {
	carouselH carouselH.Carousel
}

type Carousel interface {
	Source(rg *gin.RouterGroup)
}

func NewCarousel(carouselH carouselH.Carousel) Carousel {
	return &carousel{
		carouselH: carouselH,
	}
}

func (g carousel) Source(rg *gin.RouterGroup) {
	health := rg.Group(carouselsPath)

	health.GET("/suggest", g.carouselH.GetSuggested)
}
