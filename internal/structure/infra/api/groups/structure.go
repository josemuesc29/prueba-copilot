package groups

import (
	"ftd-td-catalog-item-read-services/internal/structure/infra/api/handler"
	"github.com/gin-gonic/gin"
)

type group struct {
	handler handler.ItemStructureHandler
}

type Structure interface {
	Resource(router *gin.RouterGroup)
}

func NewStructureGroup(handler handler.ItemStructureHandler) Structure {
	return &group{
		handler: handler,
	}
}

func (g group) Resource(router *gin.RouterGroup) {
	router.GET("/item-section-structure", g.handler.GetStructure)
}
