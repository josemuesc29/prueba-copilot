package mapper

import (
	"ftd-td-home-read-services/internal/offer/domain/model"
	modelCms "ftd-td-home-read-services/internal/offer/infra/adapters/http/cms/model"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestGetflashOfferFromCms_EmptyData(t *testing.T) {
	cms := modelCms.FlashOfferCms{Data: []modelCms.FlashOfferData{}}
	result := GetflashOfferFromCms(cms)
	assert.Empty(t, result)
}

func TestGetflashOfferFromCms_SingleElement(t *testing.T) {
	cms := modelCms.FlashOfferCms{
		Data: []modelCms.FlashOfferData{
			{
				Id:                  "1",
				Type:                "type1",
				Position:            2,
				RedirectUrl:         "http://test.com",
				StartDate:           "2024-01-01",
				EndDate:             "2024-01-02",
				AvailableStockFlash: 10,
			},
		},
	}
	result := GetflashOfferFromCms(cms)
	assert.Len(t, result, 1)
	assert.Equal(t, model.FlashOffer{
		Id:                  "1",
		Type:                "type1",
		Position:            2,
		RedirectUrl:         "http://test.com",
		StartDate:           "2024-01-01",
		EndDate:             "2024-01-02",
		AvailableStockFlash: 10,
	}, result[0])
}

func TestGetflashOfferFromCms_MultipleElements(t *testing.T) {
	cms := modelCms.FlashOfferCms{
		Data: []modelCms.FlashOfferData{
			{Id: "1", Type: "A", Position: 1, RedirectUrl: "url1", StartDate: "s1", EndDate: "e1", AvailableStockFlash: 5},
			{Id: "2", Type: "B", Position: 2, RedirectUrl: "url2", StartDate: "s2", EndDate: "e2", AvailableStockFlash: 10},
		},
	}
	result := GetflashOfferFromCms(cms)
	assert.Len(t, result, 2)
	assert.Equal(t, "1", result[0].Id)
	assert.Equal(t, "2", result[1].Id)
	assert.Equal(t, "A", result[0].Type)
	assert.Equal(t, "B", result[1].Type)
}
