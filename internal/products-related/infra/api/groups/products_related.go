package groups

//go:generate mockgen -source=products_related.go -destination=../../../../../test/mocks/products-related/infra/api/groups/products_related_mock.go

import (
	productsRelatedHandler "ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler"
	"ftd-td-catalog-item-read-services/internal/shared/middleware"
	"github.com/gin-gonic/gin"
)

const productsRelatedPath = "products-related"

type productsRelatedGroup struct {
	handler productsRelatedHandler.ProductsRelatedHandler
}

type ProductsRelatedGroup interface {
	Source(rg *gin.RouterGroup)
}

func NewProductsRelatedGroup(h productsRelatedHandler.ProductsRelatedHandler) ProductsRelatedGroup { // Updated constructor and param type
	return &productsRelatedGroup{
		handler: h,
	}
}

func (g *productsRelatedGroup) Source(rg *gin.RouterGroup) {

	group := rg.Group(productsRelatedPath)
	group.GET("/:countryId/v2/item/:itemId", g.handler.GetRelatedItems, middleware.ValidateCountryID())
}
