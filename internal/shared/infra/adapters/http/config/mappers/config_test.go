package mappers

import (
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/config/model/response"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestMapConfigDtoToConfigBestSeller(t *testing.T) {
	dto := response.Config{
		Data: response.Data{
			Value: response.Value{
				ObjectID:         "OBJ-123",
				AlgoliaRecommend: true,
				CountItems:       10,
				QueryProducts:    "query-string",
			},
		},
	}

	expected := model.ConfigBestSeller{
		ObjectID:         "OBJ-123",
		AlgoliaRecommend: true,
		CountItems:       10,
		QueryProducts:    "query-string",
	}

	result := MapConfigDtoToConfigBestSeller(dto)

	assert.Equal(t, expected, result)
}
