package groups

import (
	"net/http"
	"net/http/httptest"
	"testing"

	mockhandler "ftd-td-catalog-item-read-services/test/mocks/health/infra/api/handler"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

const (
	routerPath     = "/catalog-item/r/:countryId/v1/items"
	healthPathMock = "/health"
)

func TestNewHealthGroup(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockHealthHandler := mockhandler.NewMockHealth(ctrl)

	// Act
	group := NewHealthGroup(mockHealthHandler)

	// Assert
	assert.NotNil(t, group)
}

func TestStructureGroup(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockHealthHandler := mockhandler.NewMockHealth(ctrl)

	router := gin.Default()
	routerGroup := router.Group(routerPath)

	group := NewHealthGroup(mockHealthHandler)

	mockHealthHandler.EXPECT().HealthCheck(gomock.Any()).Times(1)

	// Act
	group.Source(routerGroup)

	req, _ := http.NewRequest(http.MethodGet, "/catalog-item/r/CO/v1/items/health", nil)

	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	// Assert
	assert.Equal(t, http.StatusOK, w.Code)
}
