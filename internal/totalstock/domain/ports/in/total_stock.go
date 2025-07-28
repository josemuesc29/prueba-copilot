package in

import (
	"context"

	"github.com/gin-gonic/gin"
)

type TotalStockUseCase interface {
	GetTotalStock(ctx context.Context, countryID string, itemID string, storeIDs []string) (int, error)
}

type TotalStockInput interface {
	GetTotalStock(c *gin.Context)
}
