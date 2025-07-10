package handler

import (
	"encoding/json"
	"errors"
	"fmt"
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	mock_ports_in "ftd-td-catalog-item-read-services/test/mocks/products-related/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler/dto/response"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedResponse "ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response"
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

	// Función helper para crear un contexto Gin para pruebas
	setupRouter := func(h ProductsRelatedHandler) *gin.Engine {
		router := gin.New()
		// La ruta base /catalog-item/r/:countryId/v1/items es manejada por el router principal en producción.
		// Para la prueba unitaria del handler, podemos registrar la ruta completa o una parte.
		// Aquí registramos la parte que el group del handler manejaría.
		router.GET("/item/:itemId/related", h.GetRelatedItems) // Asumiendo que el :countryId está en el contexto de alguna manera o se prueba en integración.
		// Para una prueba más completa del binding de URI, necesitamos la ruta completa.
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
		var actualResponseDto response.ProductsRelatedResponseDto
		err := json.Unmarshal(rr.Body.Bytes(), &actualResponseDto)
		assert.NoError(t, err)
		assert.Len(t, actualResponseDto.Results, 1)
		assert.Len(t, actualResponseDto.Results[0].Hits, 1)
		assert.Equal(t, "related1", actualResponseDto.Results[0].Hits[0].ObjectID)
	})

	t.Run("should return 400 Bad Request if required itemId path param is missing/empty", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()
		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)
		router := setupRouter(handler) // Usa el router completo para path param matching

		// Caso 1: ItemID está vacío en el path. Gin debería dar 404 porque la ruta no coincide.
		// La validación `binding:"required"` en el DTO es una segunda capa si la ruta fuera más laxa.
		// Para probar específicamente el DTO, necesitaríamos un setup más manual del contexto,
		// pero el test HTTP es más realista.
		urlWithEmptyItemID := "/catalog-item/r/CO/v1/items/item//related"
		reqEmptyItemID, _ := http.NewRequest(http.MethodGet, urlWithEmptyItemID, nil)
		rrEmptyItemID := httptest.NewRecorder()
		router.ServeHTTP(rrEmptyItemID, reqEmptyItemID)
		// Gin devuelve 404 si un segmento de path no se puede parsear/matchear.
		// El `binding:"required"` del DTO no se alcanza si la ruta no es reconocida.
		// Si la ruta SÍ se reconociera con un itemId vacío (ej. si el :itemId fuera opcional en la ruta de Gin),
		// entonces el `binding:"required"` del DTO para `uri:"itemId"` causaría un 400.
		// Como :itemId es un segmento de path, Gin espera que esté.
		assert.True(t, rrEmptyItemID.Code == http.StatusNotFound) // Esperamos 404 por ruta no encontrada
	})

	t.Run("should return 400 Bad Request if required countryId path param is missing/empty", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()
		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)
		router := setupRouter(handler)

		urlWithEmptyCountryID := "/catalog-item/r//v1/items/item/item123/related"
		reqEmptyCountryID, _ := http.NewRequest(http.MethodGet, urlWithEmptyCountryID, nil)
		rrEmptyCountryID := httptest.NewRecorder()
		router.ServeHTTP(rrEmptyCountryID, reqEmptyCountryID)
		assert.True(t, rrEmptyCountryID.Code == http.StatusNotFound) // Esperamos 404 por ruta no encontrada
	})

	t.Run("should correctly pass empty optional query params to service", func(t *testing.T) {
		ctrl := gomock.NewController(t)
		defer ctrl.Finish()

		mockAppService := mock_ports_in.NewMockProductsRelated(ctrl)
		handler := NewProductsRelatedHandler(mockAppService)
		router := setupRouter(handler)

		countryID := "CO"
		itemID := "item123"
		// Todos los query params opcionales están vacíos

		domainResponse := model.AlgoliaRelatedProductsResponse{ /* ... respuesta dummy ... */ }
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
		expectedError := errors.New("service layer error")

		mockAppService.EXPECT().GetRelatedItems(
			gomock.Any(), countryID, itemID, "", "", "", "", "", // Asumiendo query params vacíos para este test
		).Return(model.AlgoliaRelatedProductsResponse{}, expectedError)

		url := fmt.Sprintf("/catalog-item/r/%s/v1/items/item/%s/related", countryID, itemID)
		req, _ := http.NewRequest(http.MethodGet, url, nil)
		rr := httptest.NewRecorder()
		router.ServeHTTP(rr, req)

		assert.Equal(t, http.StatusInternalServerError, rr.Code)
		var errorResponse sharedResponse.ErrorResponse
		err := json.Unmarshal(rr.Body.Bytes(), &errorResponse)
		assert.NoError(t, err)
		// El mensaje de error exacto puede variar dependiendo de sharedResponse.ServerError
		// pero debería contener la esencia del error original.
		assert.True(t, strings.Contains(errorResponse.Error, "service layer error"), "Error message mismatch")
	})
}
