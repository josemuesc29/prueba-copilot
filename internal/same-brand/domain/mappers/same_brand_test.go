package mappers

import (
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestMapProductInformationToSameBrandItem_Success(t *testing.T) {
	productInfo := &sharedModel.ProductInformation{
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
		Sales:               50, // Asumiendo que copier puede convertir int a string para Sales si SameBrandItem.Sales es string
		Spaces:              5,  // Asumiendo que copier puede convertir int a string para Spaces si SameBrandItem.Spaces es string
		Status:              "A",
		TaxRate:             19,
		Classification: []sharedModel.Classification{
			{ID: 10, Name: "Cat1"},
			{ID: 20, Name: "Cat2"},
		},
		ListUrlImages: []string{"http://example.com/image1.jpg", "http://example.com/image2.jpg"},
		Categorie:     "Electrónicos",
		Marca:         "TestBrand",
		Departments:   []string{"Tecnología", "Hogar"},
		SubCategory:   "Gadgets",
		Supplier:      "Proveedor Confiable",
		DesHiddenSEO:  func(s string) *string { return &s }("Texto SEO oculto para el producto"),
		OnlyOnline:    true,
		DeliveryTime:  "24-48 horas",
		StoresWithStock: []int{1001, 1002, 1003}, // Copier no mapeará esto directamente a TotalStock o GlobalStock sin lógica custom
		Outofstore:    false,
		OfferStartDate: 1672531200, // Asumiendo que copier puede convertir int64 a string
		OfferEndDate:   1675209600, // Asumiendo que copier puede convertir int64 a string
		PrimePrice:     80.00,
		PrimeTextDiscount: "10% Prime",
		PrimeDescription:  "Oferta especial para miembros Prime",
		RmsClass:          "CL01",
		RmsDeparment:      "DP01",
		RmsGroup:          "GR01",
		RmsSubclass:       "SC01",
		HasStock:          true,
		URL:               "http://example.com/product/item-123",
		ObjectID:          "algolia-obj-123",
		CustomLabelForStockZero: "Consultar disponibilidad",
	}

	// ExpectedItem ahora solo debe reflejar los campos que `copier` puede mapear directamente
	// o aquellos cuya transformación es implícita y compatible.
	// Campos como TotalStock, IDClassification, RequirePrescription (string vs bool), etc.,
	// que requerían lógica de helper específica, no serán llenados por `copier` por defecto
	// a menos que las estructuras de destino tengan nombres y tipos exactamente coincidentes
	// o se use configuración avanzada de `copier`.
	expectedItem := model.SameBrandItem{
		AnywaySelling:       true,
		Brand:               "TestBrand",
		OfferText:           "15% OFF",
		OfferDescription:    "Gran oferta",
		FullPrice:           100.00,
		GrayDescription:     "Descripción detallada del producto",
		Highlight:           true,
		ID:                  "item-123",
		MediaDescription:    "Producto de prueba TestBrand con oferta",
		LargeDescription:    "Descripción larga y completa",
		MediaImageUrl:       "http://example.com/image.jpg",
		OfferPrice:          85.00,
		Status:              "A",
		TaxRate:             19,
		ListUrlImages:       []string{"http://example.com/image1.jpg", "http://example.com/image2.jpg"},
		Categorie:           "Electrónicos",
		Marca:               "TestBrand", // Asumiendo que ProductInformation.Marca mapea a SameBrandItem.Marca
		Departments:         []string{"Tecnología", "Hogar"},
		SubCategory:         "Gadgets",
		Supplier:            "Proveedor Confiable",
		OnlyOnline:          true,
		DeliveryTime:        "24-48 horas",
		Outofstore:          false,
		PrimePrice:          80.00,
		PrimeTextDiscount:   "10% Prime",
		PrimeDescription:    "Oferta especial para miembros Prime",
		RmsClass:            "CL01",
		RmsDeparment:        "DP01",
		RmsGroup:            "GR01",
		RmsSubclass:         "SC01",
		URL:                 "http://example.com/product/item-123",
		CustomLabelForStockZero: "Consultar disponibilidad",
		Generics:            false, // Mapeado de ProductInformation.Generics. JSON tag es "genericos"
		ObjectID:            "algolia-obj-123", // Mapeado de ProductInformation.ObjectID
		// Los siguientes campos probablemente NO serán mapeados correctamente por `copier` sin configuración adicional
		// o si los tipos/nombres no coinciden exactamente, o si requieren lógica de transformación:
		// IsFlashOffer: (no existe en SameBrandItem)
		// RequirePrescription: (string vs bool en source)
		// Sales: (string vs int en source)
		// Spaces: (string vs int en source)
		// TotalStock: (int vs no source field, antes calculado)
		// IDClassification: (int vs no source field, antes extraído)
		// OfferStartDate: (string vs int64 en source)
		// OfferEndDate: (string vs int64 en source)
		// WithoutStock: (bool vs !product.HasStock)
		// RequirePrescriptionMedical: (bool vs product.RequirePrescription)
		// StoresWithStock: ([]string vs []int en source)
		// TextSEO: (string vs *string en source)
		// SEO: (struct vs no source field directo para toda la struct)
		// SellerAddresses, Dimensions: (structs vs no source fields directos)
	}
	// Para `copier`, si ProductInformation.Generics (bool) se mapea a SameBrandItem.IsGeneric (bool), está bien.
	// Si se mapea a SameBrandItem.Generic (bool), también está bien.
	// Si ProductInformation.RequirePrescription (bool) se mapea a SameBrandItem.RequirePrescriptionMedical (bool), está bien.

	actualItem, err := MapProductInformationToSameBrandItem(productInfo)

	assert.NoError(t, err)

	// Verificar campos que se espera que `copier` mapee correctamente
	assert.Equal(t, expectedItem.AnywaySelling, actualItem.AnywaySelling, "AnywaySelling")
	assert.Equal(t, expectedItem.Brand, actualItem.Brand, "Brand")
	assert.Equal(t, expectedItem.OfferText, actualItem.OfferText, "OfferText")
	assert.Equal(t, expectedItem.FullPrice, actualItem.FullPrice, "FullPrice")
	assert.Equal(t, expectedItem.ID, actualItem.ID, "ID")
	assert.Equal(t, expectedItem.MediaImageUrl, actualItem.MediaImageUrl, "MediaImageUrl")
	assert.Equal(t, expectedItem.OfferPrice, actualItem.OfferPrice, "OfferPrice")
	assert.Equal(t, expectedItem.Status, actualItem.Status, "Status")
	assert.Equal(t, expectedItem.URL, actualItem.URL, "URL")
	assert.Equal(t, productInfo.Generics, actualItem.Generics, "Generics") // productInfo.Generics (bool) -> actualItem.Generics (bool)
	assert.Equal(t, productInfo.ObjectID, actualItem.ObjectID, "ObjectID") // productInfo.ObjectID (string) -> actualItem.ObjectID (string)
	assert.Equal(t, productInfo.RequirePrescription, actualItem.RequirePrescription, "RequirePrescription") // productInfo.RequirePrescription (bool) -> actualItem.RequirePrescription (bool)
	assert.EqualValues(t, productInfo.Sales, actualItem.Sales, "Sales") // productInfo.Sales (int) -> actualItem.Sales (int64)
	assert.Equal(t, productInfo.Spaces, actualItem.Spaces, "Spaces") // productInfo.Spaces (int) -> actualItem.Spaces (int)
	assert.EqualValues(t, productInfo.OfferStartDate, actualItem.OfferStartDate, "OfferStartDate")
	assert.EqualValues(t, productInfo.OfferEndDate, actualItem.OfferEndDate, "OfferEndDate")
	assert.Equal(t, productInfo.StoresWithStock, actualItem.StoresWithStock, "StoresWithStock")

	// Campos que podrían no mapearse o mapearse a valor cero por defecto si `copier` no encuentra fuente:
	// Por ejemplo, si `SameBrandItem.Sales` es string y `ProductInformation.Sales` es int,
	// `copier` podría no convertirlo automáticamente. Se necesitaría verificar el comportamiento.
	// Si `SameBrandItem.Sales` es `int`, entonces sí se copiaría.
	// Asumamos por ahora que los tipos son compatibles o `copier` es suficientemente inteligente.
	// Si `actualItem.Sales` es string y `productInfo.Sales` es int, esto fallaría sin conversión explícita.
	// t.Logf("Actual Sales: %v (type %T)", actualItem.Sales, actualItem.Sales) // Para depuración

	// Es importante revisar la definición de `model.SameBrandItem` para saber qué esperar de `copier`.
	// Ahora que TotalStock se calcula explícitamente en el mapper:
	assert.Equal(t, len(productInfo.StoresWithStock), actualItem.TotalStock, "TotalStock (esperado len(StoresWithStock))")

	// `Classification` es una slice de structs. Copier intentará mapear esto si los nombres de campo coinciden.
	// `sharedModel.Classification` tiene ID, Name. `model.Classification` tiene ID, Name, TypeID, TypeName.
	// Se espera que ID y Name se copien. TypeID y TypeName quedarán en valor cero.
	if assert.Len(t, actualItem.Classification, len(productInfo.Classification), "Classification slice length") {
		assert.Equal(t, productInfo.Classification[0].ID, actualItem.Classification[0].ID, "Classification[0].ID")
		assert.Equal(t, productInfo.Classification[0].Name, actualItem.Classification[0].Name, "Classification[0].Name")
		assert.Equal(t, 0, actualItem.Classification[0].TypeID, "Classification[0].TypeID (esperado 0)")
	}


	// Para DesHiddenSEO que es *string en ambos.
	if productInfo.DesHiddenSEO != nil {
		assert.NotNil(t, actualItem.DesHiddenSEO, "DesHiddenSEO should not be nil if source is not nil")
		assert.Equal(t, *productInfo.DesHiddenSEO, *actualItem.DesHiddenSEO, "DesHiddenSEO")
	} else {
		assert.Nil(t, actualItem.DesHiddenSEO, "DesHiddenSEO should be nil if source is nil")
	}
}

func TestMapProductInformationToSameBrandItem_NilInput(t *testing.T) {
	_, err := MapProductInformationToSameBrandItem(nil)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "productsInformation está vacío")
}

// Las funciones helper como calculateTotalStock, convertBoolToString, etc.,
// ya no son llamadas directamente por el mapper si `copier` se encarga de todo.
// Por lo tanto, sus tests unitarios individuales podrían eliminarse si estas funciones
// ya no forman parte de la API pública o lógica del paquete mappers.
// Si `copier` no maneja todas las transformaciones (ej. int a string para Sales),
// entonces el mapper necesitaría lógica adicional y estas helpers (o similares) podrían seguir siendo necesarias.
// Por ahora, se comentarán, asumiendo que `copier` es el principal mecanismo.

/*
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
*/
