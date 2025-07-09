package mappers

import (
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/catalog_category/model/response"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestMapCatalogCategoryDtoToCatalogCategory(t *testing.T) {
	dto := response.CatalogCategory{
		Code:    "OK",
		Message: "Success",
		Data: response.Category{
			CountryID:              "AR",
			ClassificationID:       4810,
			ClassificationTypeID:   1,
			Name:                   "Salud y medicamentos",
			Image:                  "https://image.url",
			Color:                  "#84d24c",
			SecondColor:            "#ffffff",
			Active:                 true,
			Order:                  1,
			AnywaySelling:          false,
			Division:               0,
			GroupNo:                0,
			Dept:                   0,
			ClassClassification:    0,
			SubclassClassification: 0,
			MetaTitle:              "Salud y medicamentos",
			MetaDescription:        "Salud y medicamentos",
			MenuNavigationType:     "DROPDOWN",
			Path:                   "/salud-medicamentos",
			Redirect:               false,
			HtmlSEO:                "<a>Salud y medicamentos</a>",
		},
	}

	expected := model.CatalogCategory{
		CountryID:              "AR",
		ClassificationID:       4810,
		ClassificationTypeID:   1,
		Name:                   "Salud y medicamentos",
		Image:                  "https://image.url",
		Color:                  "#84d24c",
		SecondColor:            "#ffffff",
		Active:                 true,
		Order:                  1,
		AnywaySelling:          false,
		Division:               0,
		GroupNo:                0,
		Dept:                   0,
		ClassClassification:    0,
		SubclassClassification: 0,
		MetaTitle:              "Salud y medicamentos",
		MetaDescription:        "Salud y medicamentos",
		MenuNavigationType:     "DROPDOWN",
		Path:                   "/salud-medicamentos",
		Redirect:               false,
		HtmlSEO:                "<a>Salud y medicamentos</a>",
	}

	result := MapCatalogCategoryDtoToCatalogCategory(dto)
	assert.Equal(t, expected, result)
}
