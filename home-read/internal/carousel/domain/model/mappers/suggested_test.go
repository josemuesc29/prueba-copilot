package mappers

import (
	"reflect"
	"testing"

	"ftd-td-home-read-services/internal/carousel/domain/model"
	sharedModel "ftd-td-home-read-services/internal/shared/domain/model"
)

func TestSuggestedCMSMapToSuggested(t *testing.T) {
	sugestedData := model.SuggestedData{
		ID:             123,
		Type:           "producto",
		Position:       1,
		OrderingNumber: 10,
		Action:         "ver",
		DocumentID:     "DOC456",
		Sku:            "SKU789",
		CreatedAt:      "2024-06-01T12:00:00Z",
		UpdatedAt:      "2024-06-02T12:00:00Z",
		PublishedAt:    "2024-06-03T12:00:00Z",
		Locale:         "es-CL",
		Name:           "Producto de prueba",
	}

	productInfo := sharedModel.ProductInformation{
		Prime:                "Sí",
		RMSProvider:          "ProveedorX",
		Collections:          []string{"Colección1", "Colección2"},
		AnywaySelling:        true,
		Barcode:              "1234567890123",
		BarcodeList:          []string{"1234567890123", "9876543210987"},
		Brand:                "MarcaX",
		Categorie:            "CategoríaX",
		CustomTag:            func() *string { s := "Etiqueta especial"; return &s }(),
		DeliveryTime:         "24h",
		Departments:          []string{"Dept1", "Dept2"},
		DesHiddenSEO:         func() *string { s := "SEO oculto"; return &s }(),
		FullPrice:            19990.0,
		Generics:             true,
		GrayDescription:      "Descripción gris",
		HasStock:             true,
		Highlight:            false,
		ID:                   "PROD123",
		IDOffersGroup:        []int{1, 2},
		IDHighlights:         []int{3, 4},
		IDSuggested:          []int{5, 6},
		IsCupon:              0,
		LabelPum:             "Unidades a $",
		LargeDescription:     "Descripción larga del producto",
		ListUrlImages:        []string{"https://ejemplo.com/img1.jpg", "https://ejemplo.com/img2.jpg"},
		Marca:                "MarcaX",
		MeasurePum:           1.5,
		MediaDescription:     "Descripción media",
		MediaImageUrl:        "https://ejemplo.com/imagen.jpg",
		MetadesSEO:           func() *string { s := "Meta descripción SEO"; return &s }(),
		MetatituloSEO:        func() *string { s := "Meta título SEO"; return &s }(),
		ObjectID:             "OBJ123",
		OfferDescription:     "Oferta especial",
		OfferEndDate:         1710003600,
		OfferPrice:           15990.0,
		OfferPriceByCity:     []interface{}{"Oferta ciudad"},
		OfferStartDate:       1710000000,
		OfferText:            "20% OFF",
		OnlyOnline:           true,
		Outofstore:           false,
		PrimeDescription:     "Prime exclusivo",
		PrimePrice:           14990.0,
		PrimeTextDiscount:    "10% Prime",
		RequirePrescription:  false,
		RmsClass:             "ClaseX",
		RmsDeparment:         "DeptX",
		RmsGroup:             "GrupoX",
		RmsSubclass:          "SubclaseX",
		Sales:                100,
		Spaces:               5,
		Status:               "A",
		StoresWithOffer:      []int{1, 2},
		StoresWithPrimeOffer: []interface{}{"Prime1"},
		StoresWithStock:      []int{1, 2, 3},
		SubCategory:          "SubCatX",
		Supplier:             "ProveedorX",
		TaxRate:              19,
		URL:                  "producto-x",
	}

	expected := model.Suggested{
		ID:               123,
		FirstDescription: "desc",
		OfferText:        "oferta",
		Type:             "tipo",
		UrlImage:         "img",
		OrderingNumber:   1,
		Action:           "action",
		Position:         1,
		Product: model.Product{
			ID:               "prod1",
			MediaImageUrl:    "img-url",
			Description:      "desc",
			FullPrice:        100,
			MediaDescription: "media",
		},
	}

	result := SuggestedCMSMapToSuggested(sugestedData, productInfo, 1)

	if !reflect.DeepEqual(result.ID, expected.ID) {
		t.Errorf("esperado %+v, obtenido %+v", expected, result)
	}
}
