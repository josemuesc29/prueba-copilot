package app

import (
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	mock_ports_out "ftd-td-catalog-item-read-services/test/mocks/shared/domain/ports/out"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
	"testing"
)

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
		algoliaParamsStr := "hitsPerPage=24" // hitsPerPage ahora viene del cliente

		paramsHash := calculateParamsHash("", "", queryAlgolia, "", algoliaParamsStr)
		expectedCacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, paramsHash)

		expectedResponse := model.AlgoliaRelatedProductsResponse{
			Results: []model.AlgoliaResult{
				{Hits: []sharedModel.ProductInformation{{ObjectID: "cached123"}}},
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
			{ObjectID: itemID, HasStock: true, Status: "A"}, // este se filtra
		}
		expectedAlgoliaQuery := fmt.Sprintf("query=%s&%s&filters=NOT objectID:%s", queryAlgolia, algoliaParams, itemID)
		mockCatalogProduct.EXPECT().
			GetProductsInformationByQuery(gomock.Any(), expectedAlgoliaQuery, countryID).
			Return(algoliaHits, nil)

		// Interceptar el Set y validar el contenido JSON serializado
		mockCache.EXPECT().
			Set(gomock.Any(), expectedCacheKey, gomock.Any(), gomock.Any()).
			DoAndReturn(func(_ interface{}, _ string, value string, _ interface{}) error {
				var response model.AlgoliaRelatedProductsResponse
				err := json.Unmarshal([]byte(value), &response)
				assert.NoError(t, err)

				// Validar lo importante
				assert.Len(t, response.Results, 1)
				assert.Len(t, response.Results[0].Hits, 2)
				assert.Equal(t, "algolia1", response.Results[0].Hits[0].ObjectID)
				assert.Equal(t, "algolia2", response.Results[0].Hits[1].ObjectID)
				assert.Equal(t, 10, response.Results[0].HitsPerPage)
				assert.Equal(t, queryAlgolia, response.Results[0].Query)
				return nil
			})

		// Ejecutar
		result, err := service.GetRelatedItems(ctx, countryID, itemID, "", "", queryAlgolia, "", algoliaParams)

		// Validaciones finales
		assert.NoError(t, err)
		assert.Len(t, result.Results, 1)
		assert.Len(t, result.Results[0].Hits, 2)
		assert.Equal(t, "algolia1", result.Results[0].Hits[0].ObjectID)
		assert.Equal(t, "algolia2", result.Results[0].Hits[1].ObjectID)
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
		algoliaParams := "" // No hitsPerPage especificado aquí

		paramsHash := calculateParamsHash("", "", queryAlgolia, "", algoliaParams)
		expectedCacheKey := fmt.Sprintf(keyRelatedCacheFormat, countryID, itemID, paramsHash)

		mockCache.EXPECT().Get(gomock.Any(), expectedCacheKey).Return("", nil)

		expectedError := errors.New("Algolia failed")
		// La query esperada ya no tiene hitsPerPage por defecto
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
