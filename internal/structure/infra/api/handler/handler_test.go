package handler

import (
	"errors"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
	mock_in "ftd-td-catalog-item-read-services/test/mocks/structure/domain/ports/in"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

func TestHandler_GetStructure_Success(t *testing.T) {
	// Arrange
	controller := gomock.NewController(t)
	defer controller.Finish()

	mockService := mock_in.NewMockItemStructureService(controller)
	handler := NewItemStructureHandler(mockService)

	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	req, _ := http.NewRequest(http.MethodGet, "/catalog-item/r/AR/v1/items/MLA123/item-section-structure", nil)
	req.Header.Set("X-Correlation-ID", "test-id")
	c.Request = req
	c.Params = gin.Params{
		{Key: "countryId", Value: "AR"},
		{Key: "itemId", Value: "MLA123"},
	}

	expectedResponse := []model.Component{{Label: "Test"}}
	mockService.EXPECT().GetItemStructure(gomock.Any(), "AR", "MLA123").Return(expectedResponse, nil)

	// Act
	handler.GetStructure(c)

	// Assert
	assert.Equal(t, http.StatusOK, w.Code)
	expectedJson := `{"code": "OK", "data": [{"label":"Test","componentType":""}], "message": "Success"}`
	assert.JSONEq(t, expectedJson, w.Body.String())
}

func TestHandler_GetStructure_Error(t *testing.T) {
	// Arrange
	controller := gomock.NewController(t)
	defer controller.Finish()

	mockService := mock_in.NewMockItemStructureService(controller)
	handler := NewItemStructureHandler(mockService)

	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	req, _ := http.NewRequest(http.MethodGet, "/catalog-item/r/AR/v1/items/MLA123/item-section-structure", nil)
	req.Header.Set("X-Correlation-ID", "test-id")
	c.Request = req
	c.Params = gin.Params{
		{Key: "countryId", Value: "AR"},
		{Key: "itemId", Value: "MLA123"},
	}

	mockService.EXPECT().GetItemStructure(gomock.Any(), "AR", "MLA123").Return(nil, errors.New("service error"))

	// Act
	handler.GetStructure(c)

	// Assert
	assert.Equal(t, http.StatusInternalServerError, w.Code)
}
