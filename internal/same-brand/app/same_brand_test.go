package app

import (
	"context"
	"errors"
	"fmt"
	"testing"
	"time"

	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	mock_ports "ftd-td-catalog-item-read-services/test/mocks/shared/domain/ports/out" // Asegúrate que esta ruta sea correcta

	"github.com/gin-gonic/gin"
	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
)

func TestSameBrand_GetItemsBySameBrand_Success_FromAlgolia(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_ports.NewMockCache(ctrl)
	mockOffer := mock_ports.NewMockOfferOutPort(ctrl) // Si OfferOutPort es usado

	// Cargar configuraciones de entorno para TTL (si es necesario directamente en el test)
	config.Enviroments.RedisSameBrandTTL = 60 // Ejemplo de TTL en minutos

	s := NewSameBrand(mockCatalogProduct, mockCache, mockOffer)

	ctx := &gin.Context{} // Usar un contexto real o un mock si es necesario para headers
	countryID := "CO"
	itemID := "123"
	brandName := "TestBrand"

	// 1. Cache miss
	mockCache.EXPECT().Get(gomock.Any(), fmt.Sprintf(keySameBrandCache, countryID, itemID)).Return("", nil)

	// 2. Get original item's brand
	originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: brandName}
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)

	// 3. Get brand items from Algolia
	algoliaProducts := []sharedModel.ProductInformation{
		{ObjectID: "456", Brand: brandName, GlobalStock: 10, Status: "A", HasStock: true},
		{ObjectID: "789", Brand: brandName, GlobalStock: 5, Status: "A", HasStock: true},
		{ObjectID: "101", Brand: brandName, GlobalStock: 20, Status: "A", HasStock: true}, // Este será el primero después de ordenar
	}
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), fmt.Sprintf("brand:\"%s\"", brandName), countryID).Return(algoliaProducts, nil)

	// (Opcional) Si ApplyOffers es llamado explícitamente:
	// mockOffer.EXPECT().ApplyOffers(gomock.Any(), gomock.Any(), countryID).Return(transformedItems, nil)


	// 4. Cache set
	// El orden esperado después del sort por stock y mapeo
	expectedResultForCache := []model.SameBrandItem{
		{ID: "101", Brand: brandName, TotalStock: 20, Status: "A"}, // Simplificado
		{ID: "456", Brand: brandName, TotalStock: 10, Status: "A"},
		{ID: "789", Brand: brandName, TotalStock: 5, Status: "A"},
	}
	// La expectativa de Set debe coincidir con lo que se guardaría. El matcher gomock.Any() para el valor es más simple aquí.
	mockCache.EXPECT().Set(gomock.Any(), fmt.Sprintf(keySameBrandCache, countryID, itemID), gomock.Any(), time.Duration(config.Enviroments.RedisSameBrandTTL)*time.Minute).Return(nil)

	items, err := s.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.NoError(t, err)
	assert.NotNil(t, items)
	assert.Len(t, items, 3)
	assert.Equal(t, "101", items[0].ID) // Verificando el orden por stock
	assert.Equal(t, "456", items[1].ID)
	assert.Equal(t, "789", items[2].ID)
}

func TestSameBrand_GetItemsBySameBrand_Success_FromCache(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_ports.NewMockCache(ctrl)
	mockOffer := mock_ports.NewMockOfferOutPort(ctrl)

	s := NewSameBrand(mockCatalogProduct, mockCache, mockOffer)
	ctx := &gin.Context{}
	countryID := "CO"
	itemID := "123"

	cachedItems := []model.SameBrandItem{
		{ID: "cached1", Brand: "TestBrand"},
	}
	jsonData, _ := model.MarshalSameBrandItemSlice(cachedItems) // Necesitarás un marshaller o usa json.Marshal

	mockCache.EXPECT().Get(gomock.Any(), fmt.Sprintf(keySameBrandCache, countryID, itemID)).Return(string(jsonData), nil)

	items, err := s.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.NoError(t, err)
	assert.NotNil(t, items)
	assert.Equal(t, cachedItems, items)
}


func TestSameBrand_GetItemsBySameBrand_OriginalItemNotFound(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_ports.NewMockCache(ctrl)
	mockOffer := mock_ports.NewMockOfferOutPort(ctrl)
	s := NewSameBrand(mockCatalogProduct, mockCache, mockOffer)
	ctx := &gin.Context{}
	countryID := "CO"
	itemID := "123"

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil) // Cache miss
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{}, nil) // Vacío

	items, err := s.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.Error(t, err) // El error original es "item not found"
	assert.Nil(t, items)
    assert.Contains(t, err.Error(), "item not found")
}

func TestSameBrand_GetItemsBySameBrand_OriginalItemBrandEmpty(t *testing.T) {
    ctrl := gomock.NewController(t)
    defer ctrl.Finish()

    mockCatalogProduct := mock_ports.NewMockCatalogProduct(ctrl)
    mockCache := mock_ports.NewMockCache(ctrl)
    mockOffer := mock_ports.NewMockOfferOutPort(ctrl)
    s := NewSameBrand(mockCatalogProduct, mockCache, mockOffer)

    ctx := &gin.Context{}
    countryID := "CO"
    itemID := "123"

    mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil) // Cache miss
    originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: ""} // Marca vacía
    mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)

    items, err := s.GetItemsBySameBrand(ctx, countryID, itemID)

    assert.NoError(t, err) // Ahora devuelve lista vacía, no error
    assert.NotNil(t, items)
    assert.Len(t, items, 0)
}


func TestSameBrand_GetItemsBySameBrand_AlgoliaError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_ports.NewMockCache(ctrl)
	mockOffer := mock_ports.NewMockOfferOutPort(ctrl)
	s := NewSameBrand(mockCatalogProduct, mockCache, mockOffer)
	ctx := &gin.Context{}
	countryID := "CO"
	itemID := "123"
	brandName := "TestBrand"

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil) // Cache miss
	originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: brandName}
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), fmt.Sprintf("brand:\"%s\"", brandName), countryID).Return(nil, errors.New("algolia down"))

	items, err := s.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.Error(t, err)
	assert.Nil(t, items)
	assert.Contains(t, err.Error(), "algolia down")
}

func TestSameBrand_GetItemsBySameBrand_RespectsMaxLimit(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockCatalogProduct := mock_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_ports.NewMockCache(ctrl)
	mockOffer := mock_ports.NewMockOfferOutPort(ctrl)
	s := NewSameBrand(mockCatalogProduct, mockCache, mockOffer)
	ctx := &gin.Context{}
	countryID := "CO"
	itemID := "originalItem"
	brandName := "LimitBrand"

	config.Enviroments.RedisSameBrandTTL = 10

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{{ObjectID: itemID, Brand: brandName}}, nil)

	var algoliaProducts []sharedModel.ProductInformation
	for i := 0; i < maxItemsLimit+5; i++ { // Generar más items que el límite
		algoliaProducts = append(algoliaProducts, sharedModel.ProductInformation{
			ObjectID:    fmt.Sprintf("item%d", i),
			Brand:       brandName,
			GlobalStock: int64(i + 1), // Stocks diferentes para probar ordenamiento
			Status:      "A",
			HasStock:    true,
		})
	}
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), gomock.Any(), countryID).Return(algoliaProducts, nil)
	mockCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)

	items, err := s.GetItemsBySameBrand(ctx, countryID, itemID)
	assert.NoError(t, err)
	assert.Len(t, items, maxItemsLimit)
	// Verificar que los items con mayor stock están primero
	assert.Equal(t, fmt.Sprintf("item%d", maxItemsLimit+4), items[0].ID) // El de mayor stock
}


func TestSameBrand_ShouldIncludeProduct(t *testing.T) {
	s := &sameBrand{} // No necesita mocks para esta prueba unitaria directa

	testCases := []struct {
		name     string
		product  sharedModel.ProductInformation
		expected bool
	}{
		{"Active with stock", sharedModel.ProductInformation{Status: "A", HasStock: true, GlobalStock: 1}, true},
		{"Inactive with stock", sharedModel.ProductInformation{Status: "I", HasStock: true, GlobalStock: 1}, false},
		{"Active no stock (HasStock false)", sharedModel.ProductInformation{Status: "A", HasStock: false, GlobalStock: 1}, false},
		{"Active no stock (GlobalStock zero)", sharedModel.ProductInformation{Status: "A", HasStock: true, GlobalStock: 0}, false},
		{"Active no stock (both zero/false)", sharedModel.ProductInformation{Status: "A", HasStock: false, GlobalStock: 0}, false},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			assert.Equal(t, tc.expected, s.shouldIncludeProduct(tc.product))
		})
	}
}

// Helper para mockCache.Get que espera JSON de []model.SameBrandItem
// No es directamente un test, sino un helper para el test TestSameBrand_GetItemsBySameBrand_Success_FromCache
// Necesitarás un método para serializar []model.SameBrandItem a JSON string para el mock de Get.
// Ejemplo (simplificado, json.Marshal directamente en el test es más fácil):
func marshalSameBrandItems(items []model.SameBrandItem) string {
	// En una situación real, esto podría estar en el propio paquete model o ser más robusto.
	// Aquí, asumimos que model.SameBrandItem es serializable.
	// Por simplicidad, este helper no está completo para evitar dependencia directa de json.Marshal aquí.
	// En el test TestSameBrand_GetItemsBySameBrand_Success_FromCache, se usa:
	// jsonData, _ := model.MarshalSameBrandItemSlice(cachedItems)
	// donde MarshalSameBrandItemSlice sería:
	/*
	   package model
	   import "encoding/json"
	   func MarshalSameBrandItemSlice(items []SameBrandItem) ([]byte, error) {
	       return json.Marshal(items)
	   }
	   func UnmarshalSameBrandItemSlice(data []byte) ([]SameBrandItem, error) {
	       var items []SameBrandItem
	       err := json.Unmarshal(data, &items)
	       return items, err
	   }
	*/
	return ""
}

// Nota: Para probar la propagación de X-Custom-City, necesitarías:
// 1. Un gin.Context con el header seteado.
// 2. En las expectativas de mockCatalogProduct.GetProductsInformationByObjectID y .GetProductsInformationByQuery,
//    usar un gomock.Matcher para verificar que el gin.Context pasado contiene el header esperado.
// Ejemplo de matcher:
/*
type contextWithHeaderMatcher struct {
    headerName  string
    headerValue string
}

func (m *contextWithHeaderMatcher) Matches(x interface{}) bool {
    if ctx, ok := x.(*gin.Context); ok {
        // Necesitas mockear el request dentro del gin.Context para que GetHeader funcione en un test
        // o usar ctx.Value si es como se pasa internamente.
        // Esto es complejo de mockear sin un request real.
        // Una forma más simple es verificar que el método PropagateHeader se llama con el contexto correcto si lo expones
        // o confiar en tests de integración.
        // Por ahora, asumimos que el adaptador de Algolia usa GetHeader directamente.
        return true // Simplificación para este ejemplo
    }
    return false
}

func (m *contextWithHeaderMatcher) String() string {
    return fmt.Sprintf("is a gin.Context with header %s=%s", m.headerName, m.headerValue)
}

func ContextWithHeader(name, value string) gomock.Matcher {
    return &contextWithHeaderMatcher{name, value}
}

// Luego en el test:
// mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(ContextWithHeader("X-Custom-City", "TestCity"), ...)
*/

// Se necesitará añadir `MarshalSameBrandItemSlice` al paquete `model`
// o usar `json.Marshal` directamente en el test `TestSameBrand_GetItemsBySameBrand_Success_FromCache`.
// Para `model.MarshalSameBrandItemSlice`:
// ```go
// package model
// import "encoding/json"
// func MarshalSameBrandItemSlice(items []SameBrandItem) ([]byte, error) {
// 	return json.Marshal(items)
// }
// ```
// Esto es solo para que el código del test compile.

// Para el mock de sharedModel.OfferOutPort, si es necesario, su generación debe estar configurada.
// Si no existe, el `mock_ports.NewMockOfferOutPort(ctrl)` fallará.
// Si `outPortOffer` no se usa activamente (ej. ApplyOffers no se llama), puedes pasar `nil`
// o un mock que no espere llamadas.

func init() {
	// Silenciar logs de Gin durante los tests si es necesario
	// gin.SetMode(gin.TestMode)
}

```

**Nota:** El `mock_ports.NewMockOfferOutPort(ctrl)` asumirá que tienes un mock generado para `OfferOutPort`. Si no es así, o si `outPortOffer` no se usa activamente en los flujos probados (por ejemplo, si `ApplyOffers` no se llama), puedes pasar `nil` al constructor `NewSameBrand` para ese parámetro en los tests o asegurarte de que el mock exista.

Crearé un archivo `internal/same-brand/domain/model/same_brand_ καλύπτουν.go` para el helper `MarshalSameBrandItemSlice` (usando un nombre de archivo diferente para evitar conflictos si ya existe uno para el modelo). En realidad, esto debería estar en `same_brand.go` o en un archivo `mappers.go` dentro del paquete del modelo si se usa más ampliamente, pero para el test, esto es suficiente.

No, es mejor añadirlo directamente al `model.SameBrandItem` o un archivo `json_helpers.go` en el paquete `model`. Por ahora, asumiré que `json.Marshal` se puede usar directamente en el test como se hizo en el código de ejemplo para `TestSameBrand_GetItemsBySameBrand_Success_FromCache`.

El código del test de arriba es un buen comienzo. Cubre varios escenarios. Haré un pequeño ajuste para usar `json.Marshal` directamente en el test de caché para simplificar.

```go
package app

import (
	"context"
	"encoding/json" // Añadido para el test de caché
	"errors"
	"fmt"
	"testing"
	"time"

	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	inPorts "ftd-td-catalog-item-read-services/internal/same-brand/domain/ports/in" // Importar inPorts
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	mock_shared_ports "ftd-td-catalog-item-read-services/test/mocks/shared/domain/ports/out" // Renombrado para claridad

	"github.com/gin-gonic/gin"
	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
)

// Helper para crear un gin.Context con headers para tests
func ginContextWithHeaders(headers map[string]string) *gin.Context {
	c, _ := gin.CreateTestContext(nil)
	// Para que GetHeader funcione, el request debe tener los headers.
	// Esto es complicado de mockear directamente en gin.Context sin un http.Request real.
	// Una alternativa es pasar valores a través de c.Set() y que la app los lea con c.Value()
	// o que el adaptador de Algolia sea mockeado para no depender del header en el context para el test unitario,
	// y probar la propagación del header en un test de integración.
	// Por ahora, para los tests unitarios de la lógica de app, asumiremos que el contexto se pasa correctamente
	// y el adaptador se encarga de leerlo.
	// Si necesitamos verificar que app llama a utils.PropagateHeader, eso es más fácil.
	// Para este test, un contexto vacío es suficiente si no probamos la lógica de header *dentro* de app.
	// La lógica de header ya está en app con utils.PropagateHeader.
	// El test de app debe verificar que se llame a los puertos con el contexto.
	return c
}


func TestSameBrand_GetItemsBySameBrand_Success_FromAlgolia(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_shared_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl)

	config.Enviroments.RedisSameBrandTTL = 60

	var appService inPorts.SameBrand // Usar el tipo de interfaz
	appService = NewSameBrand(mockCatalogProduct, mockCache, mockOffer)


	// gin.SetMode(gin.TestMode) // Para evitar logs de Gin
	// ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	// ctx.Request, _ = http.NewRequest("GET", "/", nil) // Necesario para GetHeader
	// ctx.Request.Header.Set(enums.HeaderXCustomCity, "some-city")
	// ctx.Set(enums.HeaderCorrelationID, "test-correl-id")
    ctx := ginContextWithHeaders(map[string]string{enums.HeaderXCustomCity: "some-city", enums.HeaderCorrelationID: "test-correl-id"})


	countryID := "CO"
	itemID := "123"
	brandName := "TestBrand"

	mockCache.EXPECT().Get(gomock.Any(), fmt.Sprintf(keySameBrandCache, countryID, itemID)).Return("", nil)

	originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: brandName, Status: "A", HasStock: true, GlobalStock: 1}
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)

	algoliaProducts := []sharedModel.ProductInformation{
		{ObjectID: "456", Brand: brandName, GlobalStock: 10, Status: "A", HasStock: true},
		{ObjectID: "789", Brand: brandName, GlobalStock: 5, Status: "A", HasStock: true},
		{ObjectID: "101", Brand: brandName, GlobalStock: 20, Status: "A", HasStock: true},
	}
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), fmt.Sprintf("brand:\"%s\"", brandName), countryID).Return(algoliaProducts, nil)

	mockCache.EXPECT().Set(gomock.Any(), fmt.Sprintf(keySameBrandCache, countryID, itemID), gomock.Any(), time.Duration(config.Enviroments.RedisSameBrandTTL)*time.Minute).Return(nil)

	items, err := appService.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.NoError(t, err)
	assert.NotNil(t, items)
	assert.Len(t, items, 3)
	assert.Equal(t, "101", items[0].ID)
	assert.Equal(t, "456", items[1].ID)
	assert.Equal(t, "789", items[2].ID)
}

func TestSameBrand_GetItemsBySameBrand_Success_FromCache(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_shared_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl)

	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache, mockOffer)

	ctx := ginContextWithHeaders(map[string]string{enums.HeaderCorrelationID: "test-correl-id"})
	countryID := "CO"
	itemID := "123"

	cachedItems := []model.SameBrandItem{
		{ID: "cached1", Brand: "TestBrand", Status: "A"},
	}
	// jsonData, _ := model.MarshalSameBrandItemSlice(cachedItems) // Si tuvieras el helper
	jsonData, _ := json.Marshal(cachedItems) // Usar json.Marshal directamente

	mockCache.EXPECT().Get(gomock.Any(), fmt.Sprintf(keySameBrandCache, countryID, itemID)).Return(string(jsonData), nil)

	items, err := appService.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.NoError(t, err)
	assert.NotNil(t, items)
	assert.Equal(t, cachedItems, items)
}


func TestSameBrand_GetItemsBySameBrand_OriginalItemNotFound(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_shared_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl)
	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache, mockOffer)
	ctx := ginContextWithHeaders(map[string]string{enums.HeaderCorrelationID: "test-correl-id"})
	countryID := "CO"
	itemID := "123"

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{}, nil)

	items, err := appService.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.Error(t, err)
	assert.Nil(t, items)
    assert.Contains(t, err.Error(), "item not found")
}

func TestSameBrand_GetItemsBySameBrand_OriginalItemBrandEmpty(t *testing.T) {
    ctrl := gomock.NewController(t)
    defer ctrl.Finish()

    mockCatalogProduct := mock_shared_ports.NewMockCatalogProduct(ctrl)
    mockCache := mock_shared_ports.NewMockCache(ctrl)
    mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl)
    var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache, mockOffer)

    ctx := ginContextWithHeaders(map[string]string{enums.HeaderCorrelationID: "test-correl-id"})
    countryID := "CO"
    itemID := "123"

    mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
    originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: "", Status: "A", HasStock: true, GlobalStock: 1}
    mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)

    items, err := appService.GetItemsBySameBrand(ctx, countryID, itemID)

    assert.NoError(t, err)
    assert.NotNil(t, items)
    assert.Len(t, items, 0)
}


func TestSameBrand_GetItemsBySameBrand_AlgoliaError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_shared_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl)
	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache, mockOffer)
	ctx := ginContextWithHeaders(map[string]string{enums.HeaderCorrelationID: "test-correl-id"})
	countryID := "CO"
	itemID := "123"
	brandName := "TestBrand"

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: brandName, Status: "A", HasStock: true, GlobalStock: 1}
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), fmt.Sprintf("brand:\"%s\"", brandName), countryID).Return(nil, errors.New("algolia down"))

	items, err := appService.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.Error(t, err)
	assert.Nil(t, items)
	assert.Contains(t, err.Error(), "algolia down")
}

func TestSameBrand_GetItemsBySameBrand_RespectsMaxLimitAndOrder(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockCatalogProduct := mock_shared_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl)
	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache, mockOffer)
	ctx := ginContextWithHeaders(map[string]string{enums.HeaderCorrelationID: "test-correl-id"})
	countryID := "CO"
	itemID := "originalItem"
	brandName := "LimitBrand"

	config.Enviroments.RedisSameBrandTTL = 10

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{{ObjectID: itemID, Brand: brandName, Status: "A", HasStock: true, GlobalStock:1}}, nil)

	var algoliaProducts []sharedModel.ProductInformation
	totalProductsFromAlgolia := maxItemsLimit + 5
	for i := 0; i < totalProductsFromAlgolia; i++ {
		algoliaProducts = append(algoliaProducts, sharedModel.ProductInformation{
			ObjectID:    fmt.Sprintf("item%d", i),
			Brand:       brandName,
			GlobalStock: int64(i + 1),
			Status:      "A",
			HasStock:    true,
		})
	}
	// Los productos de Algolia están ordenados por stock ascendente (item0 stock 1, item1 stock 2, ...)
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), gomock.Any(), countryID).Return(algoliaProducts, nil)
	mockCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)

	items, err := appService.GetItemsBySameBrand(ctx, countryID, itemID)
	assert.NoError(t, err)
	assert.Len(t, items, maxItemsLimit)
	// Después del sort en la app (descendente), el item con mayor stock debe ser el primero.
	// El item con mayor stock es "item(totalProductsFromAlgolia-1)" que tiene stock totalProductsFromAlgolia.
	assert.Equal(t, fmt.Sprintf("item%d", totalProductsFromAlgolia-1), items[0].ID)
	assert.Equal(t, int(totalProductsFromAlgolia), items[0].TotalStock) // Asumiendo que el mapper copia GlobalStock a TotalStock

	// El último item en la lista de maxItemsLimit debe ser el que tiene el (maxItemsLimit)-ésimo stock más alto
	// algoliaProducts[totalProductsFromAlgolia-maxItemsLimit].ObjectID
	assert.Equal(t, algoliaProducts[totalProductsFromAlgolia-maxItemsLimit].ObjectID, items[maxItemsLimit-1].ID)
}


func TestSameBrand_ShouldIncludeProduct(t *testing.T) {
	// s := &sameBrand{} // No necesita mocks para esta prueba unitaria directa
	// Crear una instancia real, ya que no tiene dependencias de puerto para este método.
	s := NewSameBrand(nil, nil, nil).(*sameBrand)


	testCases := []struct {
		name     string
		product  sharedModel.ProductInformation
		expected bool
	}{
		{"Active with stock", sharedModel.ProductInformation{Status: "A", HasStock: true, GlobalStock: 1}, true},
		{"Inactive with stock", sharedModel.ProductInformation{Status: "I", HasStock: true, GlobalStock: 1}, false},
		{"Active no stock (HasStock false)", sharedModel.ProductInformation{Status: "A", HasStock: false, GlobalStock: 1}, false},
		{"Active no stock (GlobalStock zero)", sharedModel.ProductInformation{Status: "A", HasStock: true, GlobalStock: 0}, false},
		{"Active no stock (both zero/false)", sharedModel.ProductInformation{Status: "A", HasStock: false, GlobalStock: 0}, false},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			assert.Equal(t, tc.expected, s.shouldIncludeProduct(tc.product))
		})
	}
}

func TestSameBrand_GetItemsBySameBrand_CacheSetError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_shared_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl)

	config.Enviroments.RedisSameBrandTTL = 60
	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache, mockOffer)
    ctx := ginContextWithHeaders(map[string]string{enums.HeaderCorrelationID: "test-correl-id"})
	countryID := "CO"
	itemID := "123"
	brandName := "TestBrand"

	mockCache.EXPECT().Get(gomock.Any(), fmt.Sprintf(keySameBrandCache, countryID, itemID)).Return("", nil)
	originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: brandName, Status: "A", HasStock: true, GlobalStock: 1}
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)
	algoliaProducts := []sharedModel.ProductInformation{
		{ObjectID: "456", Brand: brandName, GlobalStock: 10, Status: "A", HasStock: true},
	}
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), fmt.Sprintf("brand:\"%s\"", brandName), countryID).Return(algoliaProducts, nil)

	// Simular error al guardar en cache
	mockCache.EXPECT().Set(gomock.Any(), fmt.Sprintf(keySameBrandCache, countryID, itemID), gomock.Any(), gomock.Any()).Return(errors.New("cache set failed"))

	items, err := appService.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.NoError(t, err) // El error de cache set no debe propagarse al cliente
	assert.NotNil(t, items)
	assert.Len(t, items, 1)
}

func TestSameBrand_GetItemsBySameBrand_CacheGetError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_shared_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl)

	config.Enviroments.RedisSameBrandTTL = 60
	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache, mockOffer)
    ctx := ginContextWithHeaders(map[string]string{enums.HeaderCorrelationID: "test-correl-id"})
	countryID := "CO"
	itemID := "123"
	brandName := "TestBrand"

    // Simular error al obtener de cache, pero el flujo debe continuar
	mockCache.EXPECT().Get(gomock.Any(), fmt.Sprintf(keySameBrandCache, countryID, itemID)).Return("", errors.New("cache get failed"))

	originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: brandName, Status: "A", HasStock: true, GlobalStock: 1}
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)
	algoliaProducts := []sharedModel.ProductInformation{
		{ObjectID: "456", Brand: brandName, GlobalStock: 10, Status: "A", HasStock: true},
	}
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), fmt.Sprintf("brand:\"%s\"", brandName), countryID).Return(algoliaProducts, nil)

	mockCache.EXPECT().Set(gomock.Any(), fmt.Sprintf(keySameBrandCache, countryID, itemID), gomock.Any(), gomock.Any()).Return(nil)


	items, err := appService.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.NoError(t, err) // El error de cache get no debe propagarse si el flujo puede continuar
	assert.NotNil(t, items)
	assert.Len(t, items, 1)
}


// Es importante asegurarse que los mocks para sharedOutPorts (Cache, CatalogProduct, OfferOutPort)
// estén generados y en la ruta correcta: test/mocks/shared/domain/ports/out/
// Por ejemplo, para Cache:
// //go:generate mockgen -source=internal/shared/domain/ports/out/cache.go -destination=test/mocks/shared/domain/ports/out/cache_mock.go
// Y así para los otros.
```
Este es el archivo de test para `app/same_brand.go`.

A continuación, crearé el archivo de test para el handler: `internal/same-brand/infra/api/handler/same_brand_test.go`.
(El `go:generate` en el handler ya sugiere la ruta del mock `test/mocks/same-brand/infra/api/handler/same_brand_mock.go` para la interfaz `SameBrand` del handler, y un mock para `inPorts.SameBrand` que es `test/mocks/same-brand/domain/ports/in/same_brand_mock.go`).

Voy a verificar si el mock para `inPorts.SameBrand` ya existe o necesita ser generado.
`ls test/mocks/same-brand/domain/ports/in/`
