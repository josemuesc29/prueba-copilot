package mappers

import (
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
)

func MapProductInformationToBestSellerDepartment(productInfo sharedModel.ProductInformation, storeGroupID int64) model.BestSellerDepartment {
	return model.BestSellerDepartment{
		MediaImageUrl:     productInfo.MediaImageUrl,
		Description:       productInfo.MediaDescription,
		FullPrice:         productInfo.FullPrice,
		MediaDescription:  productInfo.MediaDescription,
		Brand:             productInfo.Brand,
		Sales:             productInfo.Sales,
		DetailDescription: productInfo.GrayDescription,
		OfferPrice:        productInfo.OfferPrice,
		OfferDescription:  productInfo.OfferDescription,
		ID:                productInfo.ID,
		OfferText:         productInfo.OfferText,
		IdStoreGroup:      storeGroupID,
		Marca:             productInfo.Marca,
		ObjectID:          productInfo.ObjectID,
		OnlyOnline:        productInfo.OnlyOnline,
		DeliveryTime:      productInfo.DeliveryTime,
		Highlight:         productInfo.Highlight,
		Generic:           productInfo.Generics,
		LargeDescription:  productInfo.LargeDescription,
		AnywaySelling:     productInfo.AnywaySelling,
		Spaces:            productInfo.Spaces,
		Status:            productInfo.Status,
		TaxRate:           productInfo.TaxRate,
		ListUrlImages:     productInfo.ListUrlImages,
		MeasurePum:        productInfo.MeasurePum,
		LabelPum:          productInfo.LabelPum,
		HighlightsID:      productInfo.IDHighlights,
		SuggestedID:       productInfo.IDSuggested,
		Departments:       productInfo.Departments,
		SubCategory:       productInfo.SubCategory,
		Supplier:          productInfo.Supplier,
		Outofstore:        productInfo.Outofstore,
		OfferStartDate:    productInfo.OfferStartDate,
		OfferEndDate:      productInfo.OfferEndDate,
		PrimePrice:        productInfo.PrimePrice,
		PrimeTextDiscount: productInfo.PrimeTextDiscount,
		PrimeDescription:  productInfo.PrimeDescription,
		RmsClass:          productInfo.RmsClass,
		RmsDeparment:      productInfo.RmsDeparment,
		RmsGroup:          productInfo.RmsGroup,
		RmsSubclass:       productInfo.RmsSubclass,
		WithoutStock:      productInfo.HasStock,
		URL:               productInfo.URL,
	}
}
