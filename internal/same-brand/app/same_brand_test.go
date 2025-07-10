package app

import (
	"context"
	"encoding/json" // Añadido para el test de caché
	"errors"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	inPorts "ftd-td-catalog-item-read-services/internal/same-brand/domain/ports/in"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	mock_shared_ports "ftd-td-catalog-item-read-services/test/mocks/shared/domain/ports/out"

	"github.com/gin-gonic/gin"
	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
)

// Helper para crear un gin.Context con headers para tests
func ginContextWithHeaders(t *testing.T, method, url string, headers map[string]string) *gin.Context {
	gin.SetMode(gin.TestMode)
	rec := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(rec)
	req, err := http.NewRequest(method, url, nil)
	if err != nil {
		t.Fatalf("Failed to create request: %v", err)
	}
	for k, v := range headers {
		req.Header.Set(k, v)
	}
	c.Request = req
	if id, ok := headers[enums.HeaderCorrelationID]; ok {
		c.Set(enums.HeaderCorrelationID, id)
	}
	return c
}


func TestSameBrand_GetItemsBySameBrand_Success_FromAlgolia(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_shared_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	// mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl) // Eliminado

	config.Enviroments.RedisSameBrandTTL = 60

	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache /*, mockOffer */) // Eliminado

	countryID := "CO"
	itemID := "123"
	brandName := "TestBrand"
    ctx := ginContextWithHeaders(t, "GET", "/", map[string]string{
		enums.HeaderCorrelationID: "test-corr-id-algolia",
		enums.HeaderXCustomCity: "Bogota",
	})


	mockCache.EXPECT().Get(gomock.AssignableToTypeOf(ctx), fmt.Sprintf(keySameBrandCache, countryID, itemID)).Return("", nil)

	originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: brandName, Status: "A", HasStock: true, StoresWithStock: []int{1}} // Simula stock en 1 tienda
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.AssignableToTypeOf(ctx), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)

	algoliaProducts := []sharedModel.ProductInformation{
		{ObjectID: "456", Brand: brandName, StoresWithStock: make([]int, 10), Status: "A", HasStock: true},
		{ObjectID: "789", Brand: brandName, StoresWithStock: make([]int, 5), Status: "A", HasStock: true},
		{ObjectID: "101", Brand: brandName, StoresWithStock: make([]int, 20), Status: "A", HasStock: true},
	}
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.AssignableToTypeOf(ctx), fmt.Sprintf("brand:\"%s\"", brandName), countryID).Return(algoliaProducts, nil)

	mockCache.EXPECT().Set(gomock.AssignableToTypeOf(ctx), fmt.Sprintf(keySameBrandCache, countryID, itemID), gomock.Any(), time.Duration(config.Enviroments.RedisSameBrandTTL)*time.Minute).Return(nil)

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
	// mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl) // Eliminado

	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache /*, mockOffer */) // Eliminado

	countryID := "CO"
	itemID := "123"
	ctx := ginContextWithHeaders(t, "GET", "/", map[string]string{enums.HeaderCorrelationID: "test-corr-id-cache"})


	cachedItems := []model.SameBrandItem{
		{ID: "cached1", Brand: "TestBrand", Status: "A"},
	}
	jsonData, _ := json.Marshal(cachedItems)

	mockCache.EXPECT().Get(gomock.AssignableToTypeOf(ctx), fmt.Sprintf(keySameBrandCache, countryID, itemID)).Return(string(jsonData), nil)

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
	// mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl) // Eliminado
	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache /*, mockOffer */) // Eliminado

	countryID := "CO"
	itemID := "123"
	ctx := ginContextWithHeaders(t, "GET", "/", map[string]string{enums.HeaderCorrelationID: "test-corr-id-notfound"})


	mockCache.EXPECT().Get(gomock.AssignableToTypeOf(ctx), gomock.Any()).Return("", nil)
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.AssignableToTypeOf(ctx), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{}, nil)

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
    // mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl) // Eliminado
    var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache /*, mockOffer */) // Eliminado

    countryID := "CO"
    itemID := "123"
    ctx := ginContextWithHeaders(t, "GET", "/", map[string]string{enums.HeaderCorrelationID: "test-corr-id-brandempty"})


    mockCache.EXPECT().Get(gomock.AssignableToTypeOf(ctx), gomock.Any()).Return("", nil)
    originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: "", Status: "A", HasStock: true, StoresWithStock: []int{1}} // Simula stock en 1 tienda
    mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.AssignableToTypeOf(ctx), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)

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
	// mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl) // Eliminado
	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache /*, mockOffer */) // Eliminado

	countryID := "CO"
	itemID := "123"
	brandName := "TestBrand"
	ctx := ginContextWithHeaders(t, "GET", "/", map[string]string{enums.HeaderCorrelationID: "test-corr-id-algolia-err"})


	mockCache.EXPECT().Get(gomock.AssignableToTypeOf(ctx), gomock.Any()).Return("", nil)
	originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: brandName, Status: "A", HasStock: true, StoresWithStock: []int{1}} // Simula stock en 1 tienda
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.AssignableToTypeOf(ctx), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.AssignableToTypeOf(ctx), fmt.Sprintf("brand:\"%s\"", brandName), countryID).Return(nil, errors.New("algolia down"))

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
	// mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl) // Eliminado
	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache /*, mockOffer */) // Eliminado

	countryID := "CO"
	itemID := "originalItem"
	brandName := "LimitBrand"
	ctx := ginContextWithHeaders(t, "GET", "/", map[string]string{enums.HeaderCorrelationID: "test-corr-id-limit"})


	config.Enviroments.RedisSameBrandTTL = 10

	mockCache.EXPECT().Get(gomock.AssignableToTypeOf(ctx), gomock.Any()).Return("", nil)
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.AssignableToTypeOf(ctx), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{{ObjectID: itemID, Brand: brandName, Status: "A", HasStock: true, StoresWithStock: []int{1}}}, nil)

	var algoliaProducts []sharedModel.ProductInformation
	totalProductsFromAlgolia := maxItemsLimit + 5
	for i := 0; i < totalProductsFromAlgolia; i++ {
		algoliaProducts = append(algoliaProducts, sharedModel.ProductInformation{
			ObjectID:    fmt.Sprintf("item%d", i),
			Brand:       brandName,
			StoresWithStock: make([]int, i+1), // Usar StoresWithStock
			Status:      "A",
			HasStock:    true,
			MediaImageUrl: fmt.Sprintf("http://image.com/item%d.jpg", i),
			Description: fmt.Sprintf("Description for item %d", i),
			FullPrice: float64(100 + i),
			OfferPrice: float64(90 + i),
		})
	}
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.AssignableToTypeOf(ctx), gomock.Any(), countryID).Return(algoliaProducts, nil)
	mockCache.EXPECT().Set(gomock.AssignableToTypeOf(ctx), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)

	items, err := appService.GetItemsBySameBrand(ctx, countryID, itemID)
	assert.NoError(t, err)
	assert.Len(t, items, maxItemsLimit)

	expectedFirstItemID := fmt.Sprintf("item%d", totalProductsFromAlgolia-1)
	assert.Equal(t, expectedFirstItemID, items[0].ID)
	var originalProductForFirstItem sharedModel.ProductInformation
	for _, p := range algoliaProducts {
		if p.ObjectID == expectedFirstItemID {
			originalProductForFirstItem = p
			break
		}
	}
	// Asegurarse que el mapper de dominio (MapProductInformationToSameBrandItem) usa len(StoresWithStock) para TotalStock
	// y que el ProductInformation usado para la aserción también lo refleje.
	assert.Equal(t, len(originalProductForFirstItem.StoresWithStock), items[0].TotalStock)

	expectedLastItemIDInLimit := algoliaProducts[totalProductsFromAlgolia-maxItemsLimit].ObjectID
	assert.Equal(t, expectedLastItemIDInLimit, items[maxItemsLimit-1].ID)
}


func TestSameBrand_ShouldIncludeProduct(t *testing.T) {
	s := NewSameBrand(nil, nil).(*sameBrand) // Eliminado mockOffer

	testCases := []struct {
		name     string
		product  sharedModel.ProductInformation
		expected bool
	}{
		{"Active with stock", sharedModel.ProductInformation{Status: "A", HasStock: true, StoresWithStock: []int{1}}, true},
		{"Inactive with stock", sharedModel.ProductInformation{Status: "I", HasStock: true, StoresWithStock: []int{1}}, false},
		{"Active no stock (HasStock false)", sharedModel.ProductInformation{Status: "A", HasStock: false, StoresWithStock: []int{1}}, false},
		{"Active with stock but no stores", sharedModel.ProductInformation{Status: "A", HasStock: true, StoresWithStock: []int{}}, false},
		{"Active no stock (HasStock false, no stores)", sharedModel.ProductInformation{Status: "A", HasStock: false, StoresWithStock: []int{}}, false},
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
	// mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl) // Eliminado

	config.Enviroments.RedisSameBrandTTL = 60
	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache /*, mockOffer */) // Eliminado

	countryID := "CO"
	itemID := "123"
	brandName := "TestBrand"
	ctx := ginContextWithHeaders(t, "GET", "/", map[string]string{enums.HeaderCorrelationID: "test-corr-id-cacheseterr"})


	mockCache.EXPECT().Get(gomock.AssignableToTypeOf(ctx), fmt.Sprintf(keySameBrandCache, countryID, itemID)).Return("", nil)
	originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: brandName, Status: "A", HasStock: true, StoresWithStock: []int{1}}
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.AssignableToTypeOf(ctx), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)
	algoliaProducts := []sharedModel.ProductInformation{
		{ObjectID: "456", Brand: brandName, StoresWithStock: make([]int, 10), Status: "A", HasStock: true},
	}
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.AssignableToTypeOf(ctx), fmt.Sprintf("brand:\"%s\"", brandName), countryID).Return(algoliaProducts, nil)

	mockCache.EXPECT().Set(gomock.AssignableToTypeOf(ctx), fmt.Sprintf(keySameBrandCache, countryID, itemID), gomock.Any(), gomock.Any()).Return(errors.New("cache set failed"))

	items, err := appService.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.NoError(t, err)
	assert.NotNil(t, items)
	assert.Len(t, items, 1)
}

func TestSameBrand_GetItemsBySameBrand_CacheGetError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCatalogProduct := mock_shared_ports.NewMockCatalogProduct(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	// mockOffer := mock_shared_ports.NewMockOfferOutPort(ctrl) // Eliminado

	config.Enviroments.RedisSameBrandTTL = 60
	var appService inPorts.SameBrand
	appService = NewSameBrand(mockCatalogProduct, mockCache /*, mockOffer */) // Eliminado

	countryID := "CO"
	itemID := "123"
	brandName := "TestBrand"
	ctx := ginContextWithHeaders(t, "GET", "/", map[string]string{enums.HeaderCorrelationID: "test-corr-id-cachegeterr"})


	mockCache.EXPECT().Get(gomock.AssignableToTypeOf(ctx), fmt.Sprintf(keySameBrandCache, countryID, itemID)).Return("", errors.New("cache get failed"))

	originalItem := sharedModel.ProductInformation{ObjectID: itemID, Brand: brandName, Status: "A", HasStock: true, StoresWithStock: []int{1}}
	mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.AssignableToTypeOf(ctx), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)
	algoliaProducts := []sharedModel.ProductInformation{
		{ObjectID: "456", Brand: brandName, StoresWithStock: make([]int, 10), Status: "A", HasStock: true},
	}
	mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.AssignableToTypeOf(ctx), fmt.Sprintf("brand:\"%s\"", brandName), countryID).Return(algoliaProducts, nil)

	mockCache.EXPECT().Set(gomock.AssignableToTypeOf(ctx), fmt.Sprintf(keySameBrandCache, countryID, itemID), gomock.Any(), gomock.Any()).Return(nil)


	items, err := appService.GetItemsBySameBrand(ctx, countryID, itemID)

	assert.NoError(t, err)
	assert.NotNil(t, items)
	assert.Len(t, items, 1)
}

```
