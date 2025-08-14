package handler_test

import (
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"

	"ftd-td-catalog-item-read-services/internal/total-stock/infra/api/handler"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	mock_ports "ftd-td-catalog-item-read-services/test/mocks/total-stock/domain/ports/in"

	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
	"github.com/stretchr/testify/assert"
)

func TestTotalStockHandler_GetTotalStock_Success(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockService := mock_ports.NewMockTotalStock(ctrl)
	handler := handler.NewTotalStockHandler(mockService)

	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request, _ = http.NewRequest(http.MethodGet, "/catalog-item/r/AR/v1/items/total-stock/123?storeIds=1,2,3", nil)
	c.Params = gin.Params{
		{Key: "countryId", Value: "AR"},
		{Key: "itemId", Value: "123"},
	}
	c.Set(enums.HeaderCorrelationID, "test-id")

	mockService.EXPECT().GetTotalStockByItem(c, "AR", "123", []string{"1", "2", "3"}).Return(int64(100), nil)

	// Act
	handler.GetTotalStock(c)

	// Assert
	assert.Equal(t, http.StatusOK, w.Code)
	assert.JSONEq(t, `{"code":"OK","data":{"totalStock":100},"message":"Success"}`, w.Body.String())
}

func TestTotalStockHandler_GetTotalStock_BadRequest_MissingStoreIds(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockService := mock_ports.NewMockTotalStock(ctrl)
	handler := handler.NewTotalStockHandler(mockService)

	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	// Note: The request URL is missing the storeIds query param
	c.Request, _ = http.NewRequest(http.MethodGet, "/catalog-item/r/AR/v1/items/total-stock/123", nil)
	c.Params = gin.Params{
		{Key: "countryId", Value: "AR"},
		{Key: "itemId", Value: "123"},
	}
	c.Set(enums.HeaderCorrelationID, "test-id")

	// Act
	handler.GetTotalStock(c)

	// Assert
	assert.Equal(t, http.StatusBadRequest, w.Code)
}

func TestTotalStockHandler_GetTotalStock_ServerError(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockService := mock_ports.NewMockTotalStock(ctrl)
	handler := handler.NewTotalStockHandler(mockService)

	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request, _ = http.NewRequest(http.MethodGet, "/catalog-item/r/AR/v1/items/total-stock/123?storeIds=1,2,3", nil)
	c.Params = gin.Params{
		{Key: "countryId", Value: "AR"},
		{Key: "itemId", Value: "123"},
	}
	c.Set(enums.HeaderCorrelationID, "test-id")

	mockService.EXPECT().GetTotalStockByItem(c, "AR", "123", []string{"1", "2", "3"}).Return(int64(0), errors.New("some service error"))

	// Act
	handler.GetTotalStock(c)

	// Assert
	assert.Equal(t, http.StatusInternalServerError, w.Code)
}
