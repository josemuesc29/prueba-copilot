package request

type TotalStockUriDto struct {
	CountryID string `uri:"countryId" binding:"required"`
	ItemID    string `uri:"itemId" binding:"required"`
}

type TotalStockQueryDto struct {
	StoreIDs  string `form:"storeIds" binding:"required"`
}
