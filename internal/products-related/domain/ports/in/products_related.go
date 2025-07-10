package in

//go:generate mockgen -source=products_related.go -destination=../../../../../test/mocks/products-related/domain/ports/in/products_related_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"github.com/gin-gonic/gin"
)

type ProductsRelated interface {
	GetRelatedItems(
		ctx *gin.Context,
		countryID, itemID string,
		nearbyStores string,
		city string,
		queryAlgolia string, // Query for Algolia
		indexName string, // Optional index name
		algoliaParamsStr string, // Other Algolia specific parameters as a string
	) (model.AlgoliaRelatedProductsResponse, error)
}
