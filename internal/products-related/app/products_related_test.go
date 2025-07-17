package app

import (
	"errors"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/test/mocks/shared/domain"
	"net/http"
	"net/http/httptest"
	"testing"
	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
	"github.com/stretchr/testify/assert"
)

func TestProductsRelated_GetRelatedItems(t *testing.T) {
	mockCtrl := gomock.NewController(t)
	defer mockCtrl.Finish()

	mockCache := domain.NewMockCache(mockCtrl)
	mockCatalogProduct := domain.NewMockCatalogProduct(mockCtrl)
	mockConfig := domain.NewMockConfigOutPort(mockCtrl)

	w := httptest.NewRecorder()
	ctx, _ := gin.CreateTestContext(w)
	req, _ := http.NewRequest("GET", "/", nil)
	ctx.Request = req

	correlationID := "test-correlation-id"
	ctx.Set(string(enums.HeaderCorrelationID), correlationID)

	service := NewProductsRelated(mockCatalogProduct, mockCache, mockConfig)

	t.Run("should return an error when there is a problem with get config", func(t *testing.T) {
		// Given
		mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
		mockConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(sharedModel.ConfigBestSeller{}, errors.New("error getting config"))

		// When
		result, err := service.GetRelatedItems(ctx, "co", "123")

		// Then
		assert.Error(t, err)
		assert.Nil(t, result)
	})

	t.Run("should return an error when there is a problem with get products related", func(t *testing.T) {
		// Given
		mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
		mockConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(sharedModel.ConfigBestSeller{}, nil)
		mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), gomock.Any(), gomock.Any()).Return(nil, errors.New("error getting products related"))

		// When
		result, err := service.GetRelatedItems(ctx, "co", "123")

		// Then
		assert.NoError(t, err)
		assert.Empty(t, result)
	})

	t.Run("should return an empty list when id suggested is not found", func(t *testing.T) {
		// Given
		mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
		mockConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(sharedModel.ConfigBestSeller{}, nil)
		mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), gomock.Any(), gomock.Any()).Return([]sharedModel.ProductInformation{}, nil)

		// When
		result, err := service.GetRelatedItems(ctx, "co", "123")

		// Then
		assert.NoError(t, err)
		assert.Empty(t, result)
	})

	t.Run("should return an empty list when there is a problem with get products from algolia", func(t *testing.T) {
		// Given
		mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
		mockConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(sharedModel.ConfigBestSeller{}, nil)
		mockCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), gomock.Any(), gomock.Any()).Return([]sharedModel.ProductInformation{{IDSuggested: []int{1}}}, nil)
		mockCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), gomock.Any(), gomock.Any()).Return(nil, errors.New("error getting products from algolia"))

		// When
		result, err := service.GetRelatedItems(ctx, "co", "123")

		// Then
		assert.NoError(t, err)
		assert.Empty(t, result)
	})
}
