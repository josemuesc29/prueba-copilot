package handler

import (
	"encoding/json"
	"errors"
	"fmt"
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	dto_response_samebrand "ftd-td-catalog-item-read-services/internal/same-brand/infra/api/handler/dto/response" // DTO específico para los datos de samebrand
	mock_in_ports "ftd-td-catalog-item-read-services/test/mocks/same-brand/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	// Ya no se importa "ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response" para la estructura genérica
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/golang/mock/gomock"
	"github.com/stretchr/testify/assert"
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

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, router := gin.CreateTestContext(w)

	// Definir la ruta que el handler espera
	router.GET("/catalog-item/r/:countryId/v1/item/:itemId/same-brand", handler.GetItemsSameBrand)

	// Crear la solicitud
	url := fmt.Sprintf("/catalog-item/r/%s/v1/item/%s/same-brand", countryID, itemID)
	req, _ := http.NewRequest(http.MethodGet, url, nil)
	req.Header.Set(enums.HeaderCorrelationID, correlationID)
	req.Header.Set(enums.HeaderXCustomCity, cityHeader)

	// Preparar la respuesta esperada del servicio de aplicación
	serviceResponse := []model.SameBrandItem{
		{ID: "prod1", Brand: "BrandX", TotalStock: 10, Status: "A", MediaImageUrl: "img.jpg", URL: "url1"},
	}
	// Preparar el DTO de respuesta esperado (después del mapeo de API)
	// Asumimos que el mapper ModelSameBrandItemListToSameBrandItemDtoList convierte model.SameBrandItem a dto_response_samebrand.Item
	expectedAPIResponseData := []dto_response_samebrand.Item{
		{ID: "prod1", Brand: "BrandX", TotalStock: 10, Status: "A", MediaImageURL: "img.jpg", URL: "url1"},
	}

	mockAppService.EXPECT().GetItemsSameBrand(gomock.Any(), countryID, itemID).DoAndReturn(
		func(ctx *gin.Context, cID, iID string) ([]model.SameBrandItem, error) {
			// Verificar que el handler ha puesto el correlationID en el contexto Gin
			assert.Equal(t, correlationID, ctx.Value(enums.HeaderCorrelationID))
			// Verificar que el X-Custom-City original está disponible para el servicio de app via GetHeader
			assert.Equal(t, cityHeader, ctx.GetHeader(enums.HeaderXCustomCity))
			return serviceResponse, nil
		},
	)

	// Ejecutar la solicitud a través del router
	router.ServeHTTP(w, req)

	// Verificar el código de estado y headers de respuesta
	assert.Equal(t, http.StatusOK, w.Code)
	assert.Equal(t, correlationID, w.Header().Get(enums.HeaderCorrelationID))

	// Verificar el cuerpo de la respuesta
	var actualResponse struct { // Definir struct local para el cuerpo de la respuesta genérica
		Code    string `json:"code"`
		Message string `json:"message"`
		Data    any    `json:"data"`
	}
	err := json.Unmarshal(w.Body.Bytes(), &actualResponse)
	assert.NoError(t, err)
	assert.Equal(t, http.StatusText(http.StatusOK), actualResponse.Code) // "OK"
	assert.Equal(t, "Success", actualResponse.Message)

	// Verificar los datos dentro de la respuesta
	dataBytes, err := json.Marshal(actualResponse.Data)
	assert.NoError(t, err)
	var actualDataPayload []dto_response_samebrand.Item
	err = json.Unmarshal(dataBytes, &actualDataPayload)
	assert.NoError(t, err)
	assert.Equal(t, expectedAPIResponseData, actualDataPayload)
}

func TestSameBrandHandler_GetItemsSameBrand_AppServiceError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockAppService := mock_in_ports.NewMockSameBrand(ctrl)
	handler := NewSameBrand(mockAppService)

	countryID := "CO"
	itemID := "item456"
	correlationID := "corr-id-app-error"

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	_, router := gin.CreateTestContext(w)
	router.GET("/catalog-item/r/:countryId/v1/item/:itemId/same-brand", handler.GetItemsSameBrand)

	url := fmt.Sprintf("/catalog-item/r/%s/v1/item/%s/same-brand", countryID, itemID)
	req, _ := http.NewRequest(http.MethodGet, url, nil)
	req.Header.Set(enums.HeaderCorrelationID, correlationID)

	expectedError := errors.New("database is down")
	mockAppService.EXPECT().GetItemsSameBrand(gomock.Any(), countryID, itemID).Return(nil, expectedError)

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

	mockAppService := mock_in_ports.NewMockSameBrand(ctrl) // No debería ser llamado
	handler := NewSameBrand(mockAppService)

	countryID := "CO"
	correlationID := "corr-id-bind-error"

	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	_, router := gin.CreateTestContext(w)

	// Registrar la ruta que espera :itemId
	router.GET("/catalog-item/r/:countryId/v1/item/:itemId/same-brand", handler.GetItemsSameBrand)

	// Construir una URL donde el valor para :itemId está efectivamente ausente o malformado
	// para causar un error de binding en el DTO que tiene `uri:"itemId" binding:"required"`.
	// Llamar a una ruta sin el path param completo resultará en un 404 por el router de Gin.
	// Para probar el binding:"required" directamente, necesitamos que el router llame al handler,
	// pero que el param no pueda ser bindeado.
	// Si el path es `/.../item//same-brand`, Gin no lo considera un match para `/.../item/:itemId/same-brand`.
	// Este test es más para asegurar que el DTO está bien anotado y el handler reacciona a errores de binding.
	// Sin embargo, el `ShouldBindUri` fallará si un param requerido no se encuentra.

	// Simular una solicitud a una URL donde el router de Gin no puede extraer `itemId` como se espera.
	// Esto es difícil de simular perfectamente sin que Gin de un 404 antes.
	// Una forma de forzar el llamado al handler pero con un param "vacío" es si la ruta fuera más flexible,
	// pero con path params definidos, Gin es estricto.
	// Vamos a asumir que el DTO tiene una validación que falla si itemId es vacío después de ser extraído.
	// En la práctica, `binding:"required"` en un path param string asegura que no sea vacío.

	// Caso: URL que el router matchea, pero un param de path es vacío (no permitido por Gin para :param).
	// Para este test, vamos a crear un contexto "manual" y llamar al handler,
	// y no poner el `itemId` en `c.Params` para que `ShouldBindUri` falle.

	cManual, _ := gin.CreateTestContext(w)
	reqManual, _ := http.NewRequest(http.MethodGet, fmt.Sprintf("/catalog-item/r/%s/v1/item/DUMMY/same-brand", countryID), nil) // URL es solo para el request
	reqManual.Header.Set(enums.HeaderCorrelationID, correlationID)
	cManual.Request = reqManual
	cManual.Params = gin.Params{ // Simular que Gin llenó los params, pero itemId está ausente
		{Key: "countryId", Value: countryID},
		// {Key: "itemId", Value: "somevalue"} // Omitido para causar fallo en binding:"required" del DTO
	}

	// No se espera llamada al servicio de app
	// mockAppService.EXPECT().GetItemsSameBrand(gomock.Any(), gomock.Any(), gomock.Any()).Times(0)

	handler.GetItemsSameBrand(cManual)

	assert.Equal(t, http.StatusBadRequest, w.Code)
	assert.Equal(t, correlationID, w.Header().Get(enums.HeaderCorrelationID))

	var actualResponse struct {
		Code    string `json:"code"`
		Message string `json:"message"`
		Data    any    `json:"data"`
	}
	err := json.Unmarshal(w.Body.Bytes(), &actualResponse)
	assert.NoError(t, err)
	// El mensaje de error exacto puede variar según la implementación de Gin/validator.
	// Debería indicar que 'itemId' es requerido o no pudo ser bindeado.
	// La función BadRequest usa el error.Error() como mensaje.
	assert.True(t, strings.Contains(actualResponse.Message, "itemId"), "El mensaje de error debería mencionar itemId")
	assert.Equal(t, http.StatusText(http.StatusBadRequest), actualResponse.Code) // "Bad Request"
}

// Los DTOs de request no tienen headers requeridos, por lo que un test específico para
// `ShouldBindHeader` fallando por un header requerido faltante no es aplicable aquí.
// Si se añadiera `binding:"required"` a un header en el DTO, se podría testear.
```
