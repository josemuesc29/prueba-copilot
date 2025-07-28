package groups

//go:generate mockgen -source=structure.go -destination=../../../../../test/mocks/structure/infra/api/groups/structure_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/shared/middleware"
	"ftd-td-catalog-item-read-services/internal/structure/infra/api/handler"
	"github.com/gin-gonic/gin"
)

const structurePath = "/item-section-structure"

type structure struct {
	structureH handler.ItemStructureHandler
}

type Structure interface {
	Source(router *gin.RouterGroup)
}

func NewStructureGroup(structureH handler.ItemStructureHandler) Structure {
	return &structure{
		structureH: structureH,
	}
}

func (g structure) Source(rg *gin.RouterGroup) {
	group := rg.Group(structurePath)

	group.GET("/:itemId", g.structureH.GetStructure, middleware.ValidateCountryID())
}
