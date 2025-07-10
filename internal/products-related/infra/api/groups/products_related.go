package groups

// TODO: Update mockgen source to products_related.go
//go:generate mockgen -source=products_related.go -destination=../../../../../test/mocks/products-related/infra/api/groups/products_related_mock.go

import (
	// Import for the new handler (already refactored)
	productsRelatedHandler "ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler"
	"ftd-td-catalog-item-read-services/internal/shared/middleware"

	"github.com/gin-gonic/gin"
)

// productsRelatedPath defines the sub-path for related products.
// The full path will be /catalog-item/r/:countryId/v1/items/item/:itemId/related
// as per the main router's basePath and this group's path.
const productsRelatedPath = "/item/:itemId/related" // Changed from /same-brand and adjusted path

type productsRelatedGroup struct {
	handler productsRelatedHandler.ProductsRelatedHandler // Updated handler type
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
	// The group is already under /catalog-item/r/:countryId/v1/items (from cmd/router/router.go)
	// So we just add the specific part for related items.
	group := rg.Group("") // No additional sub-grouping path like sameBrandPath needed here.

	// Path: /catalog-item/r/{countryId}/v1/items/item/{itemId}/related
	// Method: GET
	// The :countryId is handled by the main router and validated by middleware.ValidateCountryID if it's applied to the parent group.
	// We add middleware.ValidateCountryID() here again to be sure, or it could be on rg.Group in cmd/router/router.go
	group.GET(productsRelatedPath, g.handler.GetRelatedItems, middleware.ValidateCountryID())
}
