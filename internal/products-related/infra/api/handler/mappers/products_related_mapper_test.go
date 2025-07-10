package mappers

import (
	"testing"

	domainModel "ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler/dto/response"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"github.com/stretchr/testify/assert"
)

func TestToResponseDto(t *testing.T) {
	t.Run("should correctly map domain model to response DTO", func(t *testing.T) {
		domainResp := domainModel.AlgoliaRelatedProductsResponse{
			Results: []domainModel.AlgoliaResult{
				{
					AppliedRules:             "rule1",
					AppliedRelevancyStrictness: 1,
					AroundLatLng:             "1.2,3.4",
					AutomaticRadius:          "1000",
					ExhaustiveFacetsCount:    true,
					ExhaustiveNbHits:         true,
					Explain:                  map[string]interface{}{"match": "details"},
					Extensions: domainModel.Extensions{
						QueryCategorization: map[string]interface{}{"category": "A"},
					},
					Facets: map[string]map[string]int{
						"brand": {"BrandA": 10, "BrandB": 5},
					},
					FacetsStats: map[string]domainModel.FacetStat{
						"price": {Min: 10, Max: 100, Avg: 55, Sum: 550},
					},
					Hits: []sharedModel.ProductInformation{
						{ObjectID: "hit1", MediaDescription: "Product 1"},
						{ObjectID: "hit2", MediaDescription: "Product 2"},
					},
					HitsPerPage:      10,
					Index:            "products_co",
					IndexUsed:        "products_co_main",
					Length:           2,
					Message:          "ok",
					NbHits:           2,
					NbPages:          1,
					NbSortedHits:     0,
					Offset:           0,
					Page:             0,
					Params:           "query=test&hitsPerPage=10",
					ParsedQuery:      "test",
					ProcessingTimeMS: 5,
					Query:            "test",
					QueryAfterRemoval:"test",
					QueryID:          "queryABC",
					ServerUsed:       "server1",
					TimeoutCounts:    false,
					TimeoutHits:      false,
					UserData:         nil,
					ABTestVariantID:  0,
					ABTestID:         0,
					RenderingContent: map[string]interface{}{"source": "test"},
				},
			},
		}

		dtoResp := ToResponseDto(domainResp)

		assert.Len(t, dtoResp.Results, 1)
		domainResult := domainResp.Results[0]
		dtoResult := dtoResp.Results[0]

		assert.Equal(t, domainResult.AppliedRules, dtoResult.AppliedRules)
		assert.Equal(t, domainResult.AppliedRelevancyStrictness, dtoResult.AppliedRelevancyStrictness)
		assert.Equal(t, domainResult.AroundLatLng, dtoResult.AroundLatLng)
		assert.Equal(t, domainResult.AutomaticRadius, dtoResult.AutomaticRadius)
		assert.Equal(t, domainResult.ExhaustiveFacetsCount, dtoResult.ExhaustiveFacetsCount)
		assert.Equal(t, domainResult.ExhaustiveNbHits, dtoResult.ExhaustiveNbHits)
		assert.Equal(t, domainResult.Explain, dtoResult.Explain)

		assert.Equal(t, domainResult.Extensions.QueryCategorization, dtoResult.Extensions.QueryCategorization)

		assert.Equal(t, domainResult.Facets, dtoResult.Facets)
		assert.NotNil(t, dtoResult.FacetsStats["price"])
		assert.Equal(t, domainResult.FacetsStats["price"].Min, dtoResult.FacetsStats["price"].Min)

		assert.Len(t, dtoResult.Hits, 2)
		assert.Equal(t, domainResult.Hits[0].ObjectID, dtoResult.Hits[0].ObjectID)
		assert.Equal(t, domainResult.Hits[1].MediaDescription, dtoResult.Hits[1].MediaDescription)

		assert.Equal(t, domainResult.HitsPerPage, dtoResult.HitsPerPage)
		assert.Equal(t, domainResult.Index, dtoResult.Index)
		assert.Equal(t, domainResult.IndexUsed, dtoResult.IndexUsed)
		assert.Equal(t, domainResult.Length, dtoResult.Length)
		assert.Equal(t, domainResult.Message, dtoResult.Message)
		assert.Equal(t, domainResult.NbHits, dtoResult.NbHits)
		assert.Equal(t, domainResult.NbPages, dtoResult.NbPages)
		assert.Equal(t, domainResult.NbSortedHits, dtoResult.NbSortedHits)
		assert.Equal(t, domainResult.Offset, dtoResult.Offset)
		assert.Equal(t, domainResult.Page, dtoResult.Page)
		assert.Equal(t, domainResult.Params, dtoResult.Params)
		assert.Equal(t, domainResult.ParsedQuery, dtoResult.ParsedQuery)
		assert.Equal(t, domainResult.ProcessingTimeMS, dtoResult.ProcessingTimeMS)
		assert.Equal(t, domainResult.Query, dtoResult.Query)
		assert.Equal(t, domainResult.QueryAfterRemoval, dtoResult.QueryAfterRemoval)
		assert.Equal(t, domainResult.QueryID, dtoResult.QueryID)
		assert.Equal(t, domainResult.ServerUsed, dtoResult.ServerUsed)
		assert.Equal(t, domainResult.TimeoutCounts, dtoResult.TimeoutCounts)
		assert.Equal(t, domainResult.TimeoutHits, dtoResult.TimeoutHits)
		assert.Equal(t, domainResult.UserData, dtoResult.UserData)
		assert.Equal(t, domainResult.ABTestVariantID, dtoResult.ABTestVariantID)
		assert.Equal(t, domainResult.ABTestID, dtoResult.ABTestID)
		assert.Equal(t, domainResult.RenderingContent, dtoResult.RenderingContent)
	})

	t.Run("should handle empty results", func(t *testing.T) {
		domainResp := domainModel.AlgoliaRelatedProductsResponse{
			Results: []domainModel.AlgoliaResult{},
		}
		dtoResp := ToResponseDto(domainResp)
		assert.Len(t, dtoResp.Results, 0)
	})

	t.Run("should handle nil maps in domain model", func(t *testing.T) {
		domainResp := domainModel.AlgoliaRelatedProductsResponse{
			Results: []domainModel.AlgoliaResult{
				{
					Extensions: domainModel.Extensions{
						QueryCategorization: nil, // Probando nil
					},
					Facets:      nil, // Probando nil
					FacetsStats: nil, // Probando nil
					Hits:        []sharedModel.ProductInformation{},
				},
			},
		}

		dtoResp := ToResponseDto(domainResp)
		assert.Len(t, dtoResp.Results, 1)
		dtoResult := dtoResp.Results[0]

		assert.NotNil(t, dtoResult.Extensions.QueryCategorization) // Mapper debe inicializarlo
		assert.Empty(t, dtoResult.Extensions.QueryCategorization)

		assert.Nil(t, dtoResult.Facets) // Mapper actual lo pasa tal cual

		assert.NotNil(t, dtoResult.FacetsStats) // Mapper actual lo inicializa si es nil en origen
		assert.Empty(t, dtoResult.FacetsStats)

		assert.Empty(t, dtoResult.Hits)
	})
}
