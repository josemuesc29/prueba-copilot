package middleware

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
)

func setupRouter() *gin.Engine {
	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.GET("/test/:countryId", ValidateCountryID(), func(c *gin.Context) {
		c.String(http.StatusOK, "ok")
	})
	return r
}

func TestValidateCountryID_ValidCountries(t *testing.T) {
	validCountries := []string{"ar", "co", "ve", "AR", "CO", "VE"}
	router := setupRouter()

	for _, country := range validCountries {
		req, _ := http.NewRequest(http.MethodGet, "/test/"+country, nil)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)
		assert.Equal(t, http.StatusOK, w.Code)
		assert.Equal(t, "ok", w.Body.String())
	}
}

func TestValidateCountryID_InvalidCountries(t *testing.T) {
	invalidCountries := []string{"br", "us", "mx", "123"}
	router := setupRouter()

	for _, country := range invalidCountries {
		req, _ := http.NewRequest(http.MethodGet, "/test/"+country, nil)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)
		assert.Equal(t, http.StatusBadRequest, w.Code)
		assert.Contains(t, w.Body.String(), "inválido")
	}
}

func TestIsValidCountry(t *testing.T) {
	assert.True(t, isValidCountry("ar"))
	assert.True(t, isValidCountry("AR"))
	assert.True(t, isValidCountry("co"))
	assert.True(t, isValidCountry("CO"))
	assert.True(t, isValidCountry("ve"))
	assert.True(t, isValidCountry("VE"))
	assert.False(t, isValidCountry("br"))
	assert.False(t, isValidCountry("us"))
	assert.False(t, isValidCountry(""))
}
