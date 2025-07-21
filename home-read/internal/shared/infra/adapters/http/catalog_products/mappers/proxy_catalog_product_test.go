package mappers

import (
	"ftd-td-home-read-services/internal/shared/domain/model"
	"ftd-td-home-read-services/internal/shared/infra/adapters/http/catalog_products/model/response"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestCatalogProductInformationToCarouselProductInformation_NilInput(t *testing.T) {
	result := CatalogProductInformationToCarouselProductInformation(nil)
	assert.Equal(t, model.ProductInformation{}, result)
}

func TestCatalogProductInformationToCarouselProductInformation_CopyFields(t *testing.T) {
	cpInfo := &response.CatalogProductInformation{
		Brand: "Brand",
		ID:    "123",
	}
	expected := model.ProductInformation{
		Brand: "Brand",
		ID:    "123",
	}
	result := CatalogProductInformationToCarouselProductInformation(cpInfo)
	assert.Equal(t, expected.Brand, result.Brand)
	assert.Equal(t, expected.ID, result.ID)
}

func TestCatalogProductsInformationToCarouselProductInformationList(t *testing.T) {
	cpsInfo := response.CatalogProductsInformation{
		Results: []response.CatalogProductInformation{
			{Brand: "brand1", ID: "1"},
			{Brand: "brand2", ID: "2"},
		},
	}
	result := CatalogProductsInformationToCarouselProductInformationList(cpsInfo)
	assert.Len(t, result, 2)
	assert.Equal(t, "brand1", result[0].Brand)
	assert.Equal(t, "2", result[1].ID)
}
