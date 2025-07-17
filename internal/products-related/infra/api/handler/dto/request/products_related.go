package request

type ProductsRelatedRequestDto struct {
	CountryID string `uri:"countryId" binding:"required"`
	ItemID    string `uri:"itemId" binding:"required"`

	// Query parameters
	NearbyStores  string `form:"nearby-stores"`
	City          string `form:"city"`
	QueryAlgolia  string `form:"query"`      // Query para Algolia
	IndexName     string `form:"index-name"` // Nombre del índice opcional para Algolia
	AlgoliaParams string `form:"params"`     // Otros parámetros específicos de Algolia como cadena
	Category      string `form:"category"`
}
