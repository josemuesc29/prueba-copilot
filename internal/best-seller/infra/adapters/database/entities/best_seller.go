package entities

import "time"

type BestSellerEntity struct {
	CountryId        string    `json:"countryId" gorm:"primaryKey;column:country_id"`
	ItemBestSellerId string    `json:"itemBestSellerId" gorm:"primaryKey;column:item_best_seller_id"`
	DepartmentId     string    `json:"departmentId" gorm:"column:department_id"`
	ItemId           string    `json:"itemId" gorm:"column:item_id"`
	CreateDate       time.Time `json:"createDate" gorm:"column:create_date"`
	UpdateDate       time.Time `json:"updateDate" gorm:"column:update_date"`
}

func (BestSellerEntity) TableName() string {
	return "catalog_item.best_sellers"
}
