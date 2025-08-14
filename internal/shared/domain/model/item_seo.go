package model

type ItemSeo struct {
	CountryID       string
	ItemID          int64
	Description     string
	ActivePrinciple *string
	Brand           *string
	TextSeoWeb      *string
	TextSeoApp      *string
}
