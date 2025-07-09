package groups

//go:generate mockgen -source=same_brand.go -destination=../../../../../test/mocks/same-brand/infra/api/groups/same_brand_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/same-brand/infra/api/handler"
	"ftd-td-catalog-item-read-services/internal/shared/middleware"

	"github.com/gin-gonic/gin"
)

const sameBrandPath = "/same-brand"

type sameBrand struct {
	sameBrandH handler.SameBrand
}

type SameBrand interface {
	Source(rg *gin.RouterGroup)
}

func NewSameBrand(sameBrandH handler.SameBrand) SameBrand {
	return &sameBrand{
		sameBrandH: sameBrandH,
	}
}

func (g sameBrand) Source(rg *gin.RouterGroup) {
	group := rg.Group(sameBrandPath)

	group.GET("/:countryId/v2/item/:itemId", g.sameBrandH.GetItemsSameBrand, middleware.ValidateCountryID())
}
