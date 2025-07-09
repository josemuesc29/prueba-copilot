package mappers

import (
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/config/model/response"
)

func MapConfigDtoToConfigBestSeller(config response.Config) model.ConfigBestSeller {
	return model.ConfigBestSeller{
		ObjectID:         config.Data.Value.ObjectID,
		AlgoliaRecommend: config.Data.Value.AlgoliaRecommend,
		CountItems:       int(config.Data.Value.CountItems),
		QueryProducts:    config.Data.Value.QueryProducts,
	}
}
