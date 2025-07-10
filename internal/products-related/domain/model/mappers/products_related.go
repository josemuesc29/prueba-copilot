package mappers

import (
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
)

// MapToAlgoliaRelatedProductsResponse crea la respuesta principal para productos relacionados.
// Por ahora, principalmente llena los Hits. Otros campos de AlgoliaResult
// requerirían que GetProductsInformationByQuery devuelva más que solo los hits.
func MapToAlgoliaRelatedProductsResponse(
	hits []sharedModel.ProductInformation,
	queryParams string,
	page int,
	hitsPerPage int,
) model.AlgoliaRelatedProductsResponse {
	// Inicialmente, solo podemos llenar algunos campos de AlgoliaResult
	// basados en la información que tenemos directamente.
	// Si proxy_catalog_product.GetProductsInformationByQuery se modifica
	// para devolver la respuesta completa de Algolia, este mapper se actualizaría.

	algoliaResult := model.AlgoliaResult{
		Hits:             hits,
		Query:            queryParams, // Asumiendo que queryParams es el 'query' original
		Params:           queryParams, // Esto podría ser más detallado si tuviéramos todos los params de Algolia
		Page:             page,
		HitsPerPage:      hitsPerPage,
		NbHits:           len(hits), // Esto es solo el número de hits en la página actual, no el total de Algolia
		NbPages:          1,         // Asumimos 1 página por ahora, necesitaría nbHits total de Algolia
		// Otros campos como Facets, FacetsStats, QueryID, etc.,
		// necesitarían la respuesta completa de Algolia.
		// Los dejamos con sus valores cero/default por ahora.
		AppliedRules:             nil,
		AppliedRelevancyStrictness: 0,
		AroundLatLng:             "",
		AutomaticRadius:          "",
		ExhaustiveFacetsCount:    true, // Valor por defecto
		ExhaustiveNbHits:         true, // Valor por defecto
		Explain:                  nil,
		Extensions:               model.Extensions{},
		Facets:                   make(map[string]map[string]int),
		FacetsStats:              make(map[string]model.FacetStat),
		Index:                    "", // Necesitaría info de Algolia
		IndexUsed:                "", // Necesitaría info de Algolia
		Length:                   len(hits),
		Message:                  "",
		NbSortedHits:             0,
		Offset:                   page * hitsPerPage,
		ParsedQuery:              queryParams, // Asumiendo que queryParams es el 'query' original
		ProcessingTimeMS:         0, // Necesitaría info de Algolia
		QueryAfterRemoval:        "",
		QueryID:                  "", // Necesitaría info de Algolia
		ServerUsed:               "",
		TimeoutCounts:            false,
		TimeoutHits:              false,
		UserData:                 nil,
		ABTestVariantID:          0,
		ABTestID:                 0,
		RenderingContent:         make(map[string]interface{}),
	}

	return model.AlgoliaRelatedProductsResponse{
		Results: []model.AlgoliaResult{algoliaResult},
	}
}

// MapProductInformationToRelatedItem se mantiene por si se necesita internamente,
// pero la respuesta principal usará MapToAlgoliaRelatedProductsResponse.
// Esta función convierte un sharedModel.ProductInformation a model.RelatedItem.
func MapProductInformationToRelatedItem(product sharedModel.ProductInformation, storeGroupID int64) model.RelatedItem {
	return model.RelatedItem{
		MediaImageUrl:     product.MediaImageUrl,
		Description:       product.MediaDescription, // Usando MediaDescription como en best-seller
		FullPrice:         product.FullPrice,
		MediaDescription:  product.MediaDescription,
		Brand:             product.Brand, // Nota: ProductInformation tiene Brand y Marca. Usamos Brand consistentemente.
		Sales:             product.Sales,
		DetailDescription: product.GrayDescription,
		OfferPrice:        product.OfferPrice,
		OfferDescription:  product.OfferDescription,
		ID:                product.ID,
		OfferText:         product.OfferText,
		IdStoreGroup:      storeGroupID, // Este campo no está en ProductInformation, se pasa como arg.
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
		WithoutStock:      !product.HasStock, // Asumiendo que WithoutStock es la negación de HasStock
		URL:               product.URL,
	}
}
