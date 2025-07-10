package mappers

import (
	model2 "ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler/dto/response"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
)

func ToResponseDto(domainResp model2.AlgoliaRelatedProductsResponse) response.ProductsRelatedResponseDto {
	dtoResults := make([]response.AlgoliaResultDto, len(domainResp.Results))

	for i, domainResult := range domainResp.Results {
		dtoHits := make([]model.ProductInformation, len(domainResult.Hits))
		copy(dtoHits, domainResult.Hits)

		dtoFacetsStats := make(map[string]response.FacetStatDto)
		if domainResult.FacetsStats != nil {
			for k, v := range domainResult.FacetsStats {
				dtoFacetsStats[k] = response.FacetStatDto(v)
			}
		}

		dtoExtensions := response.ExtensionsDto{
			QueryCategorization: domainResult.Extensions.QueryCategorization,
		}
		if domainResult.Extensions.QueryCategorization == nil {
			dtoExtensions.QueryCategorization = make(map[string]interface{})
		}

		dtoResults[i] = response.AlgoliaResultDto{
			AppliedRules:               domainResult.AppliedRules,
			AppliedRelevancyStrictness: domainResult.AppliedRelevancyStrictness,
			AroundLatLng:               domainResult.AroundLatLng,
			AutomaticRadius:            domainResult.AutomaticRadius,
			ExhaustiveFacetsCount:      domainResult.ExhaustiveFacetsCount,
			ExhaustiveNbHits:           domainResult.ExhaustiveNbHits,
			Explain:                    domainResult.Explain,
			Extensions:                 dtoExtensions,
			Facets:                     domainResult.Facets,
			FacetsStats:                dtoFacetsStats,
			Hits:                       dtoHits,
			HitsPerPage:                domainResult.HitsPerPage,
			Index:                      domainResult.Index,
			IndexUsed:                  domainResult.IndexUsed,
			Length:                     domainResult.Length,
			Message:                    domainResult.Message,
			NbHits:                     domainResult.NbHits,
			NbPages:                    domainResult.NbPages,
			NbSortedHits:               domainResult.NbSortedHits,
			Offset:                     domainResult.Offset,
			Page:                       domainResult.Page,
			Params:                     domainResult.Params,
			ParsedQuery:                domainResult.ParsedQuery,
			ProcessingTimeMS:           domainResult.ProcessingTimeMS,
			Query:                      domainResult.Query,
			QueryAfterRemoval:          domainResult.QueryAfterRemoval,
			QueryID:                    domainResult.QueryID,
			ServerUsed:                 domainResult.ServerUsed,
			TimeoutCounts:              domainResult.TimeoutCounts,
			TimeoutHits:                domainResult.TimeoutHits,
			UserData:                   domainResult.UserData,
			ABTestVariantID:            domainResult.ABTestVariantID,
			ABTestID:                   domainResult.ABTestID,
			RenderingContent:           domainResult.RenderingContent,
		}
	}
	return response.ProductsRelatedResponseDto{Results: dtoResults}
}
