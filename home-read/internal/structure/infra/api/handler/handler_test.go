package handler

import (
	"errors"
	"fmt"
	"ftd-td-home-read-services/internal/shared/utils"
	"ftd-td-home-read-services/internal/structure/domain/model"
	mockin "ftd-td-home-read-services/test/mocks/structure/domain/ports/in"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

const (
	countryID   = "AR"
	platform    = model.Web
	handlerPath = "/home/r/:countryId/v1/structure"
	requestPath = "/home/r/" + countryID + "/v1/structure"
)

func TestGetStructureShouldReturnOkResponse(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockStructureService := mockin.NewMockStructureService(ctrl)
	handler := NewStructureHandler(mockStructureService)

	router := gin.Default()
	router.POST(handlerPath, handler.GetStructure)

	requestBody := `{"customer": {"id": "123", "firstName": "John"}}`
	expectedStructure := []model.Section{
		{
			ID:    1,
			Title: "SECCION 1",
			Components: []model.Component{
				{Label: utils.StringPtr("Component 1")},
				{Label: utils.StringPtr("Component 2")},
			},
		},
		{
			ID:    2,
			Title: "SECCION 2",
			Components: []model.Component{
				{Label: utils.StringPtr("Component 3")},
			},
		},
	}

	mockStructureService.EXPECT().
		GetStructure(gomock.Any(), countryID, model.Web, &model.Customer{ID: "123", FirstName: "John"}).
		Return(expectedStructure, nil)

	// Act
	w := httptest.NewRecorder()
	router.ServeHTTP(w, newRequest(requestBody))

	// Assert
	assert.Equal(t, http.StatusOK, w.Code)
	assert.Contains(t, w.Body.String(), fmt.Sprintf(`"code":"%s"`, http.StatusText(http.StatusOK)))
	assert.Contains(t, w.Body.String(), "Component 1")
	assert.Contains(t, w.Body.String(), "Component 2")
}

func TestGetStructureShouldReturnBadRequestOnInvalidJSON(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockService := mockin.NewMockStructureService(ctrl)
	handler := NewStructureHandler(mockService)

	router := gin.Default()
	router.POST(handlerPath, handler.GetStructure)

	invalidRequestBody := `{"customer": {"id": 123}}`

	// Act
	w := httptest.NewRecorder()
	router.ServeHTTP(w, newRequest(invalidRequestBody))

	// Assert
	assert.Equal(t, http.StatusBadRequest, w.Code)
	assert.Contains(t, w.Body.String(), fmt.Sprintf(`"code":"%s"`, http.StatusText(http.StatusBadRequest)))
}

func TestGetStructureShouldReturnServerErrorOnServiceFailure(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockService := mockin.NewMockStructureService(ctrl)
	handler := NewStructureHandler(mockService)

	router := gin.Default()
	router.POST(handlerPath, handler.GetStructure)

	requestBody := `{"customer": {"id": "123", "firstName": "John"}}`

	mockService.EXPECT().
		GetStructure(gomock.Any(), countryID, model.Web, &model.Customer{ID: "123", FirstName: "John"}).
		Return(nil, errors.New("service error"))

	// Act
	w := httptest.NewRecorder()
	router.ServeHTTP(w, newRequest(requestBody))

	// Assert
	assert.Equal(t, http.StatusInternalServerError, w.Code)
	assert.Contains(t, w.Body.String(), fmt.Sprintf(`"code":"%s"`, http.StatusText(http.StatusInternalServerError)))
	assert.Contains(t, w.Body.String(), fmt.Sprintf(`"message":"Ocurrió un error al intentar obtener la estructura del home para el país '%s'"`, countryID))
}

func newRequest(requestBody string) *http.Request {
	req, _ := http.NewRequest(http.MethodPost, requestPath, strings.NewReader(requestBody))
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("source", string(platform))

	return req
}
