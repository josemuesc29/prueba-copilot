package groups

//go:generate mockgen -source=banner.go -destination=../../../../../test/mocks/banner/infra/api/groups/banner_mock.go

import (
	"ftd-td-home-read-services/internal/banner/infra/api/handler"
	"ftd-td-home-read-services/internal/banner/infra/middleware"
	"github.com/gin-gonic/gin"
)

const pathGroup = "/banners"

type group struct {
	bannerHandler handler.Handler
}

type BannerGroup interface {
	Source(rg *gin.RouterGroup)
}

func NewBannerGroup(bannerHandler handler.Handler) BannerGroup {
	return &group{
		bannerHandler: bannerHandler,
	}
}

func (og *group) Source(rg *gin.RouterGroup) {
	bannerGroup := rg.Group(pathGroup)
	{
		bannerGroup.GET("/main-banners", og.bannerHandler.GetMainBanners)
		bannerGroup.GET("/secondary-banners/:bannerId", middleware.ValidateIDBanner(), og.bannerHandler.GetSecondaryBanners)
	}
}
