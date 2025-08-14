package model

type ConfigBestSeller struct {
	ObjectID          string `json:"objectID"`
	AlgoliaRecommend  bool   `json:"algoliaRecommend"`
	CountItems        int    `json:"countItems"`
	QueryProducts     string `json:"queryProducts"`
	MaxItemsSubtitute int    `json:"maxItemsSubtitute"`
}
