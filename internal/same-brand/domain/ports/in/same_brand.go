package in

//go:generate mockgen -source=same_brand.go -destination=../../../../../test/mocks/same-brand/domain/ports/in/same_brand_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	"github.com/gin-gonic/gin"
)

type SameBrand interface {
	GetItemsBySameBrand(c *gin.Context, countryID, itemID string) ([]model.SameBrandItem, error)
}
