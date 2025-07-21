package model

type SuggestedCMS struct {
	Data []SuggestedData `json:"data"`
}

type SuggestedData struct {
	ID             int64  `json:"id"`
	Type           string `json:"type"`
	Position       int64  `json:"position"`
	OrderingNumber int64  `json:"orderingNumber"`
	Action         string `json:"action"`
	DocumentID     string `json:"documentId"`
	Sku            string `json:"sku"`
	CreatedAt      string `json:"createdAt"`
	UpdatedAt      string `json:"updatedAt"`
	PublishedAt    string `json:"publishedAt"`
	Locale         string `json:"locale"`
	Name           string `json:"name"`
}
