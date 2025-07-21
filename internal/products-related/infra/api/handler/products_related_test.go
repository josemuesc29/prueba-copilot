package handler

import (
	"errors"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"ftd-td-catalog-item-read-services/test/mocks/products-related/domain"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

func TestProductsRelatedHandler_GetRelatedItems(t *testing.T) {
	mockCtrl := gomock.NewController(t)
	defer mockCtrl.Finish()

	mockProductsRelated := domain.NewMockProductsRelated(mockCtrl)
	handler := NewProductsRelatedHandler(mockProductsRelated)

	t.Run("should return a 200 status code when the request is successful", func(t *testing.T) {
		// Given
		w := httptest.NewRecorder()
		c, _ := gin.CreateTestContext(w)
		req, _ := http.NewRequest("GET", "/countries/co/items/123/related", nil)
		c.Request = req
		c.Params = gin.Params{
			{Key: "countryId", Value: "co"},
			{Key: "itemId", Value: "123"},
		}

		mockProductsRelated.EXPECT().GetRelatedItems(gomock.Any(), "co", "123").Return([]model.ProductsRelatedItem{}, nil)

		// When
		handler.GetRelatedItems(c)

		// Then
		assert.Equal(t, http.StatusOK, w.Code)
	})

	t.Run("should return a 400 status code when the request has invalid parameters", func(t *testing.T) {
		// Given
		w := httptest.NewRecorder()
		c, _ := gin.CreateTestContext(w)
		req, _ := http.NewRequest("GET", "/", nil)
		c.Request = req

		// When
		handler.GetRelatedItems(c)

		// Then
		assert.Equal(t, http.StatusBadRequest, w.Code)
	})

	t.Run("should return a 500 status code when the service returns an error", func(t *testing.T) {
		// Given
		w := httptest.NewRecorder()
		c, _ := gin.CreateTestContext(w)
		req, _ := http.NewRequest("GET", "/countries/co/items/123/related", nil)
		c.Request = req
		c.Params = gin.Params{
			{Key: "countryId", Value: "co"},
			{Key: "itemId", Value: "123"},
		}

		mockProductsRelated.EXPECT().GetRelatedItems(gomock.Any(), "co", "123").Return(nil, errors.New("service error"))

		// When
		handler.GetRelatedItems(c)

		// Then
		assert.Equal(t, http.StatusInternalServerError, w.Code)
	})
}
