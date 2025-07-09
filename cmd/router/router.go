package router

import (
	bestSellerGroup "ftd-td-catalog-item-read-services/internal/best-seller/infra/api/groups"
	"ftd-td-catalog-item-read-services/internal/health/infra/api/groups"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

const basePath = "/catalog-item/r/:countryId/v1/items"

type Router struct {
	healthGroup     groups.HealthGroup
	bestSellerGroup bestSellerGroup.BestSeller
}

func NewRouter(
	healthGroup groups.HealthGroup,
	bestSellerGroup bestSellerGroup.BestSeller,
) *Router {
	return &Router{
		healthGroup:     healthGroup,
		bestSellerGroup: bestSellerGroup,
	}
}

func SetupRouter(r *Router) *gin.Engine {
	router := gin.Default()

	router.Use(cors.New(configCors()))

	router.Group(basePath)

	r.healthGroup.Source(router.Group(basePath))
	r.bestSellerGroup.Source(router.Group(basePath))

	return router
}

func configCors() cors.Config {
	config := cors.DefaultConfig()
	config.AllowOrigins = []string{"*"}
	config.AllowMethods = []string{"GET", "POST"}
	config.AllowHeaders = []string{"*"}
	return config
}
