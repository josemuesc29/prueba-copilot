package groups

import (
	"ftd-td-catalog-item-read-services/internal/total-stock/infra/api/handler"
	"github.com/gin-gonic/gin"
)

//go:generate mockgen -source=total_stock.go -destination=../../../../../test/mocks/total-stock/infra/api/groups/total_stock_mock.go -package mock_total_stock_groups
type TotalStockGroup struct {
	handler handler.TotalStockHandler
}

func NewTotalStockGroup(h handler.TotalStockHandler) *TotalStockGroup {
	return &TotalStockGroup{handler: h}
}

func (g *TotalStockGroup) RegisterRoutes(group *gin.RouterGroup) {
	group.GET("/r/:countryId/v1/items/total-stock/:itemId", g.handler.GetTotalStock)
}
