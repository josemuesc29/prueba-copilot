package mappers

import (
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestMapProductInformationToSameBrandItem(t *testing.T) {
	t.Run("should return same brand item when given a valid product information", func(t *testing.T) {
		// Arrange
		productInfo := &sharedModel.ProductInformation{
			ID:               "123",
			MediaDescription: "Test Product",
			LargeDescription: "This is a test product",
			MediaImageUrl:    "https://example.com/image.jpg",
			FullPrice:        100.0,
			Brand:            "Test Brand",
			HasStock:         true,
			StoresWithStock:  []int{1, 2, 3},
		}

		// Act
		sameBrandItem := MapProductInformationToSameBrandItem(nil, productInfo)

		// Assert
		assert.Equal(t, productInfo.ID, sameBrandItem.ID)
		assert.Equal(t, productInfo.MediaDescription, sameBrandItem.MediaDescription)
		assert.Equal(t, productInfo.LargeDescription, sameBrandItem.LargeDescription)
		assert.Equal(t, productInfo.MediaImageUrl, sameBrandItem.MediaImageUrl)
		assert.Equal(t, productInfo.FullPrice, sameBrandItem.FullPrice)
		assert.Equal(t, productInfo.Brand, sameBrandItem.Brand)
		assert.Equal(t, productInfo.HasStock, sameBrandItem.HasStock)
		assert.Equal(t, len(productInfo.StoresWithStock), sameBrandItem.TotalStock)
	})

	t.Run("should return empty same brand item when given a nil product information", func(t *testing.T) {
		// Act
		sameBrandItem := MapProductInformationToSameBrandItem(nil, nil)

		// Assert
		assert.Equal(t, model.SameBrandItem{}, sameBrandItem)
	})
}
