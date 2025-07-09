package mapper

import (
	"ftd-td-catalog-item-read-services/internal/best-seller/infra/adapters/database/entities"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestGetBestSellerDepartmentByDb_Success(t *testing.T) {
	entitiesList := []entities.BestSellerEntity{
		{
			CountryId:        "CO",
			DepartmentId:     "12",
			ItemBestSellerId: "bs-1",
			ItemId:           "item-1",
		},
		{
			CountryId:        "CO",
			DepartmentId:     "13",
			ItemBestSellerId: "bs-2",
			ItemId:           "item-2",
		},
	}
	result := GetBestSellerDepartmentByDb(&entitiesList)

	assert.NotNil(t, result)
	assert.Len(t, *result, 2)
	assert.Equal(t, "CO", (*result)[0].CountryID)
	assert.Equal(t, "12", (*result)[0].DepartmentID)
	assert.Equal(t, "bs-1", (*result)[0].ItemBestSellerID)
	assert.Equal(t, "item-1", (*result)[0].ItemID)
}

func TestGetBestSellerDepartmentByDb_NilInput(t *testing.T) {
	result := GetBestSellerDepartmentByDb(nil)
	assert.Nil(t, result)
}
