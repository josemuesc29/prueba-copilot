package request

type SameBrandRequestDto struct {
	CountryID    string `uri:"countryId" binding:"required"`
	ItemID       string `uri:"itemId" binding:"required"`
	City         string `header:"X-Custom-City"`
	Source       string `form:"source"`
	NearbyStores string `form:"nearbyStores"`
	StoreId      string `form:"storeId"`
}
