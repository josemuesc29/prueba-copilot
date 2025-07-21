package model

type FlashOffer struct {
	Id                  string  `json:"id"`
	Type                string  `json:"type"`
	Position            int     `json:"position"`
	RedirectUrl         string  `json:"redirectUrl"`
	ImageUrl            string  `json:"imageUrl"`
	FullPrice           float64 `json:"fullPrice"`
	MediaDescription    string  `json:"mediaDescription"`
	LargeDescription    string  `json:"largeDescription"`
	OfferPrice          float64 `json:"offerPrice"`
	OfferText           string  `json:"offerText"`
	OfferDescription    string  `json:"offerDescription"`
	PrimePrice          float64 `json:"primePrice"`
	PrimeTextDiscount   string  `json:"primeTextDiscount"`
	PrimeDescription    string  `json:"primeDescription"`
	StartDate           string  `json:"startDate"`
	EndDate             string  `json:"endDate"`
	AvailableStockFlash int     `json:"availableStockFlash"`
}
