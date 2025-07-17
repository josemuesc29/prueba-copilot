package response

type Classification struct {
	ID       int    `json:"id"`
	Name     string `json:"name"`
	TypeID   int    `json:"typeId"`
	TypeName string `json:"typeName"`
}

type CustomLabelForStockZeroByCity struct {
	CityCode    string `json:"cityCode"`
	CustomLabel string `json:"customLabel"`
}

type FullPriceByCity struct {
	CityCode  string  `json:"cityCode"`
	FullPrice float64 `json:"fullPrice"`
}

type LastUpdate struct {
	JobName   string `json:"jobName"`
	TimeStamp int64  `json:"timeStamp"`
}

type ProductsRelatedResponse struct {
	ID                            string                          `json:"id"`
	MediaDescription              string                          `json:"mediaDescription"`
	LargeDescription              string                          `json:"largeDescription"`
	MediaImageUrl                 string                          `json:"mediaImageUrl"`
	FullPrice                     float64                         `json:"fullPrice"`
	Prime                         string                          `json:"Prime"`
	RMSProvider                   string                          `json:"RMS_PROVIDER"`
	Collections                   []string                        `json:"_collections"`
	AnywaySelling                 bool                            `json:"anywaySelling"`
	Barcode                       string                          `json:"barcode"`
	BarcodeList                   []string                        `json:"barcodeList"`
	Brand                         string                          `json:"brand"`
	Categorie                     string                          `json:"categorie"`
	Classification                []Classification                `json:"classification"`
	CustomLabelForStockZero       string                          `json:"customLabelForStockZero"`
	CustomLabelForStockZeroByCity []CustomLabelForStockZeroByCity `json:"customLabelForStockZeroByCity"`
	CustomTag                     *string                         `json:"customTag"`
	TotalStock                    int                             `json:"totalStock"`
	DeliveryTime                  string                          `json:"deliveryTime"`
	Departments                   []string                        `json:"departments"`
	DesHiddenSEO                  *string                         `json:"desHiddenSEO"`
	FullPriceByCity               []FullPriceByCity               `json:"fullPriceByCity"`
	Generics                      bool                            `json:"genericos"`
	GrayDescription               string                          `json:"grayDescription"`
	HasStock                      bool                            `json:"hasStock"`
	Highlight                     bool                            `json:"highlight"`
	IDOffersGroup                 []int                           `json:"idOffersGroup"`
	IDHighlights                  []int                           `json:"id_highlights"`
	IDSuggested                   []int                           `json:"id_suggested"`
	IsCupon                       int                             `json:"is_cupon"`
	LabelPum                      string                          `json:"labelPum"`
	LastUpdate                    LastUpdate                      `json:"lastUpdate"`
	ListUrlImages                 []string                        `json:"listUrlImages"`
	Marca                         string                          `json:"marca"`
	MeasurePum                    float64                         `json:"measurePum"`
	MetadesSEO                    *string                         `json:"metadesSEO"`
	MetatituloSEO                 *string                         `json:"metatituloSEO"`
	ObjectID                      string                          `json:"objectID"`
	OfferDescription              string                          `json:"offerDescription"`
	OfferEndDate                  int64                           `json:"offerEndDate"`
	OfferPrice                    float64                         `json:"offerPrice"`
	OfferPriceByCity              []interface{}                   `json:"offerPriceByCity"`
	OfferStartDate                int64                           `json:"offerStartDate"`
	OfferText                     string                          `json:"offerText"`
	OnlyOnline                    bool                            `json:"onlyOnline"`
	Outofstore                    bool                            `json:"outofstore"`
	PrimeDescription              string                          `json:"primeDescription"`
	PrimePrice                    float64                         `json:"primePrice"`
	PrimeTextDiscount             string                          `json:"primeTextDiscount"`
	RequirePrescription           bool                            `json:"requirePrescription"`
	RmsClass                      string                          `json:"rms_class"`
	RmsDeparment                  string                          `json:"rms_deparment"`
	RmsGroup                      string                          `json:"rms_group"`
	RmsSubclass                   string                          `json:"rms_subclass"`
	Sales                         int64                           `json:"sales"`
	Spaces                        int                             `json:"spaces"`
	Status                        string                          `json:"status"`
	StoresWithOffer               []int                           `json:"stores_with_offer"`
	StoresWithPrimeOffer          []interface{}                   `json:"stores_with_prime_offer"`
	StoresWithStock               []int                           `json:"stores_with_stock"`
	SubCategory                   string                          `json:"subCategory"`
	Supplier                      string                          `json:"supplier"`
	TaxRate                       int                             `json:"taxRate"`
	URL                           string                          `json:"url"`
	UrlCanonical                  *string                         `json:"urlCanonical"`
}
