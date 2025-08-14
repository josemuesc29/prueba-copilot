package entities

type LocationItem struct {
	CountryID string `gorm:"column:country_id"`
	LocationID int64 `gorm:"column:location_id"`
	ItemID    int64 `gorm:"column:item_id"`
	Stock     int64 `gorm:"column:stock"`
}

func (LocationItem) TableName() string {
	return "location_item"
}
