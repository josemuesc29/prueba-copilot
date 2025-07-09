package request

type RqCatalogProductsIndexByObjects struct {
	Requests []Objects `json:"requests"`
}

type Objects struct {
	IndexName string `json:"indexName"`
	ObjectID  string `json:"objectID"`
}

type CatalogByQuery struct {
	Requests []Query `json:"requests"`
}

type Query struct {
	Query     string `json:"query"`
	IndexName string `json:"indexName"`
	Params    string `json:"params"`
}
