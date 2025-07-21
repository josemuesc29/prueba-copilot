package repository

import (
	"ftd-td-home-read-services/cmd/config"
	mocks_server "ftd-td-home-read-services/test/mocks/server"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"
)

const (
	responseOKOffers = `{
        "code": "OK",
		"message": "Success",
		"data": [
			{
				"id":                  "1",
				"type":                "flash",
				"position":            1,
				"redirectUrl":         "http://example.com/offer1",
				"startDate":           "2023-01-01T00:00:00Z",
				"endDate":             "2023-01-31T23:59:59Z",
				"availableStockFlash": 100
			}
		]
	}`
	responseError = `{
        "code": "ERROR",
		"message": "Internal error",
		"data": []
	}`
	urlStrapiFlashOfferMock = "/CO/v1/offer/flash"
)

func TestGetFlashOfferSuccess(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	server := mocks_server.ConfigMockSrver(responseOKOffers,
		urlStrapiFlashOfferMock, http.MethodGet, http.StatusOK, "{}")

	defer server.Close()

	config.Enviroments.ProxyCmsUrl = server.URL

	ctx := getContext()

	offerAdapter := NewOfferCmsRepository()

	offers, err := offerAdapter.GetFlashOffer(ctx, "CO")

	assert.NoError(t, err)
	assert.Len(t, offers, 1)
	assert.Equal(t, "1", offers[0].Id)
	assert.Equal(t, "flash", offers[0].Type)
	assert.Equal(t, "http://example.com/offer1", offers[0].RedirectUrl)
}

func TestGetFlashOfferErrorResponse(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	server := mocks_server.ConfigMockSrver(responseError,
		urlStrapiFlashOfferMock, http.MethodGet, http.StatusInternalServerError, "{}")

	defer server.Close()

	config.Enviroments.ProxyCmsUrl = server.URL

	ctx := getContext()

	offerAdapter := NewOfferCmsRepository()

	offers, err := offerAdapter.GetFlashOffer(ctx, "CO")

	assert.Error(t, err)
	assert.Empty(t, offers)
}

func TestGetFlashOfferInvalidURL(t *testing.T) {
	// Simula un error de URL inválida
	config.Enviroments.ProxyCmsUrl = "http://[::1]:NamedPort"
	ctx := getContext()
	offerAdapter := NewOfferCmsRepository()

	offers, err := offerAdapter.GetFlashOffer(ctx, "CO")

	assert.Error(t, err)
	assert.Empty(t, offers)
}

func getContext() *gin.Context {
	gin.SetMode(gin.TestMode)
	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header), URL: &url.URL{}}
	ctx.Set("X-Correlation-ID", "1291823jhau1uha")
	return ctx
}
