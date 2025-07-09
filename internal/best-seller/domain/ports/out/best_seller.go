package out

//go:generate mockgen -source=best_seller.go -destination=../../../../../test/mocks/best-seller/domain/ports/out/best_seller_mock.go

import "ftd-td-catalog-item-read-services/internal/best-seller/domain/model"

type BestSellerOutPort interface {
	GetBestSellerDepartment(countryID, departmentID string) (*[]model.BestSellerDepartmentEntity, error)
}
