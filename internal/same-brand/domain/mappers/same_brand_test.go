package mappers

import (
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestMapProductInformationToSameBrandItem(t *testing.T) {
	productInfo := sharedModel.ProductInformation{
		AnywaySelling:       true,
		Brand:               "TestBrand",
		OfferText:           "15% OFF",
		OfferDescription:    "Gran oferta",
		FullPrice:           100.00,
		GrayDescription:     "Descripción detallada del producto",
		Highlight:           true,
		ID:                  "item-123",
		Generics:            false,
		MediaDescription:    "Producto de prueba TestBrand con oferta",
		LargeDescription:    "Descripción larga y completa",
		MediaImageUrl:       "http://example.com/image.jpg",
		OfferPrice:          85.00,
		RequirePrescription: true,
		Sales:               50,
		Spaces:              5,
		Status:              "A",
		TaxRate:             19,
		Classification: []sharedModel.Classification{
			{ID: 10, Name: "Cat1"},
			{ID: 20, Name: "Cat2"},
		},
		ListUrlImages: []string{"http://example.com/image1.jpg", "http://example.com/image2.jpg"},
		Categorie:     "Electrónicos",
		Marca:         "TestBrand", // Similar a Brand, verificar si hay diferencia
		Departments:   []string{"Tecnología", "Hogar"},
		SubCategory:   "Gadgets",
		Supplier:      "Proveedor Confiable",

		DesHiddenSEO:    func(s string) *string { return &s }("Texto SEO oculto para el producto"), // Helper para puntero a string
		OnlyOnline:      true,
		DeliveryTime:    "24-48 horas",
		StoresWithStock: []int{1001, 1002, 1003},
		Outofstore:      false,

		OfferStartDate:          1672531200,
		OfferEndDate:            1675209600,
		PrimePrice:              80.00,
		PrimeTextDiscount:       "10% Prime",
		PrimeDescription:        "Oferta especial para miembros Prime",
		RmsClass:                "CL01",
		RmsDeparment:            "DP01",
		RmsGroup:                "GR01",
		RmsSubclass:             "SC01",
		HasStock:                true,
		URL:                     "http://example.com/product/item-123",
		ObjectID:                "algolia-obj-123",
		CustomLabelForStockZero: "Consultar disponibilidad",
	}

	expectedItem := model.SameBrandItem{
		AnywaySelling:           true,
		Brand:                   "TestBrand",
		OfferText:               "15% OFF",
		OfferDescription:        "Gran oferta",
		FullPrice:               100.00,
		GrayDescription:         "Descripción detallada del producto",
		Highlight:               true,
		ID:                      "item-123",
		IsGeneric:               false,
		MediaDescription:        "Producto de prueba TestBrand con oferta",
		LargeDescription:        "Descripción larga y completa",
		MediaImageUrl:           "http://example.com/image.jpg",
		OfferPrice:              85.00,
		Outstanding:             false,
		RequirePrescription:     "true",
		Sales:                   "50",
		Spaces:                  "5",
		Status:                  "A",
		TaxRate:                 19,
		TotalStock:              3, // calculateTotalStock de StoresWithStock
		QuantitySold:            0,
		IDClassification:        10, // getFirstClassificationID
		ExpressWithSubscription: false,
		PosGroup:                "100",
		ListUrlImages:           []string{"http://example.com/image1.jpg", "http://example.com/image2.jpg"},
		Categorie:               "Electrónicos",
		Marca:                   "TestBrand",
		Departments:             []string{"Tecnología", "Hogar"},
		SubCategory:             "Gadgets",
		Supplier:                "Proveedor Confiable",
		DeliveryPrice:           0,
		SEO: model.SeoData{ // mapToSeoData
			Name:        "Producto de prueba TestBrand con oferta", // product.MediaDescription
			Image:       "http://example.com/image.jpg",            // product.MediaImageUrl
			Description: "Descripción detallada del producto",      // product.GrayDescription
			Sku:         "item-123",                                // product.ID
			Context:     "http://schema.org/",
			Type:        "Product",
			// Offers dentro de SEO no se mapea explícitamente en mapToSeoData, verificar si es necesario
		},
		TextSEO:                    "Texto SEO oculto para el producto", // getSEOText
		OnlyOnline:                 true,
		DeliveryTime:               "24-48 horas",
		GlobalStock:                3, // calculateTotalStock de StoresWithStock
		Outofstore:                 false,
		IsFlashOffer:               false,
		OfferStartDate:             "1672531200", // strconv.FormatInt
		OfferEndDate:               "1675209600", // strconv.FormatInt
		PrimePrice:                 80.00,
		PrimeTextDiscount:          "10% Prime",
		PrimeDescription:           "Oferta especial para miembros Prime",
		RmsClass:                   "CL01",
		RmsDeparment:               "DP01",
		RmsGroup:                   "GR01",
		RmsSubclass:                "SC01",
		WithoutStock:               false, // !product.HasStock
		URL:                        "http://example.com/product/item-123",
		RequirePrescriptionMedical: true, // product.RequirePrescription
		SellerAddresses: []model.SellerAddress{ // mapSellerAddresses (ejemplo con valores por defecto si hay stores)
			{DaneCode: "11001000", Address: "Carrera 47a 91 73"},
			{DaneCode: "11001000", Address: "Calle 93 12 54"},
		},
		Dimensions: model.Dimensions{ // mapDimensions (valores por defecto)
			Weight: "1.000000", Height: "10.000000", Width: "10.000000", Length: "10.000000",
		},
		UuidItem:                "algolia-obj-123", // product.ObjectID
		Warranty:                "",
		WarrantyTerms:           "",
		CustomLabelForStockZero: "Consultar disponibilidad",
		StoresWithStock:         []string{"1001", "1002", "1003"}, // convertIntArrayToStringArray
		Generic:                 false,                            // product.Generics
	}

	actualItem := MapProductInformationToSameBrandItem(productInfo)

	// Comparar campo por campo puede ser más fácil para debuggear que la struct completa
	assert.Equal(t, expectedItem.AnywaySelling, actualItem.AnywaySelling, "AnywaySelling")
	assert.Equal(t, expectedItem.Brand, actualItem.Brand, "Brand")
	assert.Equal(t, expectedItem.OfferText, actualItem.OfferText, "OfferText")
	// ... (todos los demás campos)

	// Para una comparación completa:
	assert.Equal(t, expectedItem, actualItem)
}

// Podrías añadir tests específicos para las funciones helper si fueran más complejas
// o si necesitaran ser probadas con más casos de borde de forma aislada.
// Por ejemplo:
func TestCalculateTotalStock(t *testing.T) {
	assert.Equal(t, 0, calculateTotalStock([]int{}), "Empty slice should be 0")
	assert.Equal(t, 3, calculateTotalStock([]int{1, 2, 3}), "Slice with 3 elements")
}

func TestConvertBoolToString(t *testing.T) {
	assert.Equal(t, "true", convertBoolToString(true), "True to string")
	assert.Equal(t, "false", convertBoolToString(false), "False to string")
}

func TestGetFirstClassificationID(t *testing.T) {
	assert.Equal(t, 0, getFirstClassificationID([]sharedModel.Classification{}), "Empty classifications")
	assert.Equal(t, 123, getFirstClassificationID([]sharedModel.Classification{{ID: 123}}), "Single classification")
	assert.Equal(t, 123, getFirstClassificationID([]sharedModel.Classification{{ID: 123}, {ID: 456}}), "Multiple classifications, get first")
}

func TestConvertIntArrayToStringArray(t *testing.T) {
	assert.Equal(t, []string{}, convertIntArrayToStringArray([]int{}), "Empty int array")
	assert.Equal(t, []string{"1", "2", "3"}, convertIntArrayToStringArray([]int{1, 2, 3}), "Populated int array")
}
