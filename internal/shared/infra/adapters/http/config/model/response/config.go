package response

type Config struct {
	Code    string `json:"code"`
	Message string `json:"message"`
	Data    Data   `json:"data"`
}

type Data struct {
	ID      string `json:"id"`
	Country string `json:"country"`
	Value   Value  `json:"value"`
	Active  bool   `json:"active"`
}

type Value struct {
	ObjectID         string  `json:"objectID"`
	CountItems       float64 `json:"countItems"`
	AlgoliaRecommend bool    `json:"algoliaRecommend"`
	QueryProducts    string  `json:"queryProducts"`
}
