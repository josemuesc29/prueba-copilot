package groups

import (
	handler "ftd-td-home-read-services/internal/structure/infra/api/handler"

	"github.com/gin-gonic/gin"
)

const itemSectionPath = "/catalog-item/r/:countryId/v1/items"

type itemSectionGroup struct {
	handler handler.ItemSectionHandler
}

type ItemSectionGroup interface {
	Source(rg *gin.RouterGroup)
}

func NewItemSectionGroup(handler handler.ItemSectionHandler) ItemSectionGroup {
	return &itemSectionGroup{
		handler: handler,
	}
}

func (g itemSectionGroup) Source(rg *gin.RouterGroup) {
	router := rg.Group(itemSectionPath)

	router.GET("/:itemId/item-section-structure", g.handler.GetItemSectionStructure)
}
