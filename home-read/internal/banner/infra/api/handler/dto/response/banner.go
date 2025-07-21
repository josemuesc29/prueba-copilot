package response

type BannerResponse struct {
	ID          int                    `json:"id"`
	DocumentID  string                 `json:"documentId"`
	Extras      map[string]interface{} `json:"extras"`
	Viewed      *bool                  `json:"viewed"`
	ImageURL    string                 `json:"imageUrl"`
	Title       string                 `json:"title"`
	Description string                 `json:"description"`
	URL         string                 `json:"url"`
	Type        string                 `json:"type"`
	ExpiresAt   string                 `json:"expiresAt"`
	LinkText    string                 `json:"linkText"`
	CreatedAt   string                 `json:"createdAt"`
	UpdatedAt   string                 `json:"updatedAt"`
	PublishedAt string                 `json:"publishedAt"`
	Locale      string                 `json:"locale"`
	Position    int                    `json:"position"`
	Product     interface{}            `json:"product"`
	Content     *string                `json:"content"`
	Category    string                 `json:"category"`
	BannerName  string                 `json:"bannername"`
}
