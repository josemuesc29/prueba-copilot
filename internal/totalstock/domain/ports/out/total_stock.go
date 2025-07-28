package out

import (
	"context"
)

type StockRepository interface {
	GetTotalStockByItemAndStores(ctx context.Context, itemID int64, storeIDs []int64) (int, error)
	GetMaxStockByItem(ctx context.Context, itemID int64) (int, error)
}

type AlgoliaRepository interface {
	GetFixedStockByItem(ctx context.Context, itemID int64) (int, bool, error)
}
