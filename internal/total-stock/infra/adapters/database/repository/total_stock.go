package repository

import (
	"ftd-td-catalog-item-read-services/internal/total-stock/domain/ports/out"
	"ftd-td-catalog-item-read-services/internal/total-stock/infra/adapters/database/entities"

	"gorm.io/gorm"
)

type totalStockRepository struct {
	db *gorm.DB
}

func NewTotalStockRepository(db *gorm.DB) out.TotalStockOutPort {
	return &totalStockRepository{
		db: db,
	}
}

func (r *totalStockRepository) GetStockByItemAndStores(countryID, itemID string, storeIDs []string) (int64, error) {
	var totalStock int64

	if err := r.db.Model(&entities.LocationItem{}).
		Where("country_id = ? AND item_id = ? AND location_id IN (?)", countryID, itemID, storeIDs).
		Select("COALESCE(SUM(stock), 0)").
		Row().
		Scan(&totalStock); err != nil {
		return 0, err
	}

	return totalStock, nil
}
