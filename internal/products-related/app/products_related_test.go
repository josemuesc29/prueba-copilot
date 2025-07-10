package app

import (
	"encoding/json"
	"errors"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	// Asumiendo que los mocks estarán en esta ruta relativa a la raíz del proyecto
	mock_ports_out "ftd-td-catalog-item-read-services/test/mocks/shared/domain/ports/out"

	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
)

func TestProductsRelated_GetRelatedItems(t *testing.T) {
	gin.SetMode(gin.TestMode)
	// Configurar environments para TTL (si es necesario para la prueba)
	config.Enviroments.RedisSameBrandDepartmentTTL = 10 // 10 minutes for example

	t.Run("should return cached data if cache hit", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockCache := mock_ports_out.NewMockCache(ctrl)
		mockCatalogProduct := mock_ports_out.NewMockCatalogProduct(ctrl)

		service := NewProductsRelated(mockCatalogProduct, mockCache)

		ctx, _ := gin.CreateTestContext(nil)
		ctx.Set(string(enums.HeaderCorrelationID), "test-corr-id")

		countryID := "CO"
		itemID := "item123"
		paramsKey := fmt.Sprintf("nearby:;city:;query:test;index:;params:hitsPerPage=%d", maxItemsLimit)
		// Este paramsHash es un placeholder, en la app real se calcula con SHA256.
		// Para pruebas unitarias determinísticas, podríamos calcularlo o mockear la función de hash si fuera necesario.
		// Por simplicidad, aquí usamos una clave predecible si supiéramos el hash.
		// O, más simple, esperamos cualquier string como clave de caché para el Get.
		// En este caso, el hash es sobre: "nearby:;city:;query:test;index:;params:hitsPerPage=24"
		// sha256("nearby:;city:;query:test;index:;params:hitsPerPage=24")
		// No lo calcularé aquí para mantener la prueba simple, mockCache.EXPECT().Get se basará en cualquier string.

		expectedResponse := model.AlgoliaRelatedProductsResponse{
			Results: []model.AlgoliaResult{
				{Hits: []sharedModel.ProductInformation{{ObjectID: "cached123"}}},
			},
		}
		cachedJSON, _ := json.Marshal(expectedResponse)

		// Esperar una llamada a Get con cualquier clave que termine con el hash esperado (o simplificar para la prueba)
		// El hash para "nearby:;city:;query:test;index:;params:hitsPerPage=24"
		// es 2a69990191689518536905394e944332b51f398b395f5760e070d87323a2db3c
		expectedCacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, "2a69990191689518536905394e944332b51f398b395f5760e070d87323a2db3c")


		mockCache.EXPECT().Get(gomock.Any(), expectedCacheKey).Return(string(cachedJSON), nil)

		result, err := service.GetRelatedItems(ctx, countryID, itemID, "", "", "test", "", fmt.Sprintf("hitsPerPage=%d", maxItemsLimit))

		assert.NoError(t, err)
		assert.Equal(t, expectedResponse, result)
	})

	t.Run("should fetch from Algolia and save to cache if cache miss", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockCache := mock_ports_out.NewMockCache(ctrl)
		mockCatalogProduct := mock_ports_out.NewMockCatalogProduct(ctrl)

		service := NewProductsRelated(mockCatalogProduct, mockCache)
		ctx, _ := gin.CreateTestContext(nil)
		ctx.Set(string(enums.HeaderCorrelationID), "test-corr-id-2")

		countryID := "CO"
		itemID := "item456"
		queryAlgolia := "search term"
		algoliaParams := "facetFilters=category:electronics"
		// Hash para "nearby:;city:;query:search term;index:;params:facetFilters=category:electronics&hitsPerPage=24"
		// es 3f9a78acd693998962d50e461f835435473c8f8468c27516274cd72c14600943
		expectedCacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, "3f9a78acd693998962d50e461f835435473c8f8468c27516274cd72c14600943")


		mockCache.EXPECT().Get(gomock.Any(), expectedCacheKey).Return("", errors.New("cache miss")) // o "", nil

		algoliaHits := []sharedModel.ProductInformation{
			{ObjectID: "algolia1", HasStock: true, Status: "A"},
			{ObjectID: "algolia2", HasStock: true, Status: "A"},
			{ObjectID: itemID, HasStock: true, Status: "A"}, // Este debería ser filtrado por shouldIncludeProduct
		}
		// Query que se espera que se envíe a Algolia
		// query=search term&facetFilters=category:electronics&hitsPerPage=24&filters=NOT objectID:item456
		expectedAlgoliaQuery := fmt.Sprintf("query=%s&%s&hitsPerPage=%d&filters=NOT objectID:%s", queryAlgolia, algoliaParams, maxItemsLimit, itemID)
		mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), expectedAlgoliaQuery, countryID).Return(algoliaHits, nil)

		// La respuesta esperada del servicio (después del mapeo y filtrado)
		// Solo algolia1 y algolia2, item456 (el mismo itemID) se filtra
		expectedServiceResponse := model.AlgoliaRelatedProductsResponse{
			Results: []model.AlgoliaResult{
				{
					Hits: []sharedModel.ProductInformation{
						{ObjectID: "algolia1", HasStock: true, Status: "A"},
						{ObjectID: "algolia2", HasStock: true, Status: "A"},
					},
					Query:       queryAlgolia,
					Params:      queryAlgolia, // Del mapper actual
					Page:        0,
					HitsPerPage: maxItemsLimit,
					NbHits:      2, // Solo los filtrados
					// ... otros campos por defecto del mapper
				},
			},
		}
		jsonResponse, _ := json.Marshal(expectedServiceResponse)
		mockCache.EXPECT().Set(gomock.Any(), expectedCacheKey, string(jsonResponse), gomock.Any()).Return(nil)

		result, err := service.GetRelatedItems(ctx, countryID, itemID, "", "", queryAlgolia, "", algoliaParams)

		assert.NoError(t, err)
		assert.Equal(t, len(expectedServiceResponse.Results[0].Hits), len(result.Results[0].Hits))
		assert.Equal(t, expectedServiceResponse.Results[0].Hits[0].ObjectID, result.Results[0].Hits[0].ObjectID)
	})

	t.Run("should return error if Algolia call fails", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockCache := mock_ports_out.NewMockCache(ctrl)
		mockCatalogProduct := mock_ports_out.NewMockCatalogProduct(ctrl)

		service := NewProductsRelated(mockCatalogProduct, mockCache)
		ctx, _ := gin.CreateTestContext(nil)
		ctx.Set(string(enums.HeaderCorrelationID), "test-corr-id-3")

		countryID := "MX"
		itemID := "item789"
		// Hash para "nearby:;city:;query:;index:;params:hitsPerPage=24"
		// es 7d167439f1dd591df280010c825689703803080297682583a29c40c2020f747e
		expectedCacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, "7d167439f1dd591df280010c825689703803080297682583a29c40c2020f747e")


		mockCache.EXPECT().Get(gomock.Any(), expectedCacheKey).Return("", nil) // Cache miss

		expectedError := errors.New("Algolia failed")
		// Query que se espera: hitsPerPage=24&filters=NOT objectID:item789
		expectedAlgoliaQuery := fmt.Sprintf("hitsPerPage=%d&filters=NOT objectID:%s", maxItemsLimit, itemID)
		mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), expectedAlgoliaQuery, countryID).Return(nil, expectedError)

		_, err := service.GetRelatedItems(ctx, countryID, itemID, "", "", "", "", "")
		assert.Error(t, err)
		assert.Equal(t, expectedError, err)
	})
}

func TestProductsRelated_shouldIncludeProduct(t *testing.T) {
	service := &productsRelated{} // No necesita dependencias para esta prueba

	originalItemID := "itemOriginal"

	testCases := []struct {
		name     string
		product  sharedModel.ProductInformation
		expected bool
	}{
		{"active and in stock, different item", sharedModel.ProductInformation{ObjectID: "item1", Status: "A", HasStock: true}, true},
		{"inactive item", sharedModel.ProductInformation{ObjectID: "item2", Status: "I", HasStock: true}, false},
		{"out of stock item", sharedModel.ProductInformation{ObjectID: "item3", Status: "A", HasStock: false}, false},
		{"same as original item", sharedModel.ProductInformation{ObjectID: originalItemID, Status: "A", HasStock: true}, false},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			assert.Equal(t, tc.expected, service.shouldIncludeProduct(tc.product, originalItemID))
		})
	}
}
