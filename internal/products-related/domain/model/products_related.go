package model

import sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"

// AlgoliaRecommendRequest is the structure for making a request to Algolia's recommend API.
type AlgoliaRecommendRequest struct {
	Requests []RequestRecommend `json:"requests"`
}

// RequestRecommend defines a single recommendation request.
type RequestRecommend struct {
	IndexName          string           `json:"indexName"`
	Model              string           `json:"model"`
	ObjectID           string           `json:"objectID"`
	Threshold          int              `json:"threshold"`
	MaxRecommendations int              `json:"maxRecommendations,omitempty"`
	QueryParameters    *QueryParameters `json:"queryParameters,omitempty"`
}

// QueryParameters holds the query parameters for the recommendation request.
type QueryParameters struct {
	FacetFilters [][]string `json:"facetFilters,omitempty"`
}

// AlgoliaRecommendResponse is the structure for the response from Algolia's recommend API.
type AlgoliaRecommendResponse struct {
	Results []RecommendResult `json:"results"`
}

// RecommendResult contains the results of a single recommendation request.
type RecommendResult struct {
	Hits    []sharedModel.ProductInformation `json:"hits"`
	Message string                           `json:"message,omitempty"`
	Status  int                              `json:"status,omitempty"`
}

type AlgoliaRelatedProductsResponse struct {
	Results []AlgoliaResult `json:"results"`
}

type AlgoliaResult struct {
	AppliedRules               interface{}                      `json:"appliedRules"`
	AppliedRelevancyStrictness int                              `json:"appliedRelevancyStrictness"`
	AroundLatLng               string                           `json:"aroundLatLng"`
	AutomaticRadius            string                           `json:"automaticRadius"`
	ExhaustiveFacetsCount      bool                             `json:"exhaustiveFacetsCount"`
	ExhaustiveNbHits           bool                             `json:"exhaustiveNbHits"`
	Explain                    interface{}                      `json:"explain"`
	Extensions                 Extensions                       `json:"extensions"`
	Facets                     map[string]map[string]int        `json:"facets"`
	FacetsStats                map[string]FacetStat             `json:"facets_stats"`
	Hits                       []sharedModel.ProductInformation `json:"hits"`
	HitsPerPage                int                              `json:"hitsPerPage"`
	Index                      string                           `json:"index"`
	IndexUsed                  string                           `json:"indexUsed"`
	Length                     int                              `json:"length"`
	Message                    string                           `json:"message"`
	NbHits                     int                              `json:"nbHits"`
	NbPages                    int                              `json:"nbPages"`
	NbSortedHits               int                              `json:"nbSortedHits"`
	Offset                     int                              `json:"offset"`
	Page                       int                              `json:"page"`
	Params                     string                           `json:"params"`
	ParsedQuery                string                           `json:"parsedQuery"`
	ProcessingTimeMS           int                              `json:"processingTimeMS"`
	Query                      string                           `json:"query"`
	QueryAfterRemoval          string                           `json:"queryAfterRemoval"`
	QueryID                    string                           `json:"queryID"`
	ServerUsed                 string                           `json:"serverUsed"`
	TimeoutCounts              bool                             `json:"timeoutCounts"`
	TimeoutHits                bool                             `json:"timeoutHits"`
	UserData                   interface{}                      `json:"userData"` // Puede ser null o un objeto
	ABTestVariantID            int                              `json:"abTestVariantID"`
	ABTestID                   int                              `json:"abTestID"`
	RenderingContent           map[string]interface{}           `json:"renderingContent"`
}

type Extensions struct {
	QueryCategorization map[string]interface{} `json:"queryCategorization"`
}

type FacetStat struct {
	Min float64 `json:"min"`
	Max float64 `json:"max"`
	Avg float64 `json:"avg"`
	Sum float64 `json:"sum"`
}

type RelatedProductItem struct {
	ID               string   `json:"id"`
	Description      string   `json:"description"`
	MediaDescription string   `json:"media_description"`
	Brand            string   `json:"brand"`
	MediaImageURL    string   `json:"media_image_url"`
	FullPrice        float64  `json:"full_price"`
	OfferPrice       float64  `json:"offer_price"`
	OfferDescription string   `json:"offer_description"`
	OfferText        string   `json:"offer_text"`
	OnlyOnline       bool     `json:"only_online"`
	DeliveryTime     string   `json:"delivery_time"`
	URL              string   `json:"url"`
	HasStock         bool     `json:"has_stock"`
	StoresWithStock  []string `json:"stores_with_stock"`
}
