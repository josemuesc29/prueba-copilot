package in

//go:generate mockgen -source=products_related.go -destination=../../../../../test/mocks/products-related/domain/products_related_mock.go -package=domain

import (
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"github.com/gin-gonic/gin"
)

type ProductsRelated interface {
	GetRelatedItems(ctx *gin.Context, countryID, itemID string) ([]model.ProductsRelatedItem, error)
}
