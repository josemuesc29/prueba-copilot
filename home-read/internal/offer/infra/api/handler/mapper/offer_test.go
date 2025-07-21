package mapper

import (
	"ftd-td-home-read-services/internal/offer/domain/model"
	"github.com/stretchr/testify/assert"
	"testing"
)

// Test básico de conversión exitosa
func TestGetFlashOfferResponseFromFlashOffer_Success(t *testing.T) {
	flashOffers := []model.FlashOffer{
		{Id: "1", Type: "A", Position: 2, RedirectUrl: "url", StartDate: "2024-01-01", EndDate: "2024-01-02", AvailableStockFlash: 10},
	}
	result := GetFlashOfferResponseFromFlashOffer(flashOffers)
	assert.Len(t, result, 1)
	assert.Equal(t, flashOffers[0].Id, result[0].Id)
	assert.Equal(t, flashOffers[0].AvailableStockFlash, result[0].AvailableStockFlash)
}
