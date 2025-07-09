package mapper

import (
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/model"
	"ftd-td-catalog-item-read-services/internal/best-seller/infra/adapters/database/entities"
)

func GetBestSellerDepartmentByDb(entities *[]entities.BestSellerEntity) *[]model.BestSellerDepartmentEntity {
	if entities == nil {
		return nil
	}
	bestSellers := make([]model.BestSellerDepartmentEntity, 0, len(*entities))
	for _, entity := range *entities {
		bestSellers = append(bestSellers, model.BestSellerDepartmentEntity{
			CountryID:        entity.CountryId,
			DepartmentID:     entity.DepartmentId,
			ItemBestSellerID: entity.ItemBestSellerId,
			ItemID:           entity.ItemId,
		})
	}
	return &bestSellers
}
