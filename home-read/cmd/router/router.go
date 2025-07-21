package router

import (
	bannerGroup "ftd-td-home-read-services/internal/banner/infra/api/groups"
	carouselGroup "ftd-td-home-read-services/internal/carousel/infra/api/groups"
	"ftd-td-home-read-services/internal/health/infra/api/groups"
	offerGroup "ftd-td-home-read-services/internal/offer/infra/api/groups"
	"ftd-td-home-read-services/internal/shared/middleware"
	structureGroup "ftd-td-home-read-services/internal/structure/infra/api/groups"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

const basePath = "/home/r/:countryId/v1"

type Router struct {
	healthGroup     groups.HealthGroup
	strucutureGroup structureGroup.StructureGroup
	offerGroup      offerGroup.Group
	carouselGroup   carouselGroup.Carousel
	bannerGroup     bannerGroup.BannerGroup
}

func NewRouter(
	healthGroup groups.HealthGroup,
	structureGroup structureGroup.StructureGroup,
	offerGroup offerGroup.Group,
	carouselGroup carouselGroup.Carousel,
	bannerGroup bannerGroup.BannerGroup,
) *Router {
	return &Router{
		healthGroup:     healthGroup,
		strucutureGroup: structureGroup,
		offerGroup:      offerGroup,
		carouselGroup:   carouselGroup,
		bannerGroup:     bannerGroup,
	}
}

func SetupRouter(r *Router) *gin.Engine {
	router := gin.Default()

	router.Use(cors.New(configCors()))

	validatedGroup := router.Group(basePath, middleware.ValidateCountryID(), middleware.ValidateSOURCE())

	r.healthGroup.Source(router.Group(basePath))
	r.strucutureGroup.Source(validatedGroup)
	r.offerGroup.Source(validatedGroup)
	r.carouselGroup.Source(validatedGroup)
	r.bannerGroup.Source(validatedGroup)

	return router
}

func configCors() cors.Config {
	config := cors.DefaultConfig()
	config.AllowOrigins = []string{"*"}
	config.AllowMethods = []string{"GET", "POST"}
	config.AllowHeaders = []string{"*"}
	return config
}
