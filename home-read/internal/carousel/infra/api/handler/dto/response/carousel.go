package response

type Suggested struct {
	ID               int64   `json:"id"`
	FirstDescription string  `json:"firstDescription"`
	OfferText        string  `json:"offerText"`
	Type             string  `json:"type"`
	UrlImage         string  `json:"urlImage"`
	StartDate        int64   `json:"startDate"`
	EndDate          int64   `json:"endDate"`
	OrderingNumber   int64   `json:"orderingNumber"`
	Action           string  `json:"action"`
	Position         int64   `json:"position"`
	Product          Product `json:"product"`
}

type Product struct {
	MediaImageUrl     string   `json:"mediaImageUrl"`
	Description       string   `json:"description"`
	FullPrice         float64  `json:"fullPrice"`
	MediaDescription  string   `json:"mediaDescription"`
	Brand             string   `json:"brand"`
	Sales             int64    `json:"sales"`
	DetailDescription string   `json:"detailDescription"`
	OfferPrice        float64  `json:"offerPrice"`
	OfferDescription  string   `json:"offerDescription"`
	ID                string   `json:"id"`
	OfferText         string   `json:"offerText"`
	IdStoreGroup      int64    `json:"idStoreGroup"`
	Marca             string   `json:"marca"`
	ObjectID          string   `json:"objectID"`
	OnlyOnline        bool     `json:"onlyOnline"`
	DeliveryTime      string   `json:"deliveryTime"`
	Highlight         bool     `json:"highlight"`
	Generic           int      `json:"generic"`
	LargeDescription  string   `json:"largeDescription"`
	AnywaySelling     bool     `json:"anywaySelling"`
	Spaces            int      `json:"spaces"`
	Status            string   `json:"status"`
	TaxRate           int      `json:"taxRate"`
	ListUrlImages     []string `json:"listUrlImages"`
	MeasurePum        float64  `json:"measurePum"`
	LabelPum          string   `json:"labelPum"`
	HighlightsID      []int    `json:"id_highlights"`
	SuggestedID       []int    `json:"id_suggested"`
	Departments       []string `json:"departments"`
	SubCategory       string   `json:"subCategory"`
	Supplier          string   `json:"supplier"`
	Outofstore        bool     `json:"outofstore"`
	OfferStartDate    int64    `json:"offerStartDate"`
	OfferEndDate      int64    `json:"offerEndDate"`
	PrimePrice        float64  `json:"primePrice"`
	PrimeTextDiscount string   `json:"primeTextDiscount"`
	PrimeDescription  string   `json:"primeDescription"`
	RmsClass          string   `json:"rms_class"`
	RmsDeparment      string   `json:"rms_deparment"`
	RmsGroup          string   `json:"rms_group"`
	RmsSubclass       string   `json:"rms_subclass"`
	WithoutStock      bool     `json:"without_stock"`
	URL               string   `json:"url"`
}
