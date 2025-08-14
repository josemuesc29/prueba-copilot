package out

//go:generate mockgen -source=catalog_product.go -destination=../../../../../test/mocks/shared/domain/ports/out/catalog_product_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"github.com/gin-gonic/gin"
)

type CatalogProduct interface {
	GetProductInformation(c *gin.Context, productID string) (model.ProductInformation, error)
	GetProductsInformationByObjectID(c *gin.Context, products []string, countryID string) ([]model.ProductInformation, error)
	GetProductsInformationByQuery(c *gin.Context, params string, countryID string) ([]model.ProductInformation, error)
	GetProductsInformationByQueryRelated(c *gin.Context, params string, countryID string, queryDescription string) ([]model.ProductInformation, error)
}
