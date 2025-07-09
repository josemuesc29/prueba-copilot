package groups

//go:generate mockgen -source=best_seller.go -destination=../../../../../test/mocks/best-seller/infra/api/groups/best_seller_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/best-seller/infra/api/handler"
	"ftd-td-catalog-item-read-services/internal/shared/middleware"

	"github.com/gin-gonic/gin"
)

const bestSellerPath = "/best-seller"

type bestSeller struct {
	bestSellerH handler.BestSeller
}

type BestSeller interface {
	Source(rg *gin.RouterGroup)
}

func NewBestSeller(bestSellerH handler.BestSeller) BestSeller {
	return &bestSeller{
		bestSellerH: bestSellerH,
	}
}

func (g bestSeller) Source(rg *gin.RouterGroup) {
	health := rg.Group(bestSellerPath)

	health.GET("/department/:departmentId", g.bestSellerH.GetBestSellerDepartment, middleware.ValidateCountryID())
}
