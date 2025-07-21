package repository

import (
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"

	"ftd-td-home-read-services/cmd/config"
	mocks_server "ftd-td-home-read-services/test/mocks/server"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

const (
	responseOKGetSuggests = `{
		"code": "OK",
    	"message": "Success",
		"data": [
			{
				"id": 6,
				"documentId": "jc3n8bbyv6rnfkjccd4dkabk",
				"sku": "263550021",
				"createdAt": "2025-04-21T14:22:45.288Z",
				"updatedAt": "2025-04-21T14:22:45.288Z",
				"publishedAt": "2025-04-21T14:22:46.392Z",
				"locale": "es-CO",
				"name": "Sku Argentina"
			}
		]
	}`
	urlProxyCmsSuggestedMock = "/CO/v1/carousel/suggest"
)

func TestStrapi_GetSuggestedWhenSuccess(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	server := mocks_server.ConfigMockSrver(responseOKGetSuggests,
		urlProxyCmsSuggestedMock, http.MethodGet, http.StatusOK, "{}")

	defer server.Close()

	config.Enviroments.ProxyCmsUrl = server.URL

	gin.SetMode(gin.TestMode)

	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header), URL: &url.URL{}}

	ctx.Set("X-Correlation-ID", "1291823jhau1uha")

	httpStrapi := NewSuggested()

	data, err := httpStrapi.GetSuggested(ctx, "CO")

	assert.NoError(t, err)
	assert.Equal(t, data.Data[0].Sku, "263550021")
}
