package app

import (
	"context"
	"strconv"

	"github.com/fatihtelis/total-stock-app/internal/totalstock/domain/ports/out"
)

type TotalStockService struct {
	stockRepo   out.StockRepository
	algoliaRepo out.AlgoliaRepository
}

func NewTotalStockService(stockRepo out.StockRepository, algoliaRepo out.AlgoliaRepository) *TotalStockService {
	return &TotalStockService{
		stockRepo:   stockRepo,
		algoliaRepo: algoliaRepo,
	}
}

func (s *TotalStockService) GetTotalStock(ctx context.Context, countryID string, itemID string, storeIDs []string) (int, error) {
	itemIDInt, err := strconv.ParseInt(itemID, 10, 64)
	if err != nil {
		return 0, err
	}

	storeIDsInt := make([]int64, len(storeIDs))
	for i, storeID := range storeIDs {
		storeIDInt, err := strconv.ParseInt(storeID, 10, 64)
		if err != nil {
			return 0, err
		}
		storeIDsInt[i] = storeIDInt
	}

	fixedStock, ok, err := s.algoliaRepo.GetFixedStockByItem(ctx, itemIDInt)
	if err != nil {
		return 0, err
	}
	if ok {
		return fixedStock, nil
	}

	maxStock, err := s.stockRepo.GetMaxStockByItem(ctx, itemIDInt)
	if err != nil {
		return 0, err
	}

	totalStock, err := s.stockRepo.GetTotalStockByItemAndStores(ctx, itemIDInt, storeIDsInt)
	if err != nil {
		return 0, err
	}

	if maxStock > 0 && totalStock > maxStock {
		return maxStock, nil
	}

	return totalStock, nil
}
