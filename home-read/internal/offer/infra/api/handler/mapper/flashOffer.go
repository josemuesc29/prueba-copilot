package mapper

import (
	"ftd-td-home-read-services/internal/offer/domain/model"
	"ftd-td-home-read-services/internal/offer/infra/api/handler/dto/response"
	"github.com/jinzhu/copier"
)

func GetFlashOfferResponseFromFlashOffer(flashOffers []model.FlashOffer) []response.FlashOfferResponse {
	var responseSr []response.FlashOfferResponse

	for _, flashOffer := range flashOffers {
		var flashOfferResponse response.FlashOfferResponse

		er := copier.Copy(&flashOfferResponse, &flashOffer)
		if er != nil {
			flashOfferResponse = response.FlashOfferResponse{}
		}
		responseSr = append(responseSr, flashOfferResponse)
	}
	return responseSr
}
