package app

import (
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	mock_ports_out "ftd-td-catalog-item-read-services/test/mocks/shared/domain/ports/out"
	"testing"
	"time"

	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"

	"github.com/gin-gonic/gin"
	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
)

// Helper para calcular hash como en la app
func calculateParamsHash(nearbyStores, city, queryAlgolia, indexName, algoliaParamsStr string) string {
	paramsKey := fmt.Sprintf("nearby:%s;city:%s;query:%s;index:%s;params:%s", nearbyStores, city, queryAlgolia, indexName, algoliaParamsStr)
	hasher := sha256.New()
	hasher.Write([]byte(paramsKey))
	return hex.EncodeToString(hasher.Sum(nil))
}

func TestProductsRelated_GetRelatedItems(t *testing.T) {
	gin.SetMode(gin.TestMode)
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
		queryAlgolia := "test"
		algoliaParamsStr := "hitsPerPage=24"

		paramsHash := calculateParamsHash("", "", queryAlgolia, "", algoliaParamsStr)
		expectedCacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, paramsHash)

		// Esta es la respuesta que esperamos de la caché, debe coincidir con lo que se guardaría.
		// Los campos como ExhaustiveFacetsCount, Facets, etc., serán false/nil según el mapper.
		expectedResponse := model.AlgoliaRelatedProductsResponse{
			Results: []model.AlgoliaResult{
				{
					Hits:                     []sharedModel.ProductInformation{{ObjectID: "cached123"}},
					Query:                    queryAlgolia, // El query original
					Params:                   queryAlgolia, // El query original como params
					Page:                     0,       // Default de la app si no se parsea de params
					HitsPerPage:              24,      // Parseado de algoliaParamsStr
					NbHits:                   1,       // len(hits)
					NbPages:                  1,       // Calculado por el mapper
					ExhaustiveFacetsCount:    false,
					ExhaustiveNbHits:         false,
					Facets:                   nil,
					FacetsStats:              nil,
					RenderingContent:         nil,
					Length:                   1, // len(hits)
					Extensions:               model.Extensions{QueryCategorization: nil},
					// ... otros campos con sus valores cero o por defecto del mapper
				},
			},
		}
		cachedJSON, _ := json.Marshal(expectedResponse)

		mockCache.EXPECT().Get(gomock.Any(), expectedCacheKey).Return(string(cachedJSON), nil)

		result, err := service.GetRelatedItems(ctx, countryID, itemID, "", "", queryAlgolia, "", algoliaParamsStr)

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
		algoliaParams := "facetFilters=category:electronics&hitsPerPage=10"

		paramsHash := calculateParamsHash("", "", queryAlgolia, "", algoliaParams)
		expectedCacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, paramsHash)

		mockCache.EXPECT().Get(gomock.Any(), expectedCacheKey).Return("", errors.New("cache miss"))

		algoliaHits := []sharedModel.ProductInformation{
			{ObjectID: "algolia1", HasStock: true, Status: "A"},
			{ObjectID: "algolia2", HasStock: true, Status: "A"},
			{ObjectID: itemID, HasStock: true, Status: "A"},
		}
		expectedAlgoliaQuery := fmt.Sprintf("query=%s&%s&filters=NOT objectID:%s", queryAlgolia, algoliaParams, itemID)
		mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), expectedAlgoliaQuery, countryID).Return(algoliaHits, nil)

		// Esto es lo que se espera que el mapper genere y se guarde en caché/devuelva.
		expectedServiceResponseToCache := model.AlgoliaRelatedProductsResponse{
			Results: []model.AlgoliaResult{
				{
					Hits: []sharedModel.ProductInformation{
						{ObjectID: "algolia1", HasStock: true, Status: "A"},
						{ObjectID: "algolia2", HasStock: true, Status: "A"},
					},
					Query:                    queryAlgolia,
					Params:                   queryAlgolia,
					Page:                     0,    // Default de la app
					HitsPerPage:              10,   // Parseado de algoliaParams
					NbHits:                   2,    // len(filteredHits)
					NbPages:                  1,    // Calculado: Ceil(2/10) = 1
					ExhaustiveFacetsCount:    false,
					ExhaustiveNbHits:         false,
					Facets:                   nil,
					FacetsStats:              nil,
					RenderingContent:         nil,
					Length:                   2,    // len(filteredHits)
					Extensions:               model.Extensions{QueryCategorization: nil},
					// ... otros campos con sus valores cero o por defecto del mapper
				},
			},
		}
		jsonToCache, _ := json.Marshal(expectedServiceResponseToCache)
		mockCache.EXPECT().Set(gomock.Any(), expectedCacheKey, string(jsonToCache), gomock.Any()).Return(nil)

		result, err := service.GetRelatedItems(ctx, countryID, itemID, "", "", queryAlgolia, "", algoliaParams)

		assert.NoError(t, err)
		// Comparamos la estructura completa devuelta, que debe coincidir con lo que se guardaría en caché.
		assert.Equal(t, expectedServiceResponseToCache, result)
		// Verificaciones individuales adicionales si se desea
		assert.Len(t, result.Results[0].Hits, 2)
		assert.Equal(t, "algolia1", result.Results[0].Hits[0].ObjectID)
		assert.Equal(t, 10, result.Results[0].HitsPerPage)
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
		queryAlgolia := ""
		algoliaParams := ""

		paramsHash := calculateParamsHash("", "", queryAlgolia, "", algoliaParams)
		expectedCacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, paramsHash)

		mockCache.EXPECT().Get(gomock.Any(), expectedCacheKey).Return("", nil)

		expectedError := errors.New("Algolia failed")
		expectedAlgoliaQuery := fmt.Sprintf("filters=NOT objectID:%s", itemID)
		mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), expectedAlgoliaQuery, countryID).Return(nil, expectedError)

		_, err := service.GetRelatedItems(ctx, countryID, itemID, "", "", queryAlgolia, "", algoliaParams)
		assert.Error(t, err)
		assert.Equal(t, expectedError, err)
	})
}

func TestProductsRelated_shouldIncludeProduct(t *testing.T) {
	service := &productsRelated{}
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
