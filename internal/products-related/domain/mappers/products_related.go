package mappers

import (
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
)

func MapProductInformationToRelatedItem(productInfo *sharedModel.ProductInformation) model.RelatedProductItem {
	if productInfo == nil {
		return model.RelatedProductItem{}
	}

	var storesWithStock []string
	for _, store := range productInfo.StoresWithStock {
		storesWithStock = append(storesWithStock, store.StoreID)
	}

	return model.RelatedProductItem{
		ID:               productInfo.ObjectID,
		Description:      productInfo.Description,
		MediaDescription: productInfo.MediaDescription,
		Brand:            productInfo.Brand,
		MediaImageURL:    productInfo.MediaImageURL,
		FullPrice:        productInfo.FullPrice,
		OfferPrice:       productInfo.OfferPrice,
		OfferDescription: productInfo.OfferDescription,
		OfferText:        productInfo.OfferText,
		OnlyOnline:       productInfo.OnlyOnline,
		DeliveryTime:     productInfo.DeliveryTime,
		URL:              productInfo.URL,
		HasStock:         productInfo.HasStock,
		StoresWithStock:  storesWithStock,
	}
}
