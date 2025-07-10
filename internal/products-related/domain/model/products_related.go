package model

import sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"

// AlgoliaRelatedProductsResponse es la estructura principal para la respuesta de productos relacionados.
type AlgoliaRelatedProductsResponse struct {
	Results []AlgoliaResult `json:"results"`
}

// AlgoliaResult contiene una lista de hits y metadatos de la búsqueda.
// Basado en la estructura de respuesta de Algolia que proporcionaste.
type AlgoliaResult struct {
	AppliedRules             interface{}            `json:"appliedRules"` // Puede ser null o un objeto, usamos interface{}
	AppliedRelevancyStrictness int                    `json:"appliedRelevancyStrictness"`
	AroundLatLng             string                 `json:"aroundLatLng"`
	AutomaticRadius          string                 `json:"automaticRadius"`
	ExhaustiveFacetsCount    bool                   `json:"exhaustiveFacetsCount"`
	ExhaustiveNbHits         bool                   `json:"exhaustiveNbHits"`
	Explain                  interface{}            `json:"explain"` // Puede ser null o un objeto
	Extensions               Extensions             `json:"extensions"`
	Facets                   map[string]map[string]int `json:"facets"`
	FacetsStats              map[string]FacetStat   `json:"facets_stats"`
	Hits                     []sharedModel.ProductInformation `json:"hits"` // Reutilizamos ProductInformation para los hits
	HitsPerPage              int                    `json:"hitsPerPage"`
	Index                    string                 `json:"index"`
	IndexUsed                string                 `json:"indexUsed"`
	Length                   int                    `json:"length"`
	Message                  string                 `json:"message"`
	NbHits                   int                    `json:"nbHits"`
	NbPages                  int                    `json:"nbPages"`
	NbSortedHits             int                    `json:"nbSortedHits"`
	Offset                   int                    `json:"offset"`
	Page                     int                    `json:"page"`
	Params                   string                 `json:"params"`
	ParsedQuery              string                 `json:"parsedQuery"`
	ProcessingTimeMS         int                    `json:"processingTimeMS"`
	Query                    string                 `json:"query"`
	QueryAfterRemoval        string                 `json:"queryAfterRemoval"`
	QueryID                  string                 `json:"queryID"`
	ServerUsed               string                 `json:"serverUsed"`
	TimeoutCounts            bool                   `json:"timeoutCounts"`
	TimeoutHits              bool                   `json:"timeoutHits"`
	UserData                 interface{}            `json:"userData"` // Puede ser null o un objeto
	ABTestVariantID          int                    `json:"abTestVariantID"`
	ABTestID                 int                    `json:"abTestID"`
	RenderingContent         map[string]interface{} `json:"renderingContent"`
}

// Extensions contiene información adicional como la categorización de la query.
type Extensions struct {
	QueryCategorization map[string]interface{} `json:"queryCategorization"`
}

// FacetStat contiene estadísticas para una faceta numérica.
type FacetStat struct {
	Min float64 `json:"min"`
	Max float64 `json:"max"`
	Avg float64 `json:"avg"`
	Sum float64 `json:"sum"`
}

// RelatedItem es la estructura que se usaba antes en la app,
// la mantenemos por si es necesaria internamente o para una capa de presentación distinta,
// pero la respuesta principal será AlgoliaRelatedProductsResponse.
// Por ahora, el mapper se centrará en AlgoliaRelatedProductsResponse.
type RelatedItem struct {
	MediaImageUrl     string   `json:"mediaImageUrl"`
	Description       string   `json:"description"`
	FullPrice         float64  `json:"fullPrice"`
	MediaDescription  string   `json:"mediaDescription"`
	Brand             string   `json:"brand"`
	Sales             int64    `json:"sales"`
	DetailDescription string   `json:"detailDescription"`
	OfferPrice        float64  `json:"offerPrice"`
	OfferDescription  string   `json:"offerDescription"`
	ID                string   `json:"id"`
	OfferText         string   `json:"offerText"`
	IdStoreGroup      int64    `json:"idStoreGroup"`
	Marca             string   `json:"marca"`
	ObjectID          string   `json:"objectID"`
	OnlyOnline        bool     `json:"onlyOnline"`
	DeliveryTime      string   `json:"deliveryTime"`
	Highlight         bool     `json:"highlight"`
	Generic           bool     `json:"genericos"`
	LargeDescription  string   `json:"largeDescription"`
	AnywaySelling     bool     `json:"anywaySelling"`
	Spaces            int      `json:"spaces"`
	Status            string   `json:"status"`
	TaxRate           int      `json:"taxRate"`
	ListUrlImages     []string `json:"listUrlImages"`
	MeasurePum        float64  `json:"measurePum"`
	LabelPum          string   `json:"labelPum"`
	HighlightsID      []int    `json:"id_highlights"`
	SuggestedID       []int    `json:"id_suggested"`
	Departments       []string `json:"departments"`
	SubCategory       string   `json:"subCategory"`
	Supplier          string   `json:"supplier"`
	Outofstore        bool     `json:"outofstore"`
	OfferStartDate    int64    `json:"offerStartDate"`
	OfferEndDate      int64    `json:"offerEndDate"`
	PrimePrice        float64  `json:"primePrice"`
	PrimeTextDiscount string   `json:"primeTextDiscount"`
	PrimeDescription  string   `json:"primeDescription"`
	RmsClass          string   `json:"rms_class"`
	RmsDeparment      string   `json:"rms_deparment"`
	RmsGroup          string   `json:"rms_group"`
	RmsSubclass       string   `json:"rms_subclass"`
	WithoutStock      bool     `json:"withoutStock"` // Asumo que es lo contrario a HasStock
	URL               string   `json:"url"`
}
