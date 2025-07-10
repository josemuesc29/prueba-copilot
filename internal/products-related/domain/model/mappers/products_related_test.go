package mappers

import (
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"github.com/stretchr/testify/assert"
	"testing"
	"time"
)

func TestMapToAlgoliaRelatedProductsResponse(t *testing.T) {
	t.Run("should map ProductInformation hits to AlgoliaRelatedProductsResponse", func(t *testing.T) {
		now := time.Now().Unix()
		hits := []sharedModel.ProductInformation{
			{
				ObjectID:         "123",
				ID:               "123_id",
				MediaDescription: "Test Product 1",
				FullPrice:        100.0,
				HasStock:         true,
				Status:           "A",
				OfferStartDate:   now,
				OfferEndDate:     now + 3600,
			},
			{
				ObjectID:         "456",
				ID:               "456_id",
				MediaDescription: "Test Product 2",
				FullPrice:        200.0,
				HasStock:         true,
				Status:           "A",
			},
		}
		queryParams := "test query"
		page := 0
		hitsPerPage := 10

		expectedNbHits := len(hits)

		result := MapToAlgoliaRelatedProductsResponse(hits, queryParams, page, hitsPerPage)

		assert.NotNil(t, result)
		assert.Len(t, result.Results, 1)

		algoliaResult := result.Results[0]
		assert.Equal(t, queryParams, algoliaResult.Query)
		assert.Equal(t, queryParams, algoliaResult.Params) // As per current mapper logic
		assert.Equal(t, page, algoliaResult.Page)
		assert.Equal(t, hitsPerPage, algoliaResult.HitsPerPage)
		assert.Equal(t, expectedNbHits, algoliaResult.NbHits) // Current mapper sets NbHits to len(hits)
		assert.Len(t, algoliaResult.Hits, expectedNbHits)

		if expectedNbHits > 0 {
			assert.Equal(t, hits[0].ObjectID, algoliaResult.Hits[0].ObjectID)
			assert.Equal(t, hits[0].MediaDescription, algoliaResult.Hits[0].MediaDescription)
			assert.Equal(t, hits[1].FullPrice, algoliaResult.Hits[1].FullPrice)
		}
	})

	t.Run("should return empty hits when input hits are empty", func(t *testing.T) {
		var hits []sharedModel.ProductInformation
		queryParams := "empty"
		page := 0
		hitsPerPage := 5

		result := MapToAlgoliaRelatedProductsResponse(hits, queryParams, page, hitsPerPage)

		assert.NotNil(t, result)
		assert.Len(t, result.Results, 1)
		algoliaResult := result.Results[0]
		assert.Empty(t, algoliaResult.Hits)
		assert.Equal(t, 0, algoliaResult.NbHits)
	})
}

func TestMapProductInformationToRelatedItem(t *testing.T) {
	t.Run("should map basic fields correctly", func(t *testing.T) {
		productInfo := sharedModel.ProductInformation{
			ObjectID:         "789",
			ID:               "789_id",
			MediaDescription: "Mapping Test",
			FullPrice:        150.0,
			HasStock:         true,
			Status:           "A",
			Brand:            "TestBrand",
			Marca:            "TestMarca",
			Sales:            50,
		}
		storeGroupID := int64(101)

		relatedItem := MapProductInformationToRelatedItem(productInfo, storeGroupID)

		assert.Equal(t, productInfo.ObjectID, relatedItem.ObjectID)
		assert.Equal(t, productInfo.MediaDescription, relatedItem.Description)
		assert.Equal(t, productInfo.FullPrice, relatedItem.FullPrice)
		assert.Equal(t, productInfo.Brand, relatedItem.Brand)
		assert.Equal(t, productInfo.Marca, relatedItem.Marca)
		assert.Equal(t, productInfo.Sales, relatedItem.Sales)
		assert.Equal(t, storeGroupID, relatedItem.IdStoreGroup)
		assert.Equal(t, !productInfo.HasStock, relatedItem.WithoutStock)
	})
}
