package mapper

import (
	"ftd-td-home-read-services/internal/offer/domain/model"
	sharedModel "ftd-td-home-read-services/internal/shared/domain/model"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestFlashOfferFromProductInformation_Success(t *testing.T) {
	flashOffers := []model.FlashOffer{
		{Id: "1"},
		{Id: "2"},
	}
	products := []sharedModel.ProductInformation{
		{
			ObjectID:          "1",
			MediaImageUrl:     "img1",
			FullPrice:         100,
			MediaDescription:  "desc1",
			LargeDescription:  "large1",
			OfferPrice:        80,
			OfferText:         "oferta1",
			OfferDescription:  "descOferta1",
			PrimePrice:        70,
			PrimeTextDiscount: "prime1",
			PrimeDescription:  "primeDesc1",
		},
		{
			ObjectID:          "2",
			MediaImageUrl:     "img2",
			FullPrice:         200,
			MediaDescription:  "desc2",
			LargeDescription:  "large2",
			OfferPrice:        180,
			OfferText:         "oferta2",
			OfferDescription:  "descOferta2",
			PrimePrice:        170,
			PrimeTextDiscount: "prime2",
			PrimeDescription:  "primeDesc2",
		},
	}

	err := FlashOfferFromProductInformation(&flashOffers, &products)
	assert.NoError(t, err)
	assert.Len(t, flashOffers, 2)
	assert.Equal(t, "img1", flashOffers[0].ImageUrl)
	assert.Equal(t, "img2", flashOffers[1].ImageUrl)
	assert.Equal(t, 100.0, flashOffers[0].FullPrice)
	assert.Equal(t, 200.0, flashOffers[1].FullPrice)
}

func TestFlashOfferFromProductInformation_EmptyInputs(t *testing.T) {
	var flashOffers []model.FlashOffer
	var products []sharedModel.ProductInformation

	err := FlashOfferFromProductInformation(&flashOffers, &products)
	assert.Error(t, err)
}

func TestFlashOfferFromProductInformation_ProductNotFound(t *testing.T) {
	flashOffers := []model.FlashOffer{
		{Id: "1"},
		{Id: "2"},
	}
	products := []sharedModel.ProductInformation{
		{
			ObjectID:      "1",
			MediaImageUrl: "img1",
		},
		// No hay producto con ObjectID "2"
	}

	err := FlashOfferFromProductInformation(&flashOffers, &products)
	assert.NoError(t, err)
	assert.Len(t, flashOffers, 1)
	assert.Equal(t, "1", flashOffers[0].Id)
	assert.Equal(t, "img1", flashOffers[0].ImageUrl)
}
