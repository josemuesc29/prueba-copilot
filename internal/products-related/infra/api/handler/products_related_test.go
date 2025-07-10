package handler

import (
	"encoding/json"
	"errors"
	"fmt"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	mock_ports_in "ftd-td-catalog-item-read-services/test/mocks/products-related/domain/ports/in"
	dtoResponse "ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler/dto/response"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	// Importar el paquete de respuesta genérica compartida
	sharedInfraResponse "ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
)

func TestProductsRelatedHandler_GetRelatedItems(t *testing.T) {
	gin.SetMode(gin.TestMode)

	setupRouter := func(h ProductsRelatedHandler) *gin.Engine {
		router := gin.New()
		fullPathRouter := gin.New()
		fullPathRouter.GET("/catalog-item/r/:countryId/v1/items/item/:itemId/related", h.GetRelatedItems)
		return fullPathRouter
	}

	t.Run("should return 200 OK with related items on valid request", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)
		router := setupRouter(handler)

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

		url := fmt.Sprintf("/catalog-item/r/%s/v1/items/item/%s/related?nearby-stores=%s&city=%s&query=%s&index-name=%s&params=%s",
			countryID, itemID, nearbyStores, city, queryAlgolia, indexName, algoliaParams)
		req, _ := http.NewRequest(http.MethodGet, url, nil)
		req.Header.Set(string(enums.HeaderCorrelationID), "test-corr-id-handler")

		rr := httptest.NewRecorder()
		router.ServeHTTP(rr, req)

		assert.Equal(t, http.StatusOK, rr.Code)
		var actualResponseDto dtoResponse.ProductsRelatedResponseDto // Usar el DTO específico del handler
		err := json.Unmarshal(rr.Body.Bytes(), &actualResponseDto)
		assert.NoError(t, err)
		assert.Len(t, actualResponseDto.Results, 1)
		assert.Len(t, actualResponseDto.Results[0].Hits, 1)
		assert.Equal(t, "related1", actualResponseDto.Results[0].Hits[0].ObjectID)
	})

	t.Run("should return 400 Bad Request if required itemId path param is missing/empty and binding fails", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()
		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)

		// Para probar el fallo de ShouldBindUri, necesitamos un contexto donde el param no esté seteado
		// pero la ruta sí se registre de una forma que permita el match parcial antes del binding.
		// Esto es difícil con el router de Gin que hace un match estricto de path primero.
		// Si la ruta es /item/:itemId/related, y se llama a /item//related, Gin da 404.
		// Si la ruta fuera /item/*itemId/related (con wildcard), entonces el handler sería llamado
		// y el ShouldBindUri podría fallar si itemId es vacío y tiene `binding:"required"`.

		// Simulación: Creamos el contexto y llamamos al handler directamente.
		// Esta prueba es más unitaria para el handler, no tanto un test HTTP completo de la ruta.
		w := httptest.NewRecorder()
		c, _ := gin.CreateTestContext(w)
		// No establecemos c.Params para itemId, o lo establecemos vacío.
		// Para que ShouldBindUri falle, el DTO debe tener `binding:"required"` y el param no estar.
		// Nuestro DTO tiene `uri:"itemId" binding:"required"`.
		c.Params = gin.Params{gin.Param{Key: "countryId", Value: "CO"}} // itemId falta

		// Crear un request dummy para que el contexto no sea totalmente nil
		req, _ := http.NewRequest(http.MethodGet, "/dummy", nil)
		c.Request = req

		handler.GetRelatedItems(c) // Llamar directamente al método del handler

		assert.Equal(t, http.StatusBadRequest, w.Code)
		var errorResponse sharedInfraResponse.GenericResponse // Usar la estructura correcta
		err := json.Unmarshal(w.Body.Bytes(), &errorResponse)
		assert.NoError(t, err)
		assert.Equal(t, http.StatusText(http.StatusBadRequest), errorResponse.Code)
		assert.True(t, strings.Contains(errorResponse.Message, "Invalid path parameters"), "Error message mismatch: %s", errorResponse.Message)
	})


	t.Run("should correctly pass empty optional query params to service", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)
		router := setupRouter(handler)

		countryID := "CO"
		itemID := "item123"

		domainResponse := model.AlgoliaRelatedProductsResponse{ Results: []model.AlgoliaResult{} }
		mockAppService.EXPECT().GetRelatedItems(
			gomock.Any(), countryID, itemID, "", "", "", "", "",
		).Return(domainResponse, nil)

		url := fmt.Sprintf("/catalog-item/r/%s/v1/items/item/%s/related", countryID, itemID)
		req, _ := http.NewRequest(http.MethodGet, url, nil)
		rr := httptest.NewRecorder()
		router.ServeHTTP(rr, req)

		assert.Equal(t, http.StatusOK, rr.Code)
	})

	t.Run("should return 500 Internal Server Error if app service returns error", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)
		router := setupRouter(handler)

		countryID := "CO"
		itemID := "item123"
		expectedErrorMessage := "service layer error"
		expectedError := errors.New(expectedErrorMessage)

		mockAppService.EXPECT().GetRelatedItems(
			gomock.Any(), countryID, itemID, "", "", "", "", "",
		).Return(model.AlgoliaRelatedProductsResponse{}, expectedError)

		url := fmt.Sprintf("/catalog-item/r/%s/v1/items/item/%s/related", countryID, itemID)
		req, _ := http.NewRequest(http.MethodGet, url, nil)
		rr := httptest.NewRecorder()
		router.ServeHTTP(rr, req)

		assert.Equal(t, http.StatusInternalServerError, rr.Code)
		var errorResponse sharedInfraResponse.GenericResponse // Usar la estructura correcta
		err := json.Unmarshal(rr.Body.Bytes(), &errorResponse)
		assert.NoError(t, err)
		assert.Equal(t, http.StatusText(http.StatusInternalServerError), errorResponse.Code)
		assert.Equal(t, expectedErrorMessage, errorResponse.Message)
	})
}
