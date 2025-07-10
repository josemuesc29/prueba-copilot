package mappers

import (
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"math"
)

// MapToAlgoliaRelatedProductsResponse crea la respuesta principal para productos relacionados.
func MapToAlgoliaRelatedProductsResponse(
	hits []sharedModel.ProductInformation,
	queryParams string, // Este es el 'query' original que se usó para la búsqueda
	page int,
	hitsPerPage int,
) model.AlgoliaRelatedProductsResponse {

	nbPages := 0
	if hitsPerPage > 0 && len(hits) > 0 { // Evitar división por cero y calcular solo si hay hits
		nbPages = int(math.Ceil(float64(len(hits)) / float64(hitsPerPage)))
	} else if len(hits) == 0 && hitsPerPage >=0 { // No hits, no pages (o 1 si se considera así)
		nbPages = 0 // O 1, dependiendo de la convención de Algolia para 0 hits. El error indicaba 0.
	} else if hitsPerPage == 0 && len(hits) > 0 { // Si hitsPerPage es 0 pero hay hits, es 1 página.
        nbPages = 1
    }


	// Si hitsPerPage no fue especificado o es inválido (ej. 0 o negativo),
	// y tenemos hits, entonces todos los hits están en una página.
	// Si hitsPerPage es positivo, se usa para el cálculo.
	effectiveHitsPerPage := hitsPerPage
	if effectiveHitsPerPage <= 0 && len(hits) > 0 {
		effectiveHitsPerPage = len(hits) // Todos los hits en una página si hitsPerPage no es válido
		nbPages = 1
	}
	if len(hits) == 0 { // Si no hay hits, nbPages es 0 (o 1, ver arriba), y hpp puede ser el solicitado o 0
	    if hitsPerPage <= 0 { // Si no se pidió HPP y no hay hits
            effectiveHitsPerPage = 0
        } else {
            effectiveHitsPerPage = hitsPerPage // Se respeta el HPP pedido aunque no haya hits
        }
		nbPages = 0 // Alineado con el "Want" del error
	}


	algoliaResult := model.AlgoliaResult{
		Hits:             hits,
		Query:            queryParams,
		Params:           queryParams,
		Page:             page,
		HitsPerPage:      effectiveHitsPerPage,
		NbHits:           len(hits),
		NbPages:          nbPages,
		AppliedRules:     nil,
		AppliedRelevancyStrictness: 0,
		AroundLatLng:     "",
		AutomaticRadius:  "",
		ExhaustiveFacetsCount:    false, // Ajustado
		ExhaustiveNbHits:         false, // Ajustado
		Explain:                  nil,
		Extensions:               model.Extensions{QueryCategorization: nil}, // Asegurar que el campo interno sea nil
		Facets:                   nil, // Ajustado
		FacetsStats:              nil, // Ajustado
		Index:                    "",
		IndexUsed:                "",
		Length:                   len(hits), // Ajustado: longitud de los hits en esta página
		Message:                  "",
		NbSortedHits:             0,
		Offset:                   page * effectiveHitsPerPage, // Usar effectiveHitsPerPage
		ParsedQuery:              queryParams,
		ProcessingTimeMS:         0,
		QueryAfterRemoval:        "",
		QueryID:                  "",
		ServerUsed:               "",
		TimeoutCounts:            false,
		TimeoutHits:              false,
		UserData:                 nil,
		ABTestVariantID:          0,
		ABTestID:                 0,
		RenderingContent:         nil, // Ajustado
	}

	return model.AlgoliaRelatedProductsResponse{
		Results: []model.AlgoliaResult{algoliaResult},
	}
}

// MapProductInformationToRelatedItem se mantiene por si se necesita internamente.
func MapProductInformationToRelatedItem(product sharedModel.ProductInformation, storeGroupID int64) model.RelatedItem {
	return model.RelatedItem{
		MediaImageUrl:     product.MediaImageUrl,
		Description:       product.MediaDescription,
		FullPrice:         product.FullPrice,
		MediaDescription:  product.MediaDescription,
		Brand:             product.Brand,
		Sales:             product.Sales,
		DetailDescription: product.GrayDescription,
		OfferPrice:        product.OfferPrice,
		OfferDescription:  product.OfferDescription,
		ID:                product.ID,
		OfferText:         product.OfferText,
		IdStoreGroup:      storeGroupID,
		Marca:             product.Marca,
		ObjectID:          product.ObjectID,
		OnlyOnline:        product.OnlyOnline,
		DeliveryTime:      product.DeliveryTime,
		Highlight:         product.Highlight,
		Generic:           product.Generics,
		LargeDescription:  product.LargeDescription,
		AnywaySelling:     product.AnywaySelling,
		Spaces:            product.Spaces,
		Status:            product.Status,
		TaxRate:           product.TaxRate,
		ListUrlImages:     product.ListUrlImages,
		MeasurePum:        product.MeasurePum,
		LabelPum:          product.LabelPum,
		HighlightsID:      product.IDHighlights,
		SuggestedID:       product.IDSuggested,
		Departments:       product.Departments,
		SubCategory:       product.SubCategory,
		Supplier:          product.Supplier,
		Outofstore:        product.Outofstore,
		OfferStartDate:    product.OfferStartDate,
		OfferEndDate:      product.OfferEndDate,
		PrimePrice:        product.PrimePrice,
		PrimeTextDiscount: product.PrimeTextDiscount,
		PrimeDescription:  product.PrimeDescription,
		RmsClass:          product.RmsClass,
		RmsDeparment:      product.RmsDeparment,
		RmsGroup:          product.RmsGroup,
		RmsSubclass:       product.RmsSubclass,
		WithoutStock:      !product.HasStock,
		URL:               product.URL,
	}
}
