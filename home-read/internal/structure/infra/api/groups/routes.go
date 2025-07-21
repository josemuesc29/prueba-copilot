package groups

//go:generate mockgen -source=routes.go -destination=../../../../../test/mocks/structure/infra/api/groups/structure_mock.go

import (
	handler "ftd-td-home-read-services/internal/structure/infra/api/handler"

	"github.com/gin-gonic/gin"
)

const path = "/structure"

type group struct {
	handler handler.StructureHandler
}

type StructureGroup interface {
	Source(rg *gin.RouterGroup)
}

func NewStructureGroup(handler handler.StructureHandler) StructureGroup {
	return &group{
		handler: handler,
	}
}

func (g group) Source(rg *gin.RouterGroup) {
	router := rg.Group(path)

	router.POST("", g.handler.GetStructure)
}
