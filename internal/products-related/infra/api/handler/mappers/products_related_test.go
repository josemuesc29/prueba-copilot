package mappers

import (
	"testing"

	domainModel "ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"github.com/stretchr/testify/assert"
)

func TestToResponse(t *testing.T) {
	t.Run("should correctly map a slice of domain models to a slice of response DTOs", func(t *testing.T) {
		domainItems := []domainModel.ProductsRelatedItem{
			{
				ID:               "prod123",
				MediaDescription: "Test Media Description 1",
				LargeDescription: "Test Large Description 1",
				MediaImageUrl:    "http://example.com/image1.jpg",
				FullPrice:        99.99,
			},
			{
				ID:               "prod456",
				MediaDescription: "Test Media Description 2",
				LargeDescription: "Test Large Description 2",
				MediaImageUrl:    "http://example.com/image2.jpg",
				FullPrice:        199.99,
			},
		}

		responseDtos := ToResponse(domainItems)

		assert.Len(t, responseDtos, 2)

		assert.Equal(t, domainItems[0].ID, responseDtos[0].ID)
		assert.Equal(t, domainItems[0].MediaDescription, responseDtos[0].MediaDescription)

		assert.Equal(t, domainItems[1].ID, responseDtos[1].ID)
		assert.Equal(t, domainItems[1].FullPrice, responseDtos[1].FullPrice)
	})
}
