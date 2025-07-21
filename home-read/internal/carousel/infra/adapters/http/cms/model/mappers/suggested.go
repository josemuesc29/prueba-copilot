package mappers

import (
	"ftd-td-home-read-services/internal/carousel/domain/model"
	"ftd-td-home-read-services/internal/carousel/infra/adapters/http/cms/model/response"
	"github.com/jinzhu/copier"
)

func SuggestedCMSToDomainSuggestedCMS(cmsSuggestedCMS response.SuggestedCMS) model.SuggestedCMS {
	var rs model.SuggestedCMS

	err := copier.Copy(&rs, &cmsSuggestedCMS)
	if err != nil {
		return model.SuggestedCMS{}
	}

	return rs
}
