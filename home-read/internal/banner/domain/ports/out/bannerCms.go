package out

//go:generate mockgen -source=bannerCms.go -destination=../../../../../test/mocks/banner/domain/ports/out/banner_mock.go

import (
	"ftd-td-home-read-services/internal/banner/domain/model"
	"github.com/gin-gonic/gin"
)

type BannerCmsOutPort interface {
	GetBanners(c *gin.Context, countryId string) ([]model.Banner, error)
}
