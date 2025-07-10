package handler

import (
	"encoding/json"
	"errors"
	"fmt"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	dtoResponse "ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler/dto/response"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	_ "ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response" // Import for side effects or if types are used indirectly
	mock_ports_in "ftd-td-catalog-item-read-services/test/mocks/products-related/domain/ports/in"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

// newTestContext crea un gin.Context y un httptest.Recorder para pruebas.
func newTestContext(recorder *httptest.ResponseRecorder) *gin.Context {
	c, _ := gin.CreateTestContext(recorder)
	// Inicializar Request para evitar nil pointer dereference si no se setea explícitamente un request completo
	c.Request = &http.Request{
		Header: make(http.Header),
		URL:    &url.URL{},
	}
	return c
}

func TestProductsRelatedHandler_GetRelatedItems(t *testing.T) {
	gin.SetMode(gin.TestMode)

	t.Run("should return 200 OK with related items on valid request", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)

		countryID := "CO"
		itemID := "item123"
		nearbyStores := "storeA,storeB"
		city := "Bogota"
		queryAlgolia := "test query"
		indexName := "products_co"
		algoliaParams := "facetFilters=category:A"

		domainResponse := model.AlgoliaRelatedProductsResponse{
			Results: []model.AlgoliaResult{
				{
					Hits: []sharedModel.ProductInformation{
						{ObjectID: "related1", MediaDescription: "Related Product 1"},
					},
					Query: queryAlgolia, NbHits: 1, Page: 0, HitsPerPage: 10,
				},
			},
		}

		mockAppService.EXPECT().GetRelatedItems(
			gomock.Any(), countryID, itemID, nearbyStores, city, queryAlgolia, indexName, algoliaParams,
		).Return(domainResponse, nil)

		// Configurar contexto Gin
		rr := httptest.NewRecorder()
		c := newTestContext(rr)
		c.Params = gin.Params{
			{Key: "countryId", Value: countryID},
			{Key: "itemId", Value: itemID},
		}
		// Simular query params
		parsedURL, _ := url.Parse(fmt.Sprintf("/dummy?nearby-stores=%s&city=%s&query=%s&index-name=%s&params=%s",
			nearbyStores, city, queryAlgolia, indexName, algoliaParams))
		c.Request.URL = parsedURL
		c.Request.Header.Set(string(enums.HeaderCorrelationID), "test-corr-id-handler")

		handler.GetRelatedItems(c) // Llamar directamente al handler

		assert.Equal(t, http.StatusOK, rr.Code)
		var actualResponseDto dtoResponse.ProductsRelatedResponseDto
		err := json.Unmarshal(rr.Body.Bytes(), &actualResponseDto)
		assert.NoError(t, err)
		assert.Len(t, actualResponseDto.Results, 1)
		assert.Len(t, actualResponseDto.Results[0].Hits, 1)
		assert.Equal(t, "related1", actualResponseDto.Results[0].Hits[0].ObjectID)
	})

	t.Run("should return 400 Bad Request if required itemId path param is missing and binding fails", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()
		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)

		rr := httptest.NewRecorder()
		c := newTestContext(rr)
		c.Params = gin.Params{gin.Param{Key: "countryId", Value: "CO"}} // itemId falta en c.Params
		// Request URL no es crucial aquí ya que ShouldBindUri usa c.Params
		c.Request.URL, _ = url.Parse("/dummy")

		handler.GetRelatedItems(c)

		assert.Equal(t, http.StatusBadRequest, rr.Code)
		var errorContent map[string]interface{} // <--- CORRECCIÓN AQUÍ
		err := json.Unmarshal(rr.Body.Bytes(), &errorContent)
		assert.NoError(t, err)
		assert.Equal(t, http.StatusText(http.StatusBadRequest), errorContent["code"])
		assert.True(t, strings.Contains(errorContent["message"].(string), "Invalid path parameters"), "Error message mismatch: %s", errorContent["message"])
	})

	t.Run("should return 400 Bad Request if required countryId path param is missing and binding fails", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()
		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)

		rr := httptest.NewRecorder()
		c := newTestContext(rr)
		c.Params = gin.Params{gin.Param{Key: "itemId", Value: "item123"}} // countryId falta en c.Params
		c.Request.URL, _ = url.Parse("/dummy")

		handler.GetRelatedItems(c)

		assert.Equal(t, http.StatusBadRequest, rr.Code)
		var errorContent map[string]interface{} // <--- CORRECCIÓN AQUÍ
		err := json.Unmarshal(rr.Body.Bytes(), &errorContent)
		assert.NoError(t, err)
		assert.Equal(t, http.StatusText(http.StatusBadRequest), errorContent["code"])
		assert.True(t, strings.Contains(errorContent["message"].(string), "Invalid path parameters"), "Error message mismatch: %s", errorContent["message"])
	})

	t.Run("should correctly pass empty optional query params to service", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)

		countryID := "CO"
		itemID := "item123"

		domainResponse := model.AlgoliaRelatedProductsResponse{Results: []model.AlgoliaResult{}}
		mockAppService.EXPECT().GetRelatedItems(
			gomock.Any(), countryID, itemID, "", "", "", "", "",
		).Return(domainResponse, nil)

		rr := httptest.NewRecorder()
		c := newTestContext(rr)
		c.Params = gin.Params{
			{Key: "countryId", Value: countryID},
			{Key: "itemId", Value: itemID},
		}
		c.Request.URL, _ = url.Parse("/dummy") // Sin query params

		handler.GetRelatedItems(c)

		assert.Equal(t, http.StatusOK, rr.Code)
	})

	t.Run("should return 500 Internal Server Error if app service returns error", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)

		countryID := "CO"
		itemID := "item123"
		expectedErrorMessage := "service layer error"
		expectedError := errors.New(expectedErrorMessage)

		mockAppService.EXPECT().GetRelatedItems(
			gomock.Any(), countryID, itemID, "", "", "", "", "",
		).Return(model.AlgoliaRelatedProductsResponse{}, expectedError)

		rr := httptest.NewRecorder()
		c := newTestContext(rr)
		c.Params = gin.Params{
			{Key: "countryId", Value: countryID},
			{Key: "itemId", Value: itemID},
		}
		c.Request.URL, _ = url.Parse("/dummy")

		handler.GetRelatedItems(c)

		assert.Equal(t, http.StatusInternalServerError, rr.Code)
		var errorContent map[string]interface{} // <--- CORRECCIÓN AQUÍ
		err := json.Unmarshal(rr.Body.Bytes(), &errorContent)
		assert.NoError(t, err)
		assert.Equal(t, http.StatusText(http.StatusInternalServerError), errorContent["code"])
		assert.Equal(t, expectedErrorMessage, errorContent["message"])
	})
}
