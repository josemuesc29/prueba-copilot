package model

type CatalogCategory struct {
	CountryID              string  `json:"countryId"`
	ClassificationID       int     `json:"classificationId"`
	ClassificationTypeID   int     `json:"classificationTypeId"`
	Name                   string  `json:"name"`
	Image                  string  `json:"image"`
	Color                  string  `json:"color"`
	SecondColor            string  `json:"secondColor"`
	Active                 bool    `json:"active"`
	Order                  int     `json:"order"`
	AnywaySelling          bool    `json:"anywaySelling"`
	Division               int     `json:"division"`
	GroupNo                int     `json:"groupNo"`
	Dept                   int     `json:"dept"`
	ClassClassification    int     `json:"classClassification"`
	SubclassClassification int     `json:"subclassClassification"`
	MetaTitle              string  `json:"metaTitle"`
	MetaDescription        string  `json:"metaDescription"`
	MenuNavigationType     string  `json:"menuNavigationType"`
	Path                   string  `json:"path"`
	Redirect               bool    `json:"redirect"`
	HtmlSEO                string  `json:"htmlSEO"`
	ClassificationParentID *int    `json:"classificationParentId"`
	UpdateDate             *string `json:"updateDate"`
	CreateDate             *string `json:"createDate"`
}
