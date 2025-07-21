package mappers

import (
	"ftd-td-home-read-services/internal/carousel/domain/model"
	"ftd-td-home-read-services/internal/carousel/infra/api/handler/dto/response"
	"github.com/jinzhu/copier"
)

func ModelSuggestedListToSuggestedDtoList(suggestedModel []model.Suggested) []response.Suggested {
	var rs []response.Suggested

	for _, sug := range suggestedModel {
		var rsSuggest response.Suggested

		err := copier.Copy(&rsSuggest, &sug)
		if err != nil {
			rsSuggest = response.Suggested{}
		}

		rs = append(rs, rsSuggest)
	}

	return rs
}
