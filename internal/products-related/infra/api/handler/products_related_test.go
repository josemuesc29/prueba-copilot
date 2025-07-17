package handler

import (
	"encoding/json"
	"errors"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler/dto/response"
	mock_in "ftd-td-catalog-item-read-services/test/mocks/products-related/domain/ports/in"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

func TestProductsRelatedHandler_GetRelatedItems(t *testing.T) {
	gin.SetMode(gin.TestMode)

	t.Run("should return related items successfully", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockAppService := mock_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)

		w := httptest.NewRecorder()
		ctx, _ := gin.CreateTestContext(w)
		ctx.Params = gin.Params{
			{Key: "countryId", Value: "CO"},
			{Key: "itemId", Value: "123"},
		}

		expectedItems := []model.ProductsRelatedItem{
			{ID: "456", MediaDescription: "Related Product"},
		}

		mockAppService.EXPECT().
			GetRelatedItems(gomock.Any(), "CO", "123").
			Return(expectedItems, nil)

		handler.GetRelatedItems(ctx)

		assert.Equal(t, http.StatusOK, w.Code)
		var responseBody response.ProductsRelatedResponseDto
		err := json.Unmarshal(w.Body.Bytes(), &responseBody)
		assert.NoError(t, err)
		assert.Len(t, responseBody, 1)
		assert.Equal(t, "456", responseBody[0].ID)
	})

	t.Run("should return error when service fails", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockAppService := mock_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)

		w := httptest.NewRecorder()
		ctx, _ := gin.CreateTestContext(w)
		ctx.Params = gin.Params{
			{Key: "countryId", Value: "CO"},
			{Key: "itemId", Value: "123"},
		}

		expectedError := errors.New("service error")
		mockAppService.EXPECT().
			GetRelatedItems(gomock.Any(), "CO", "123").
			Return(nil, expectedError)

		handler.GetRelatedItems(ctx)

		assert.Equal(t, http.StatusInternalServerError, w.Code)
	})

	t.Run("should return bad request for missing countryId", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockAppService := mock_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)

		w := httptest.NewRecorder()
		ctx, _ := gin.CreateTestContext(w)
		ctx.Params = gin.Params{
			{Key: "itemId", Value: "123"},
		}

		handler.GetRelatedItems(ctx)

		assert.Equal(t, http.StatusBadRequest, w.Code)
	})
}
