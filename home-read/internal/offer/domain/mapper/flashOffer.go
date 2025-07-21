package mapper

import (
	"fmt"
	"ftd-td-home-read-services/internal/offer/domain/model"
	sharedModel "ftd-td-home-read-services/internal/shared/domain/model"
)

func FlashOfferFromProductInformation(flashOffers *[]model.FlashOffer, productsInformation *[]sharedModel.ProductInformation) error {
	if *flashOffers == nil || *productsInformation == nil {
		return fmt.Errorf("flashOffers o productsInformation están vacíos")
	}

	productMap := make(map[string]sharedModel.ProductInformation)
	for _, product := range *productsInformation {
		productMap[product.ObjectID] = product
	}

	productsValid := 0
	for _, flashOffer := range *flashOffers {
		if product, exists := productMap[flashOffer.Id]; exists {
			flashOffer.ImageUrl = product.MediaImageUrl
			flashOffer.FullPrice = product.FullPrice
			flashOffer.MediaDescription = product.MediaDescription
			flashOffer.LargeDescription = product.LargeDescription
			flashOffer.OfferPrice = product.OfferPrice
			flashOffer.OfferText = product.OfferText
			flashOffer.OfferDescription = product.OfferDescription
			flashOffer.PrimePrice = product.PrimePrice
			flashOffer.PrimeTextDiscount = product.PrimeTextDiscount
			flashOffer.PrimeDescription = product.PrimeDescription
			(*flashOffers)[productsValid] = flashOffer
			productsValid++
		}
	}

	*flashOffers = (*flashOffers)[:productsValid]
	return nil
}
