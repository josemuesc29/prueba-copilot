package mappers

import (
	"ftd-td-home-read-services/internal/carousel/domain/model"
	"ftd-td-home-read-services/internal/carousel/infra/api/handler/dto/response"
	"reflect"
	"testing"
)

func TestModelSuggestedListToSuggestedDtoList(t *testing.T) {
	input := []model.Suggested{
		{
			ID:               1,
			FirstDescription: "desc",
			OfferText:        "oferta",
			Type:             "tipo",
			UrlImage:         "img",
			OrderingNumber:   1,
			Action:           "action",
			Position:         1,
			Product: model.Product{
				ID:               "prod1",
				MediaImageUrl:    "img-url",
				Description:      "desc",
				FullPrice:        100,
				MediaDescription: "media",
			},
		},
	}

	expected := []response.Suggested{
		{
			ID:               1,
			FirstDescription: "desc",
			OfferText:        "oferta",
			Type:             "tipo",
			UrlImage:         "img",
			OrderingNumber:   1,
			Action:           "action",
			Position:         1,
			Product: response.Product{
				ID:               "prod1",
				MediaImageUrl:    "img-url",
				Description:      "desc",
				FullPrice:        100,
				MediaDescription: "media",
			},
		},
	}

	result := ModelSuggestedListToSuggestedDtoList(input)
	if !reflect.DeepEqual(result, expected) {
		t.Errorf("esperado %+v, obtenido %+v", expected, result)
	}
}
