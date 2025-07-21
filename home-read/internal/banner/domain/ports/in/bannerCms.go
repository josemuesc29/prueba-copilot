package in

//go:generate mockgen -source=bannerCms.go -destination=../../../../../test/mocks/banner/domain/ports/in/banner_mock.go

import (
	"ftd-td-home-read-services/internal/banner/domain/model"
	"github.com/gin-gonic/gin"
)

type BannerInPort interface {
	GetMainBanners(c *gin.Context) ([]model.Banner, error)
	GetSecondaryBanners(c *gin.Context) ([]model.Banner, error)
}
