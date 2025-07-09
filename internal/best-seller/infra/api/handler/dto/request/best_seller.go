package request

type DepartmentBestSellerDto struct {
	CountryID    string `uri:"countryId"`
	DepartmentID string `uri:"departmentId" binding:"required"`
	StoreID      string `form:"storeId"`
	Source       string `header:"source"`
}
