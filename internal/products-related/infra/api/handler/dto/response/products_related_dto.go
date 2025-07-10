package response

import (
	// Necesitamos el tipo base del modelo de dominio para construir este DTO.
	// O, si queremos total desacoplamiento, replicaríamos las estructuras aquí.
	// Por simplicidad y dado que la respuesta es compleja y ya está definida en el dominio,
	// vamos a basar el DTO en las estructuras del dominio.
	domainModel "ftd-td-catalog-item-read-services/internal/products-related/domain/model"
)

// ProductsRelatedResponseDto define la estructura de respuesta para el endpoint de productos relacionados.
// Se basa directamente en la estructura AlgoliaRelatedProductsResponse del dominio,
// ya que la HU pide replicar la respuesta de Algolia.
// Si la API necesitara una forma diferente en el futuro, esta DTO se modificaría.
type ProductsRelatedResponseDto struct {
	Results []AlgoliaResultDto `json:"results"`
}

// AlgoliaResultDto es la versión DTO de domainModel.AlgoliaResult.
type AlgoliaResultDto struct {
	AppliedRules             interface{}                       `json:"appliedRules"`
	AppliedRelevancyStrictness int                               `json:"appliedRelevancyStrictness"`
	AroundLatLng             string                            `json:"aroundLatLng"`
	AutomaticRadius          string                            `json:"automaticRadius"`
	ExhaustiveFacetsCount    bool                              `json:"exhaustiveFacetsCount"`
	ExhaustiveNbHits         bool                              `json:"exhaustiveNbHits"`
	Explain                  interface{}                       `json:"explain"`
	Extensions               ExtensionsDto                     `json:"extensions"`
	Facets                   map[string]map[string]int         `json:"facets"`
	FacetsStats              map[string]FacetStatDto           `json:"facets_stats"`
	Hits                     []domainModel.ProductInformation  `json:"hits"` // Reutiliza ProductInformation del shared domain model
	HitsPerPage              int                               `json:"hitsPerPage"`
	Index                    string                            `json:"index"`
	IndexUsed                string                            `json:"indexUsed"`
	Length                   int                               `json:"length"`
	Message                  string                            `json:"message"`
	NbHits                   int                               `json:"nbHits"`
	NbPages                  int                               `json:"nbPages"`
	NbSortedHits             int                               `json:"nbSortedHits"`
	Offset                   int                               `json:"offset"`
	Page                     int                               `json:"page"`
	Params                   string                            `json:"params"`
	ParsedQuery              string                            `json:"parsedQuery"`
	ProcessingTimeMS         int                               `json:"processingTimeMS"`
	Query                    string                            `json:"query"`
	QueryAfterRemoval        string                            `json:"queryAfterRemoval"`
	QueryID                  string                            `json:"queryID"`
	ServerUsed               string                            `json:"serverUsed"`
	TimeoutCounts            bool                              `json:"timeoutCounts"`
	TimeoutHits              bool                              `json:"timeoutHits"`
	UserData                 interface{}                       `json:"userData"`
	ABTestVariantID          int                               `json:"abTestVariantID"`
	ABTestID                 int                               `json:"abTestID"`
	RenderingContent         map[string]interface{}            `json:"renderingContent"`
}

// ExtensionsDto es la versión DTO de domainModel.Extensions.
type ExtensionsDto struct {
	QueryCategorization map[string]interface{} `json:"queryCategorization"`
}

// FacetStatDto es la versión DTO de domainModel.FacetStat.
type FacetStatDto struct {
	Min float64 `json:"min"`
	Max float64 `json:"max"`
	Avg float64 `json:"avg"`
	Sum float64 `json:"sum"`
}

// Helper para convertir desde el modelo de dominio al DTO de respuesta.
// Esto se moverá a un archivo mapper en el siguiente paso.
// func FromDomainToResponseDto(domainResp domainModel.AlgoliaRelatedProductsResponse) ProductsRelatedResponseDto {
// 	dtoResults := make([]AlgoliaResultDto, len(domainResp.Results))
// 	for i, domainResult := range domainResp.Results {
// 		dtoHits := make([]domainModel.ProductInformation, len(domainResult.Hits))
// 		copy(dtoHits, domainResult.Hits) // ProductInformation es compartido

// 		dtoFacetsStats := make(map[string]FacetStatDto)
// 		for k, v := range domainResult.FacetsStats {
// 			dtoFacetsStats[k] = FacetStatDto(v)
// 		}

// 		dtoResults[i] = AlgoliaResultDto{
// 			AppliedRules:             domainResult.AppliedRules,
// 			AppliedRelevancyStrictness: domainResult.AppliedRelevancyStrictness,
// 			AroundLatLng:             domainResult.AroundLatLng,
// 			AutomaticRadius:          domainResult.AutomaticRadius,
// 			ExhaustiveFacetsCount:    domainResult.ExhaustiveFacetsCount,
// 			ExhaustiveNbHits:         domainResult.ExhaustiveNbHits,
// 			Explain:                  domainResult.Explain,
// 			Extensions:               ExtensionsDto(domainResult.Extensions),
// 			Facets:                   domainResult.Facets, // map[string]map[string]int es compartido
// 			FacetsStats:              dtoFacetsStats,
// 			Hits:                     dtoHits,
// 			HitsPerPage:              domainResult.HitsPerPage,
// 			Index:                    domainResult.Index,
// 			IndexUsed:                domainResult.IndexUsed,
// 			Length:                   domainResult.Length,
// 			Message:                  domainResult.Message,
// 			NbHits:                   domainResult.NbHits,
// 			NbPages:                  domainResult.NbPages,
// 			NbSortedHits:             domainResult.NbSortedHits,
// 			Offset:                   domainResult.Offset,
// 			Page:                     domainResult.Page,
// 			Params:                   domainResult.Params,
// 			ParsedQuery:              domainResult.ParsedQuery,
// 			ProcessingTimeMS:         domainResult.ProcessingTimeMS,
// 			Query:                    domainResult.Query,
// 			QueryAfterRemoval:        domainResult.QueryAfterRemoval,
// 			QueryID:                  domainResult.QueryID,
// 			ServerUsed:               domainResult.ServerUsed,
// 			TimeoutCounts:            domainResult.TimeoutCounts,
// 			TimeoutHits:              domainResult.TimeoutHits,
// 			UserData:                 domainResult.UserData,
// 			ABTestVariantID:          domainResult.ABTestVariantID,
// 			ABTestID:                 domainResult.ABTestID,
// 			RenderingContent:         domainResult.RenderingContent,
// 		}
// 	}
// 	return ProductsRelatedResponseDto{Results: dtoResults}
// }
