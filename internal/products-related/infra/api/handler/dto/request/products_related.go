package request

type ProductsRelatedRequestDto struct {
	CountryID string `uri:"countryId" binding:"required"`
	ItemID    string `uri:"itemId" binding:"required"`
}
