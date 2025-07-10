package mappers

import (
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
)

func MapToAlgoliaRelatedProductsResponse(
	hits []sharedModel.ProductInformation,
	queryParams string,
	page int,
	hitsPerPage int,
) model.AlgoliaRelatedProductsResponse {

	algoliaResult := model.AlgoliaResult{
		Hits:                       hits,
		Query:                      queryParams,
		Params:                     queryParams,
		Page:                       page,
		HitsPerPage:                hitsPerPage,
		NbHits:                     len(hits),
		NbPages:                    1,
		AppliedRules:               nil,
		AppliedRelevancyStrictness: 0,
		AroundLatLng:               "",
		AutomaticRadius:            "",
		ExhaustiveFacetsCount:      true, // Valor por defecto
		ExhaustiveNbHits:           true, // Valor por defecto
		Explain:                    nil,
		Extensions:                 model.Extensions{},
		Facets:                     make(map[string]map[string]int),
		FacetsStats:                make(map[string]model.FacetStat),
		Index:                      "",
		IndexUsed:                  "",
		Length:                     len(hits),
		Message:                    "",
		NbSortedHits:               0,
		Offset:                     page * hitsPerPage,
		ParsedQuery:                queryParams,
		ProcessingTimeMS:           0,
		QueryAfterRemoval:          "",
		QueryID:                    "",
		ServerUsed:                 "",
		TimeoutCounts:              false,
		TimeoutHits:                false,
		UserData:                   nil,
		ABTestVariantID:            0,
		ABTestID:                   0,
		RenderingContent:           make(map[string]interface{}),
	}

	return model.AlgoliaRelatedProductsResponse{
		Results: []model.AlgoliaResult{algoliaResult},
	}
}

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
