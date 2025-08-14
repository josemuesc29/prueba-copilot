package in

import "github.com/gin-gonic/gin"

//go:generate mockgen -source=total_stock.go -destination=../../../../../test/mocks/total-stock/domain/ports/in/total_stock_mock.go
type TotalStock interface {
	GetTotalStockByItem(c *gin.Context, countryID, itemID string, storeIDs []string) (int64, error)
}
