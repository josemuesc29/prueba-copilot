package repository

import (
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/database/entities"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/database/mappers"
	"gorm.io/gorm"
)

const (
	filterItemIDAndCountryID = "country_id = ? AND item_id = ?"
)

type itemSeoRepository struct {
	db *gorm.DB
}

func NewItemSeoRepository(db *gorm.DB) out.ItemSeo {
	return &itemSeoRepository{
		db: db,
	}
}

func (i itemSeoRepository) GetItemSeo(itemID int64, countryID string) (*model.ItemSeo, error) {
	var itemSeoEntity entities.ItemSeoEntity

	if err := i.db.
		Where(filterItemIDAndCountryID, countryID, itemID).
		Find(&itemSeoEntity).Error; err != nil {
		return &model.ItemSeo{}, err
	}

	return mappers.ItemSeoEntityToItemSeo(itemSeoEntity), nil
}
