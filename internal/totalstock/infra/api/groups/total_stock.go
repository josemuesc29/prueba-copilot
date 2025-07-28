package groups

import (
	"github.com/fatihtelis/total-stock-app/internal/totalstock/domain/ports/in"
	"github.com/gin-gonic/gin"
)

func TotalStockRoutes(router *gin.RouterGroup, handler in.TotalStockInput) {
	router.GET("/catalog-item/r/:countryId/v1/items/total-stock/:itemId", handler.GetTotalStock)
}
