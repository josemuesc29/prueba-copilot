package mappers

import (
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"strconv"
)

func MapProductInformationToSameBrandItem(product sharedModel.ProductInformation) model.SameBrandItem {
	return model.SameBrandItem{
		AnywaySelling:              product.AnywaySelling,
		Brand:                      product.Brand,
		OfferText:                  product.OfferText,
		OfferDescription:           product.OfferDescription,
		FullPrice:                  product.FullPrice,
		GrayDescription:            product.GrayDescription,
		Highlight:                  product.Highlight,
		ID:                         product.ID,
		IsGeneric:                  product.Generics,
		MediaDescription:           product.MediaDescription,
		LargeDescription:           product.LargeDescription,
		MediaImageUrl:              product.MediaImageUrl,
		OfferPrice:                 product.OfferPrice,
		Outstanding:                false, // No está en ProductInformation, valor por defecto
		RequirePrescription:        convertBoolToString(product.RequirePrescription),
		Sales:                      strconv.FormatInt(product.Sales, 10),
		Spaces:                     strconv.Itoa(product.Spaces),
		Status:                     product.Status,
		TaxRate:                    product.TaxRate,
		TotalStock:                 calculateTotalStock(product.StoresWithStock),
		QuantitySold:               0, // No está en ProductInformation, valor por defecto
		IDClassification:           getFirstClassificationID(product.Classification),
		ExpressWithSubscription:    false, // No está en ProductInformation, valor por defecto
		PosGroup:                   "100",
		ListUrlImages:              product.ListUrlImages,
		Categorie:                  product.Categorie,
		Marca:                      product.Marca,
		Departments:                product.Departments,
		SubCategory:                product.SubCategory,
		Supplier:                   product.Supplier,
		DeliveryPrice:              0,
		SEO:                        mapToSeoData(product), //Mientras se define como debe quedar
		TextSEO:                    getSEOText(product),   // Mientras se define como debe quedar
		OnlyOnline:                 product.OnlyOnline,
		DeliveryTime:               product.DeliveryTime,
		GlobalStock:                calculateTotalStock(product.StoresWithStock),
		Outofstore:                 product.Outofstore,
		IsFlashOffer:               false, // No está en ProductInformation, valor por defecto
		OfferStartDate:             strconv.FormatInt(product.OfferStartDate, 10),
		OfferEndDate:               strconv.FormatInt(product.OfferEndDate, 10),
		PrimePrice:                 product.PrimePrice,
		PrimeTextDiscount:          product.PrimeTextDiscount,
		PrimeDescription:           product.PrimeDescription,
		RmsClass:                   product.RmsClass,
		RmsDeparment:               product.RmsDeparment,
		RmsGroup:                   product.RmsGroup,
		RmsSubclass:                product.RmsSubclass,
		WithoutStock:               !product.HasStock,
		URL:                        product.URL,
		RequirePrescriptionMedical: product.RequirePrescription,
		SellerAddresses:            mapSellerAddresses(product.StoresWithStock),
		Dimensions:                 mapDimensions(),
		UuidItem:                   product.ObjectID,
		Warranty:                   "", // No está en ProductInformation
		WarrantyTerms:              "", // No está en ProductInformation
		CustomLabelForStockZero:    product.CustomLabelForStockZero,
		StoresWithStock:            convertIntArrayToStringArray(product.StoresWithStock),
		Generic:                    product.Generics,
	}
}

// Funciones auxiliares para el mapeo

func convertBoolToString(b bool) string {
	if b {
		return "true"
	}
	return "false"
}

func calculateTotalStock(stores []int) int {
	return len(stores) //Temporal mientras se define como deben de ir stock
}

func getFirstClassificationID(classifications []sharedModel.Classification) int {
	if len(classifications) > 0 {
		return classifications[0].ID
	}
	return 0
}

func mapToSeoData(product sharedModel.ProductInformation) model.SeoData {
	return model.SeoData{
		Name:        product.MediaDescription,
		Image:       product.MediaImageUrl,
		Description: product.GrayDescription,
		Sku:         product.ID,
		Context:     "http://schema.org/",
		Type:        "Product",
	}
}

func getSEOText(product sharedModel.ProductInformation) string {
	// Lógica para generar texto SEO basado en el producto
	if product.DesHiddenSEO != nil {
		return *product.DesHiddenSEO
	}
	return ""
}

func mapSellerAddresses(stores []int) []model.SellerAddress {
	// Mapeo simplificado - en un caso real, necesitarías obtener las direcciones reales
	addresses := make([]model.SellerAddress, 0)

	// Ejemplo con direcciones por defecto
	if len(stores) > 0 {
		addresses = append(addresses, model.SellerAddress{
			DaneCode: "11001000",
			Address:  "Carrera 47a 91 73",
		})
		addresses = append(addresses, model.SellerAddress{
			DaneCode: "11001000",
			Address:  "Calle 93 12 54",
		})
	}

	return addresses
}

func mapDimensions() model.Dimensions {
	// Dimensiones por defecto - en un caso real, estos datos vendrían del producto
	return model.Dimensions{
		Weight: "1.000000",
		Height: "10.000000",
		Width:  "10.000000",
		Length: "10.000000",
	}
}

func convertIntArrayToStringArray(intArray []int) []string {
	stringArray := make([]string, len(intArray))
	for i, v := range intArray {
		stringArray[i] = strconv.Itoa(v)
	}
	return stringArray
}
