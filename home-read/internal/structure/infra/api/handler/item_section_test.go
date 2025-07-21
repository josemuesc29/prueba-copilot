package handler

import (
	"context"
	"errors"
	"ftd-td-home-read-services/internal/structure/domain/model"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

type MockItemSectionService struct {
	mock.Mock
}

func (m *MockItemSectionService) GetItemSectionStructure(ctx context.Context, countryID string, itemID string) ([]model.ItemSection, error) {
	args := m.Called(ctx, countryID, itemID)
	return args.Get(0).([]model.ItemSection), args.Error(1)
}

func TestItemSectionHandler_GetItemSectionStructure(t *testing.T) {
	mockService := new(MockItemSectionService)
	handler := NewItemSectionHandler(mockService)

	gin.SetMode(gin.TestMode)

	t.Run("Success", func(t *testing.T) {
		w := httptest.NewRecorder()
		c, _ := gin.CreateTestContext(w)
		c.Params = gin.Params{
			{Key: "countryId", Value: "AR"},
			{Key: "itemId", Value: "123"},
		}
		c.Request, _ = http.NewRequest(http.MethodGet, "/", nil)
		c.Request.Header.Set("source", "WEB")

		serviceResponse := []model.ItemSection{
			{
				Label:         "Item Principal",
				ComponentType: "MAIN_ITEM",
				ServiceUrl:    "/catalog-item/r/AR/v1/items/123",
			},
		}

		mockService.On("GetItemSectionStructure", mock.Anything, "AR", "123").Return(serviceResponse, nil).Once()

		handler.GetItemSectionStructure(c)

		assert.Equal(t, http.StatusOK, w.Code)
		mockService.AssertExpectations(t)
	})

	t.Run("ServiceError", func(t *testing.T) {
		w := httptest.NewRecorder()
		c, _ := gin.CreateTestContext(w)
		c.Params = gin.Params{
			{Key: "countryId", Value: "AR"},
			{Key: "itemId", Value: "123"},
		}
		c.Request, _ = http.NewRequest(http.MethodGet, "/", nil)
		c.Request.Header.Set("source", "WEB")

		mockService.On("GetItemSectionStructure", mock.Anything, "AR", "123").Return([]model.ItemSection{}, errors.New("service error")).Once()

		handler.GetItemSectionStructure(c)

		assert.Equal(t, http.StatusInternalServerError, w.Code)
		mockService.AssertExpectations(t)
	})
}
