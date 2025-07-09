package in

//go:generate mockgen -source=best_seller.go -destination=../../../../../test/mocks/best-seller/domain/ports/in/best_seller_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/model"
	"github.com/gin-gonic/gin"
)

type BestSeller interface {
	GetBestSellerDepartment(c *gin.Context, countryID, departmentID, storeID string) ([]model.BestSellerDepartment, error)
}
