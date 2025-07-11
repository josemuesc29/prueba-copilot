package groups

//go:generate mockgen -source=detail.go -destination=../../../../../test/mocks/detail/infra/api/groups/detail_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/detail/infra/api/handler"
	"github.com/gin-gonic/gin"
)

type group struct {
	detailHandler handler.Handler
}

type Group interface {
	Source(rg *gin.RouterGroup)
}

func NewDetailGroup(detailHandler handler.Handler) Group {
	return &group{
		detailHandler: detailHandler,
	}
}

func (g *group) Source(rg *gin.RouterGroup) {
	detailGroup := rg.Group("")
	detailGroup.GET("/:id/detail", g.detailHandler.GetProductDetail)
}
