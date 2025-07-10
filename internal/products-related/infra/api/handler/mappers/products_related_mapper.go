package mappers

import (
	domainModel "ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler/dto/response"
	// sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model" // No es necesario aquí directamente si ProductInformation es parte de domainModel.AlgoliaResult
)

// ToResponseDto convierte el modelo de dominio AlgoliaRelatedProductsResponse
// al DTO ProductsRelatedResponseDto para la capa de handler.
func ToResponseDto(domainResp domainModel.AlgoliaRelatedProductsResponse) response.ProductsRelatedResponseDto {
	dtoResults := make([]response.AlgoliaResultDto, len(domainResp.Results))

	for i, domainResult := range domainResp.Results {
		// Hits ya son del tipo []sharedModel.ProductInformation, que es lo que espera AlgoliaResultDto.
		// No se necesita una copia profunda aquí a menos que queramos asegurar inmutabilidad total,
		// pero para DTOs de respuesta, una copia superficial de la slice es común.
		dtoHits := make([]domainModel.ProductInformation, len(domainResult.Hits))
		copy(dtoHits, domainResult.Hits)

		dtoFacetsStats := make(map[string]response.FacetStatDto)
		if domainResult.FacetsStats != nil {
			for k, v := range domainResult.FacetsStats {
				dtoFacetsStats[k] = response.FacetStatDto(v) // Conversión directa de struct
			}
		}

		dtoExtensions := response.ExtensionsDto{ // Conversión directa de struct
			QueryCategorization: domainResult.Extensions.QueryCategorization,
		}
		if domainResult.Extensions.QueryCategorization == nil {
			dtoExtensions.QueryCategorization = make(map[string]interface{})
		}


		dtoResults[i] = response.AlgoliaResultDto{
			AppliedRules:             domainResult.AppliedRules,
			AppliedRelevancyStrictness: domainResult.AppliedRelevancyStrictness,
			AroundLatLng:             domainResult.AroundLatLng,
			AutomaticRadius:          domainResult.AutomaticRadius,
			ExhaustiveFacetsCount:    domainResult.ExhaustiveFacetsCount,
			ExhaustiveNbHits:         domainResult.ExhaustiveNbHits,
			Explain:                  domainResult.Explain,
			Extensions:               dtoExtensions,
			Facets:                   domainResult.Facets, // map[string]map[string]int es compartido directamente
			FacetsStats:              dtoFacetsStats,
			Hits:                     dtoHits,
			HitsPerPage:              domainResult.HitsPerPage,
			Index:                    domainResult.Index,
			IndexUsed:                domainResult.IndexUsed,
			Length:                   domainResult.Length,
			Message:                  domainResult.Message,
			NbHits:                   domainResult.NbHits,
			NbPages:                  domainResult.NbPages,
			NbSortedHits:             domainResult.NbSortedHits,
			Offset:                   domainResult.Offset,
			Page:                     domainResult.Page,
			Params:                   domainResult.Params,
			ParsedQuery:              domainResult.ParsedQuery,
			ProcessingTimeMS:         domainResult.ProcessingTimeMS,
			Query:                    domainResult.Query,
			QueryAfterRemoval:        domainResult.QueryAfterRemoval,
			QueryID:                  domainResult.QueryID,
			ServerUsed:               domainResult.ServerUsed,
			TimeoutCounts:            domainResult.TimeoutCounts,
			TimeoutHits:              domainResult.TimeoutHits,
			UserData:                 domainResult.UserData,
			ABTestVariantID:          domainResult.ABTestVariantID,
			ABTestID:                 domainResult.ABTestID,
			RenderingContent:         domainResult.RenderingContent,
		}
	}
	return response.ProductsRelatedResponseDto{Results: dtoResults}
}
