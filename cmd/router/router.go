package router

import (
	bestSellerGroup "ftd-td-catalog-item-read-services/internal/best-seller/infra/api/groups"
	healthGroup "ftd-td-catalog-item-read-services/internal/health/infra/api/groups"
	productsRelatedGroup "ftd-td-catalog-item-read-services/internal/products-related/infra/api/groups" // Added products-related group
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

const basePath = "/catalog-item/r/:countryId/v1/items"

type Router struct {
	healthGroup          healthGroup.HealthGroup
	bestSellerGroup      bestSellerGroup.BestSeller
	productsRelatedGroup productsRelatedGroup.ProductsRelatedGroup // Added products-related group
}

func NewRouter(
	healthGroup healthGroup.HealthGroup,
	bestSellerGroup bestSellerGroup.BestSeller,
	productsRelatedGroup productsRelatedGroup.ProductsRelatedGroup, // Added products-related group
) *Router {
	return &Router{
		healthGroup:          healthGroup,
		bestSellerGroup:      bestSellerGroup,
		productsRelatedGroup: productsRelatedGroup, // Added products-related group
	}
}

func SetupRouter(r *Router) *gin.Engine {
	router := gin.Default()

	router.Use(cors.New(configCors()))

	// Main group with basePath
	mainGroup := router.Group(basePath)

	// Register groups under the main group
	r.healthGroup.Source(mainGroup)
	r.bestSellerGroup.Source(mainGroup)
	r.productsRelatedGroup.Source(mainGroup) // Added products-related group

	return router
}

func configCors() cors.Config {
	config := cors.DefaultConfig()
	config.AllowOrigins = []string{"*"}
	config.AllowMethods = []string{"GET", "POST"} // Consider if other methods like OPTIONS are needed
	config.AllowHeaders = []string{"*"}
	return config
}
