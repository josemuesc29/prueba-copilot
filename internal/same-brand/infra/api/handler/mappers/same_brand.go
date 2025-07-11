package mappers

import (
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	"ftd-td-catalog-item-read-services/internal/same-brand/infra/api/handler/dto/response"
	"github.com/jinzhu/copier"
	log "github.com/sirupsen/logrus"
)

func ModelSameBrandItemListToSameBrandItemDtoList(items []model.SameBrandItem) []response.SameBrandResponse {
	var dtoList []response.SameBrandResponse

	err := copier.Copy(&dtoList, &items)
	if err != nil {

		log.Println("Error copying items:", err)
		return []response.SameBrandResponse{}
	}

	return dtoList
}
