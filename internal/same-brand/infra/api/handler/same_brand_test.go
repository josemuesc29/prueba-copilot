package handler

import (
	"encoding/json"
	"errors"
	"fmt"
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	dto_response_samebrand "ftd-td-catalog-item-read-services/internal/same-brand/infra/api/handler/dto/response"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	mock_in_ports "ftd-td-catalog-item-read-services/test/mocks/same-brand/domain/ports/in"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"go.uber.org/mock/gomock"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
)

func TestSameBrandHandler_GetItemsSameBrand_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockAppService := mock_in_ports.NewMockSameBrand(ctrl)
	handler := NewSameBrand(mockAppService)

	countryID := "CO"
	itemID := "item123"
	correlationID := "corr-id-success"
	cityHeader := "Bogota"
	source := "WEB"
	nearbyStores := "24,25,26"
	storeId := "26"
	city := "Bogota"

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	_, router := gin.CreateTestContext(w)

	router.GET("/catalog-item/r/:countryId/v1/item/:itemId/same-brand", handler.GetItemsSameBrand)

	url := fmt.Sprintf("/catalog-item/r/%s/v1/item/%s/same-brand?source=%s&nearbyStores=%s&storeId=%s&city=%s", countryID, itemID, source, nearbyStores, storeId, city)
	req, _ := http.NewRequest(http.MethodGet, url, nil)
	req.Header.Set(enums.HeaderCorrelationID, correlationID)
	req.Header.Set(enums.HeaderXCustomCity, cityHeader)

	serviceResponse := []model.SameBrandItem{
		{ID: "prod1", Brand: "BrandX", TotalStock: 10, Status: "A", MediaImageUrl: "img.jpg", URL: "url1"},
	}

	mockAppService.EXPECT().GetItemsBySameBrand(gomock.Any(), countryID, itemID, source, nearbyStores, storeId, city).DoAndReturn(
		func(ctx *gin.Context, cID, iID, src, nearby, sId, cty string) ([]model.SameBrandItem, error) {
			assert.Equal(t, correlationID, ctx.Value(enums.HeaderCorrelationID))
			assert.Equal(t, cityHeader, ctx.GetHeader(enums.HeaderXCustomCity))
			return serviceResponse, nil
		},
	)

	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	assert.Equal(t, correlationID, w.Header().Get(enums.HeaderCorrelationID))

	var actualResponse struct {
		Code    string `json:"code"`
		Message string `json:"message"`
		Data    any    `json:"data"`
	}
	err := json.Unmarshal(w.Body.Bytes(), &actualResponse)
	assert.NoError(t, err)
	assert.Equal(t, http.StatusText(http.StatusOK), actualResponse.Code)
	assert.Equal(t, "Success", actualResponse.Message)

	// Deserializa el payload real
	dataBytes, err := json.Marshal(actualResponse.Data)
	assert.NoError(t, err)

	var actualDataPayload []dto_response_samebrand.SameBrandResponse
	err = json.Unmarshal(dataBytes, &actualDataPayload)
	assert.NoError(t, err)

	require.Len(t, actualDataPayload, 1)
	item := actualDataPayload[0]
	assert.Equal(t, "prod1", item.ID)
	assert.Equal(t, "BrandX", item.Brand)
	assert.Equal(t, 10, item.TotalStock)
	assert.Equal(t, "A", item.Status)
	assert.Equal(t, "img.jpg", item.MediaImageUrl) // Corregido de MediaImageURL a MediaImageUrl
	assert.Equal(t, "url1", item.URL)
}

func TestSameBrandHandler_GetItemsSameBrand_AppServiceError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockAppService := mock_in_ports.NewMockSameBrand(ctrl)
	handler := NewSameBrand(mockAppService)

	countryID := "CO"
	itemID := "item456"
	correlationID := "corr-id-app-error"
	source := "WEB"
	nearbyStores := "24,25,26"
	storeId := "26"
	city := "Bogota"

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	_, router := gin.CreateTestContext(w)
	router.GET("/catalog-item/r/:countryId/v1/item/:itemId/same-brand", handler.GetItemsSameBrand)

	url := fmt.Sprintf("/catalog-item/r/%s/v1/item/%s/same-brand?source=%s&nearbyStores=%s&storeId=%s&city=%s", countryID, itemID, source, nearbyStores, storeId, city)
	req, _ := http.NewRequest(http.MethodGet, url, nil)
	req.Header.Set(enums.HeaderCorrelationID, correlationID)

	expectedError := errors.New("database is down")
	mockAppService.
		EXPECT().
		GetItemsBySameBrand(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).
		DoAndReturn(func(ctx *gin.Context, countryID, itemID, source, nearbyStores, storeId, city string) ([]model.SameBrandItem, error) {
			t.Logf("source=%s", source) // <- para validar que sea "WEB"
			return nil, expectedError
		})
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusInternalServerError, w.Code)
	assert.Equal(t, correlationID, w.Header().Get(enums.HeaderCorrelationID))

	var actualResponse struct {
		Code    string `json:"code"`
		Message string `json:"message"`
		Data    any    `json:"data"`
	}
	err := json.Unmarshal(w.Body.Bytes(), &actualResponse)
	assert.NoError(t, err)
	assert.Equal(t, http.StatusText(http.StatusInternalServerError), actualResponse.Code) // "Internal Server Error"
	assert.Equal(t, expectedError.Error(), actualResponse.Message)
}

func TestSameBrandHandler_GetItemsSameBrand_BindingError_MissingItemID(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockAppService := mock_in_ports.NewMockSameBrand(ctrl)
	handler := NewSameBrand(mockAppService)

	countryID := "CO"
	correlationID := "corr-id-bind-error"

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	_, router := gin.CreateTestContext(w)

	router.GET("/catalog-item/r/:countryId/v1/item/:itemId/same-brand", handler.GetItemsSameBrand)

	// Simular itemId faltante usando doble slash
	url := fmt.Sprintf("/catalog-item/r/%s/v1/item//same-brand", countryID)
	req, _ := http.NewRequest(http.MethodGet, url, nil)
	req.Header.Set(enums.HeaderCorrelationID, correlationID)

	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusBadRequest, w.Code)
	assert.Equal(t, correlationID, w.Header().Get(enums.HeaderCorrelationID))

	var actualResponse struct {
		Code    string `json:"code"`
		Message string `json:"message"`
		Data    any    `json:"data"`
	}
	err := json.Unmarshal(w.Body.Bytes(), &actualResponse)
	assert.NoError(t, err)

	assert.True(t, strings.Contains(strings.ToLower(actualResponse.Message), "itemid"), "El mensaje de error debería mencionar itemId")
	assert.Equal(t, http.StatusText(http.StatusBadRequest), actualResponse.Code)
}
