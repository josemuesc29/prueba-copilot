package request

type SuggestedDto struct {
	CountryID    string `uri:"countryId" binding:"required"`
	StoreGroupID int64  `form:"idStoreGroup"`
}
