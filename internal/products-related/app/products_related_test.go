package app

import (
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
	"strings"
	"testing"
	"time"
)

func TestProductsRelated_GetRelatedItems(t *testing.T) {
	gin.SetMode(gin.TestMode)
	config.Enviroments.RedisProductsRelatedDepartmentTTL = 10

	t.Run("should return cached data if cache hit", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockCache := mock_ports_out.NewMockCache(ctrl)
		mockCatalogProduct := mock_ports_out.NewMockCatalogProduct(ctrl)
		mockConfig := mock_ports_out.NewMockConfigOutPort(ctrl) // Mock para ConfigOutPort
		service := NewProductsRelated(mockCatalogProduct, mockCache, mockConfig)
		ctx, _ := gin.CreateTestContext(nil)
		ctx.Set(string(enums.HeaderCorrelationID), "test-corr-id")

		countryID := "CO"
		itemID := "item123"
		expectedCacheKey := fmt.Sprintf(keyRelatedProductsCache, countryID, itemID, "")
		expectedCacheKey := fmt.Sprintf(keyRelatedProductsCache, countryID, itemID, "")

		expectedResponse := []model.ProductsRelatedItem{
			{ID: "cached123"},
		}
		cachedJSON, _ := json.Marshal(expectedResponse)

		mockCache.EXPECT().Get(gomock.Any(), expectedCacheKey).Return(string(cachedJSON), nil)

		result, err := service.GetRelatedItems(ctx, countryID, itemID)

		assert.NoError(t, err)
		assert.Equal(t, expectedResponse, result)
	})

	t.Run("should fetch from service and save to cache if cache miss", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockCache := mock_ports_out.NewMockCache(ctrl)
		mockCatalogProduct := mock_ports_out.NewMockCatalogProduct(ctrl)
		mockConfig := mock_ports_out.NewMockConfigOutPort(ctrl)
		service := NewProductsRelated(mockCatalogProduct, mockCache, mockConfig)
		ctx, _ := gin.CreateTestContext(nil)
		ctx.Set(string(enums.HeaderCorrelationID), "test-corr-id-2")

		countryID := "CO"
		itemID := "item456"
		expectedCacheKey := fmt.Sprintf(keyRelatedProductsCache, countryID, itemID)

		mockCache.EXPECT().Get(gomock.Any(), expectedCacheKey).Return("", errors.New("cache miss"))

		configBestSeller := &sharedModel.ConfigBestSeller{
			QueryProducts: "some_query_format_%s",
			CountItems:    10,
		}
		mockConfig.EXPECT().GetConfigBestSeller(gomock.Any(), countryID, configProductsRelatedKey).Return(configBestSeller, nil)

		originalItem := sharedModel.ProductInformation{
			ObjectID:    itemID,
			IDSuggested: []int{1, 2, 3},
		}
		mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{itemID}, countryID).Return([]sharedModel.ProductInformation{originalItem}, nil)

		suggestedItems := []sharedModel.ProductInformation{
			{ObjectID: "suggested1", Status: "A", StoresWithStock: []int{10, 20}},
			{ObjectID: "suggested2", Status: "A", StoresWithStock: []int{30}},
		}
		idSuggestedStr := strings.Trim(strings.Join(strings.Fields(fmt.Sprint(originalItem.IDSuggested)), ", "), "[]")
		query := fmt.Sprintf(configBestSeller.QueryProducts, idSuggestedStr)
		mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), query, countryID).Return(suggestedItems, nil)

		mockCache.EXPECT().Set(gomock.Any(), expectedCacheKey, gomock.Any(), time.Duration(config.Enviroments.RedisProductsRelatedDepartmentTTL)*time.Minute).Return(nil)

		result, err := service.GetRelatedItems(ctx, countryID, itemID)

		assert.NoError(t, err)
		assert.Len(t, result, 2)
		assert.Equal(t, "suggested1", result[0].ID)
	})

	t.Run("should return error if GetConfigBestSeller fails", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockCache := mock_ports_out.NewMockCache(ctrl)
		mockCatalogProduct := mock_ports_out.NewMockCatalogProduct(ctrl)
		mockConfig := mock_ports_out.NewMockConfigOutPort(ctrl)
		service := NewProductsRelated(mockCatalogProduct, mockCache, mockConfig)
		ctx, _ := gin.CreateTestContext(nil)

		countryID := "CO"
		itemID := "item789"

		mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
		expectedErr := errors.New("config error")
		mockConfig.EXPECT().GetConfigBestSeller(gomock.Any(), countryID, configProductsRelatedKey).Return(nil, expectedErr)

		_, err := service.GetRelatedItems(ctx, countryID, itemID)
		assert.Error(t, err)
		assert.Equal(t, expectedErr, err)
	})
}

func TestProductsRelated_shouldIncludeProduct(t *testing.T) {
	service := &productsRelated{}

	testCases := []struct {
		name     string
		product  sharedModel.ProductInformation
		expected bool
	}{
		{"active and in stock", sharedModel.ProductInformation{ObjectID: "item1", Status: "A", StoresWithStock: []int{1, 2}}, true},
		{"inactive item", sharedModel.ProductInformation{ObjectID: "item2", Status: "I", StoresWithStock: []int{1, 2}}, false},
		{"out of stock item", sharedModel.ProductInformation{ObjectID: "item3", Status: "A", StoresWithStock: []int{}}, false},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			assert.Equal(t, tc.expected, service.shouldIncludeProduct(tc.product))
		})
	}
}
