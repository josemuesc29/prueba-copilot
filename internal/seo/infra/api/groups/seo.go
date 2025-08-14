package groups

//go:generate mockgen -source=seo.go -destination=../../../../../test/mocks/seo/infra/api/groups/seo_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/seo/infra/api/handler"
	"github.com/gin-gonic/gin"
)

type group struct {
	seoHandler handler.Handler
}

type Group interface {
	Source(rg *gin.RouterGroup)
}

func NewSeoGroup(seoHandler handler.Handler) Group {
	return &group{
		seoHandler: seoHandler,
	}
}

func (g *group) Source(rg *gin.RouterGroup) {
	detailGroup := rg.Group("")
	detailGroup.GET("/:id/seo", g.seoHandler.GetProductSeo)
}
