package in

//go:generate mockgen -source=products_related.go -destination=../../../../../test/mocks/products-related/domain/ports/in/products_related_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"github.com/gin-gonic/gin"
)

type ProductsRelated interface {
	GetRelatedProducts(c *gin.Context, countryID, itemID string) ([]model.RelatedProduct, error)
}
