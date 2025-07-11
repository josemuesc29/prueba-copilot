package response

import (
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
)

type ProductsRelatedResponseDto struct {
	Results []AlgoliaResultDto `json:"results"`
}

type AlgoliaResultDto struct {
	AppliedRules               interface{}                `json:"appliedRules"`
	AppliedRelevancyStrictness int                        `json:"appliedRelevancyStrictness"`
	AroundLatLng               string                     `json:"aroundLatLng"`
	AutomaticRadius            string                     `json:"automaticRadius"`
	ExhaustiveFacetsCount      bool                       `json:"exhaustiveFacetsCount"`
	ExhaustiveNbHits           bool                       `json:"exhaustiveNbHits"`
	Explain                    interface{}                `json:"explain"`
	Extensions                 ExtensionsDto              `json:"extensions"`
	Facets                     map[string]map[string]int  `json:"facets"`
	FacetsStats                map[string]FacetStatDto    `json:"facets_stats"`
	Hits                       []model.ProductInformation `json:"hits"`
	HitsPerPage                int                        `json:"hitsPerPage"`
	Index                      string                     `json:"index"`
	IndexUsed                  string                     `json:"indexUsed"`
	Length                     int                        `json:"length"`
	Message                    string                     `json:"message"`
	NbHits                     int                        `json:"nbHits"`
	NbPages                    int                        `json:"nbPages"`
	NbSortedHits               int                        `json:"nbSortedHits"`
	Offset                     int                        `json:"offset"`
	Page                       int                        `json:"page"`
	Params                     string                     `json:"params"`
	ParsedQuery                string                     `json:"parsedQuery"`
	ProcessingTimeMS           int                        `json:"processingTimeMS"`
	Query                      string                     `json:"query"`
	QueryAfterRemoval          string                     `json:"queryAfterRemoval"`
	QueryID                    string                     `json:"queryID"`
	ServerUsed                 string                     `json:"serverUsed"`
	TimeoutCounts              bool                       `json:"timeoutCounts"`
	TimeoutHits                bool                       `json:"timeoutHits"`
	UserData                   interface{}                `json:"userData"`
	ABTestVariantID            int                        `json:"abTestVariantID"`
	ABTestID                   int                        `json:"abTestID"`
	RenderingContent           map[string]interface{}     `json:"renderingContent"`
}

type ExtensionsDto struct {
	QueryCategorization map[string]interface{} `json:"queryCategorization"`
}

type FacetStatDto struct {
	Min float64 `json:"min"`
	Max float64 `json:"max"`
	Avg float64 `json:"avg"`
	Sum float64 `json:"sum"`
}
