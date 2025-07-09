package entities

import (
	"github.com/stretchr/testify/assert"
	"testing"
	"time"
)

func TestBestSellerEntity_TableName(t *testing.T) {
	entity := BestSellerEntity{}
	assert.Equal(t, "catalog_item.best_sellers", entity.TableName())
}

func TestBestSellerEntity_Fields(t *testing.T) {
	now := time.Now()
	entity := BestSellerEntity{
		CountryId:        "CO",
		ItemBestSellerId: "bs-1",
		DepartmentId:     "12",
		ItemId:           "item-1",
		CreateDate:       now,
		UpdateDate:       now,
	}

	assert.Equal(t, "CO", entity.CountryId)
	assert.Equal(t, "bs-1", entity.ItemBestSellerId)
	assert.Equal(t, "12", entity.DepartmentId)
	assert.Equal(t, "item-1", entity.ItemId)
	assert.Equal(t, now, entity.CreateDate)
	assert.Equal(t, now, entity.UpdateDate)
}
