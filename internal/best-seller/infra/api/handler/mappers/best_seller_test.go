package mappers

import (
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/model"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestModelBestSellerDepartmentListToBestSellerDepartmentDtoList_Success(t *testing.T) {
	input := []model.BestSellerDepartment{
		{
			MediaImageUrl: "https://example.com/image.jpg",
			Description:   "desc",
			FullPrice:     100.0,
			ID:            "id1",
		},
	}
	result := ModelBestSellerDepartmentListToBestSellerDepartmentDtoList(input)

	assert.Len(t, result, 1)
	assert.Equal(t, input[0].MediaImageUrl, result[0].MediaImageUrl)
	assert.Equal(t, input[0].Description, result[0].Description)
	assert.Equal(t, input[0].FullPrice, result[0].FullPrice)
	assert.Equal(t, input[0].ID, result[0].ID)
}

func TestModelBestSellerDepartmentListToBestSellerDepartmentDtoList_EmptyInput(t *testing.T) {
	result := ModelBestSellerDepartmentListToBestSellerDepartmentDtoList([]model.BestSellerDepartment{})
	assert.Empty(t, result)
}
