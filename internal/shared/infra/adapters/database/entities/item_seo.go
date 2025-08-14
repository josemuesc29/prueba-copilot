package entities

type ItemSeoEntity struct {
	CountryID       string  `gorm:"column:country_id;type:varchar(2);not null"`
	ItemID          int64   `gorm:"column:item_id;type:numeric(10);not null"`
	Description     string  `gorm:"column:description;type:varchar(250);not null"`
	ActivePrinciple *string `gorm:"column:active_principle;type:varchar(250)"`
	Brand           *string `gorm:"column:brand;type:varchar(50)"`
	TextSeoWeb      *string `gorm:"column:text_seo_web;type:varchar(10000000)"`
	TextSeoApp      *string `gorm:"column:text_seo_app;type:varchar(10000000)"`
}

func (ItemSeoEntity) TableName() string {
	return "catalog.item_seo"
}
