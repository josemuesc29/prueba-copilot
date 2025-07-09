package handler

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
)

const (
	countryID   = "AR"
	handlerPath = "/proxy-cms/:countryId/v1/health"
	requestPath = "/proxy-cms/" + countryID + "/v1/health"
)

func TestHealthCheckShouldReturnOkResponse(t *testing.T) {
	// Arrange
	handler := NewHealth()

	router := gin.Default()
	router.GET(handlerPath, handler.HealthCheck)

	// Act
	req, _ := http.NewRequest(http.MethodGet, requestPath, nil)
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	// Assert
	assert.Equal(t, http.StatusOK, w.Code)
}
