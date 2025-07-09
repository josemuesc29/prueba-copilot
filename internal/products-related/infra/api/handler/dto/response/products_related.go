package response

type SellerAddress struct {
	DaneCode string `json:"daneCode"`
	Address  string `json:"address"`
}

type Dimensions struct {
	Weight string `json:"weight"`
	Height string `json:"height"`
	Width  string `json:"width"`
	Length string `json:"length"`
}

type SEOOffer struct {
	PriceCurrency   string `json:"priceCurrency"`
	Price           string `json:"price"`
	LowPrice        string `json:"lowPrice"`
	PriceValidUntil string `json:"priceValidUntil"`
	ItemCondition   string `json:"itemCondition"`
	Availability    string `json:"availability"`
	Seller          struct {
		Name string `json:"name"`
		Type string `json:"@type"`
	} `json:"seller"`
	HighPrice string `json:"highPrice"`
	Type      string `json:"@type"`
}

type SEO struct {
	Name        string   `json:"name"`
	Image       string   `json:"image"`
	Description string   `json:"description"`
	SKU         string   `json:"sku"`
	Offers      SEOOffer `json:"offers"`
	Context     string   `json:"@context"`
	Type        string   `json:"@type"`
}

type Item struct {
	AnywaySelling              bool            `json:"anywaySelling"`
	Brand                      string          `json:"brand"`
	OfferText                  string          `json:"offerText"`
	OfferDescription           string          `json:"offerDescription"`
	FullPrice                  float64         `json:"fullPrice"`
	GrayDescription            string          `json:"grayDescription"`
	Highlight                  bool            `json:"highlight"`
	ID                         string          `json:"id"`
	IsGeneric                  bool            `json:"isGeneric"`
	MediaDescription           string          `json:"mediaDescription"`
	LargeDescription           string          `json:"largeDescription"`
	MediaImageURL              string          `json:"mediaImageUrl"`
	OfferPrice                 float64         `json:"offerPrice"`
	Outstanding                bool            `json:"outstanding"`
	RequirePrescription        string          `json:"requirePrescription"`
	Sales                      string          `json:"sales"`
	Spaces                     string          `json:"spaces"`
	Status                     string          `json:"status"`
	TaxRate                    float64         `json:"taxRate"`
	TotalStock                 int             `json:"totalStock"`
	QuantitySold               int             `json:"quantitySold"`
	IDClassification           int             `json:"idClassification"`
	ExpressWithSubscription    bool            `json:"expressWithSubscription"`
	PosGroup                   string          `json:"posGroup"`
	ListURLImages              []string        `json:"listUrlImages"`
	Categorie                  string          `json:"categorie"`
	Marca                      string          `json:"marca"`
	Departments                []string        `json:"departments"`
	SubCategory                string          `json:"subCategory"`
	Supplier                   string          `json:"supplier"`
	DeliveryPrice              float64         `json:"deliveryPrice"`
	SEO                        SEO             `json:"seo"`
	TextSEO                    string          `json:"textSEO"`
	OnlyOnline                 bool            `json:"onlyOnline"`
	DeliveryTime               string          `json:"deliveryTime"`
	GlobalStock                int             `json:"globalStock"`
	OutOfStore                 bool            `json:"outofstore"`
	IsFlashOffer               bool            `json:"isFlashOffer"`
	OfferStartDate             string          `json:"offerStartDate"`
	OfferEndDate               string          `json:"offerEndDate"`
	PrimePrice                 float64         `json:"primePrice"`
	PrimeTextDiscount          string          `json:"primeTextDiscount"`
	PrimeDescription           string          `json:"primeDescription"`
	RMSClass                   string          `json:"rms_class"`
	RMSDepartment              string          `json:"rms_deparment"`
	RMSGroup                   string          `json:"rms_group"`
	RMSSubclass                string          `json:"rms_subclass"`
	WithoutStock               bool            `json:"without_stock"`
	URL                        string          `json:"url"`
	RequirePrescriptionMedical bool            `json:"requirePrescriptionMedical"`
	SellerAddresses            []SellerAddress `json:"sellerAddresses"`
	Dimensions                 Dimensions      `json:"dimensions"`
	UUIDItem                   string          `json:"uuidItem"`
	Warranty                   string          `json:"warranty"`
	WarrantyTerms              string          `json:"warrantyTerms"`
	CustomLabelForStockZero    string          `json:"customLabelForStockZero"`
	StoresWithStock            []string        `json:"storesWithStock"`
	Generic                    bool            `json:"generic"`
}
