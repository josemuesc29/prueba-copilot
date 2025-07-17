package mappers

import (
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"strconv"
)

func MapProductInformationToSameBrandItem(product sharedModel.ProductInformation, totalStock int) model.SameBrandItem {
	return model.SameBrandItem{
		ID:                            product.ObjectID,
		MediaDescription:              product.MediaDescription,
		LargeDescription:              product.LargeDescription,
		MediaImageUrl:                 product.MediaImageUrl,
		FullPrice:                     product.FullPrice,
		Brand:                         product.Brand,
		OfferPrice:                    product.OfferPrice,
		OfferText:                     product.OfferText,
		RequirePrescription:           product.RequirePrescription,
		Sales:                         product.Sales,
		Spaces:                        product.Spaces,
		Status:                        product.Status,
		SubCategory:                   product.SubCategory,
		URL:                           product.URL,
		TotalStock:                    totalStock,
		CustomLabelForStockZero:       product.CustomLabelForStockZero,
		CustomLabelForStockZeroByCity: mapToCustomLabelForStockZeroByCity(product.CustomLabelForStockZeroByCity),
		FullPriceByCity:               mapToFullPriceByCity(product.FullPriceByCity),
		OfferPriceByCity:              product.OfferPriceByCity,
		StoresWithOffer:               product.StoresWithOffer,
		StoresWithStock:               product.StoresWithStock,
		BarcodeList:                   product.BarcodeList,
		OnlyOnline:                    product.OnlyOnline,
		GrayDescription:               product.GrayDescription,
		LabelPum:                      product.LabelPum,
		MeasurePum:                    product.MeasurePum,
		OfferDescription:              product.OfferDescription,
		OfferEndDate:                  product.OfferEndDate,
		OfferStartDate:                product.OfferStartDate,
		Prime:                         product.Prime,
		PrimeDescription:              product.PrimeDescription,
		PrimePrice:                    product.PrimePrice,
		PrimeTextDiscount:             product.PrimeTextDiscount,
		StoresWithPrimeOffer:          product.StoresWithPrimeOffer,
		TaxRate:                       product.TaxRate,
		CustomTag:                     product.CustomTag,
		ListUrlImages:                 product.ListUrlImages,
	}
}

func mapToCustomLabelForStockZeroByCity(byCity []sharedModel.CustomLabelForStockZeroByCity) []model.CustomLabelForStockZeroByCity {
	var result []model.CustomLabelForStockZeroByCity
	for _, c := range byCity {
		result = append(result, model.CustomLabelForStockZeroByCity{
			CityCode:    c.CityCode,
			CustomLabel: c.CustomLabel,
		})
	}
	return result
}

func mapToFullPriceByCity(byCity []sharedModel.FullPriceByCity) []model.FullPriceByCity {
	var result []model.FullPriceByCity
	for _, p := range byCity {
		result = append(result, model.FullPriceByCity{
			CityCode:  p.CityCode,
			FullPrice: p.FullPrice,
		})
	}
	return result
}

func convertIntArrayToStringArray(intArray []int) []string {
	stringArray := make([]string, len(intArray))
	for i, v := range intArray {
		stringArray[i] = strconv.Itoa(v)
	}
	return stringArray
}
