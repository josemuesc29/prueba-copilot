package mappers

import (
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/database/entities"
	"github.com/jinzhu/copier"
)

func ItemSeoEntityToItemSeo(entity entities.ItemSeoEntity) *model.ItemSeo {
	var itemSeo model.ItemSeo

	err := copier.Copy(&itemSeo, &entity)
	if err != nil {
		itemSeo = model.ItemSeo{}
	}

	return &itemSeo
}
