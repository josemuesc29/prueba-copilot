package model

type FlashOfferCms struct {
	Code    string           `json:"code"`
	Message string           `json:"message"`
	Data    []FlashOfferData `json:"data"`
}

type FlashOfferData struct {
	Id                  string `json:"id"`
	Type                string `json:"type"`
	Position            int    `json:"position"`
	RedirectUrl         string `json:"redirectUrl"`
	StartDate           string `json:"startDate"`
	EndDate             string `json:"endDate"`
	TotalStockFlash     int    `json:"totalStockFlash"`
	AvailableStockFlash int    `json:"availableStockFlash"`
}
