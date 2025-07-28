package router

import (
	bestSellerGroup "ftd-td-catalog-item-read-services/internal/best-seller/infra/api/groups"
	itemDetailGroup "ftd-td-catalog-item-read-services/internal/detail/infra/api/groups"
	"ftd-td-catalog-item-read-services/internal/health/infra/api/groups"
	productsRelatedGroup "ftd-td-catalog-item-read-services/internal/products-related/infra/api/groups"
	sameBrandGroup "ftd-td-catalog-item-read-services/internal/same-brand/infra/api/groups"
	structureGroup "ftd-td-catalog-item-read-services/internal/structure/infra/api/groups"
	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"
)

const basePath = "/catalog-item/r/:countryId/v1/items"

type Router struct {
	healthGroup          groups.HealthGroup
	bestSellerGroup      bestSellerGroup.BestSeller
	sameBrandGroup       sameBrandGroup.SameBrand
	productsRelatedGroup productsRelatedGroup.ProductsRelatedGroup
	itemDetailGroup      itemDetailGroup.Group
	structureGroup       structureGroup.Structure
}

func NewRouter(
	healthGroup groups.HealthGroup,
	bestSellerGroup bestSellerGroup.BestSeller,
	sameBrandGroup sameBrandGroup.SameBrand,
	productsRelatedGroup productsRelatedGroup.ProductsRelatedGroup,
	itemDetailGroup itemDetailGroup.Group,
	structureGroup structureGroup.Structure,
) *Router {
	return &Router{
		healthGroup:          healthGroup,
		bestSellerGroup:      bestSellerGroup,
		sameBrandGroup:       sameBrandGroup,
		productsRelatedGroup: productsRelatedGroup,
		itemDetailGroup:      itemDetailGroup,
		structureGroup:       structureGroup,
	}
}

func SetupRouter(r *Router) *gin.Engine {
	router := gin.Default()

	router.Use(cors.New(configCors()))

	router.Group(basePath)

	r.healthGroup.Source(router.Group(basePath))
	r.bestSellerGroup.Source(router.Group(basePath))
	r.sameBrandGroup.Source(router.Group(basePath))
	r.productsRelatedGroup.Source(router.Group(basePath))
	r.itemDetailGroup.Source(router.Group(basePath))
	r.structureGroup.Source(router.Group(basePath))
	return router
}

func configCors() cors.Config {
	config := cors.DefaultConfig()
	config.AllowOrigins = []string{"*"}
	config.AllowMethods = []string{"GET", "POST"}
	config.AllowHeaders = []string{"*"}
	return config
}
