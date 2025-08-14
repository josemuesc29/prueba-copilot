package out

//go:generate mockgen -source=total_stock.go -destination=../../../../../test/mocks/total-stock/domain/ports/out/total_stock_mock.go
type TotalStockOutPort interface {
	GetStockByItemAndStores(countryID, itemID string, storeIDs []string) (int64, error)
}
