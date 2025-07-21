package request

type RqCatalogProductsIndexByObjects struct {
	Requests []Objects `json:"requests"`
}

type Objects struct {
	IndexName string `json:"indexName"`
	ObjectID  string `json:"objectID"`
}
