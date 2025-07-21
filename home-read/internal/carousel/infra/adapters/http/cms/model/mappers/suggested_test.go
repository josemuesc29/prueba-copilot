package mappers

import (
	"reflect"
	"testing"

	"ftd-td-home-read-services/internal/carousel/domain/model"
	"ftd-td-home-read-services/internal/carousel/infra/adapters/http/cms/model/response"
)

func TestSuggestedCMSToDomainSuggestedCMS(t *testing.T) {
	cms := response.SuggestedCMS{
		Data: []response.SuggestedData{
			{
				ID:             123,
				Type:           "producto",
				Position:       1,
				OrderingNumber: 10,
				Action:         "ver",
				DocumentID:     "DOC456",
				Sku:            "SKU789",
				CreatedAt:      "2024-06-01T12:00:00Z",
				UpdatedAt:      "2024-06-02T12:00:00Z",
				PublishedAt:    "2024-06-03T12:00:00Z",
				Locale:         "es-CL",
				Name:           "Producto de prueba",
			},
		},
	}

	expected := model.SuggestedCMS{
		Data: []model.SuggestedData{
			{
				ID:             123,
				Type:           "producto",
				Position:       1,
				OrderingNumber: 10,
				Action:         "ver",
				DocumentID:     "DOC456",
				Sku:            "SKU789",
				CreatedAt:      "2024-06-01T12:00:00Z",
				UpdatedAt:      "2024-06-02T12:00:00Z",
				PublishedAt:    "2024-06-03T12:00:00Z",
				Locale:         "es-CL",
				Name:           "Producto de prueba",
			},
		},
	}

	result := SuggestedCMSToDomainSuggestedCMS(cms)

	if !reflect.DeepEqual(result, expected) {
		t.Errorf("esperado %+v, obtenido %+v", expected, result)
	}
}
