package mapper

import (
	"ftd-td-home-read-services/internal/offer/domain/model"
	modelCms "ftd-td-home-read-services/internal/offer/infra/adapters/http/cms/model"
)

func GetflashOfferFromCms(flashOfferCms modelCms.FlashOfferCms) []model.FlashOffer {

	if flashOfferCms.Data == nil {
		return nil
	}
	flashOffers := make([]model.FlashOffer, 0, len(flashOfferCms.Data))

	for _, flashOffer := range flashOfferCms.Data {
		flashOffers = append(flashOffers, model.FlashOffer{
			Id:                  flashOffer.Id,
			Type:                flashOffer.Type,
			Position:            flashOffer.Position,
			RedirectUrl:         flashOffer.RedirectUrl,
			StartDate:           flashOffer.StartDate,
			EndDate:             flashOffer.EndDate,
			AvailableStockFlash: flashOffer.AvailableStockFlash,
		})
	}
	return flashOffers
}
