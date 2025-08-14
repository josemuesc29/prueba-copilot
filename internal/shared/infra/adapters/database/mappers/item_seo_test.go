package mappers

import (
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/database/entities"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestItemSeoEntityToItemSeo(t *testing.T) {
	entity := entities.ItemSeoEntity{
		CountryID:   "AR",
		ItemID:      123,
		Description: "html",
	}
	result := ItemSeoEntityToItemSeo(entity)
	assert.Equal(t, entity.CountryID, result.CountryID)
	assert.Equal(t, entity.ItemID, result.ItemID)
	assert.Equal(t, entity.Description, result.Description)
}
