package repository

import (
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/model"
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/ports/out"
	"ftd-td-catalog-item-read-services/internal/best-seller/infra/adapters/database/entities"
	"ftd-td-catalog-item-read-services/internal/best-seller/infra/adapters/database/mapper"
	"gorm.io/gorm"
)

type bestSellerRepository struct {
	db *gorm.DB
}

func NewBestSellerRepository(db *gorm.DB) out.BestSellerOutPort {
	return &bestSellerRepository{
		db: db,
	}
}

func (b bestSellerRepository) GetBestSellerDepartment(countryId string, departmentId string) (*[]model.BestSellerDepartmentEntity, error) {
	var bestSellers []entities.BestSellerEntity

	if err := b.db.
		Where("country_id = ? AND department_id = ?", countryId, departmentId).
		Order("create_date DESC").
		Find(&bestSellers).Error; err != nil {
		return nil, err
	}

	return mapper.GetBestSellerDepartmentByDb(&bestSellers), nil
}
