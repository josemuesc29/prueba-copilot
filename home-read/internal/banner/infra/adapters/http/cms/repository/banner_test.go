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
	responseOKBanners = `{
		"code": "OK",
		"message": "Success",
		"data": [
			{
				"id": 1,
				"documentId": "doc-1",
				"extras": {"key": "value"},
				"viewed": true,
				"imageUrl": "https://example.com/image.jpg",
				"title": "Banner Title",
				"description": "Banner Description",
				"url": "https://example.com",
				"type": "promo",
				"expiresAt": "2025-12-31",
				"linkText": "Click here",
				"createdAt": "2025-01-01",
				"updatedAt": "2025-01-02",
				"publishedAt": "2025-01-03",
				"locale": "es-AR",
			    "position": 1,
				"product": "product-123",
				"content": "Some content",
				"category": "main",
				"bannername": "Main Banner"
			}
		]
	}`
	responseError = `{
        "code": "ERROR",
        "message": "Internal error",
        "data": []
    }`
	urlStrapiBannerMock = "/CO/v1/banner/banners"
)

func TestGetBannersSuccess(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	server := mocks_server.ConfigMockSrver(responseOKBanners,
		urlStrapiBannerMock, http.MethodGet, http.StatusOK, "{}")

	defer server.Close()

	config.Enviroments.ProxyCmsUrl = server.URL

	ctx := getContext()

	bannerAdapter := NewBannerCmsRepository()

	banners, err := bannerAdapter.GetBanners(ctx, "CO")

	assert.NoError(t, err)
	assert.Len(t, banners, 1)
	assert.Equal(t, 1, banners[0].ID)
	assert.Equal(t, "doc-1", banners[0].DocumentID)
	assert.Equal(t, "Banner Title", banners[0].Title)
	assert.Equal(t, "Main Banner", banners[0].BannerName)
	assert.Equal(t, "https://example.com", banners[0].URL)
}

func TestGetBannersErrorResponse(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	server := mocks_server.ConfigMockSrver(responseError,
		urlStrapiBannerMock, http.MethodGet, http.StatusInternalServerError, "{}")

	defer server.Close()

	config.Enviroments.ProxyCmsUrl = server.URL

	ctx := getContext()

	bannerAdapter := NewBannerCmsRepository()

	banners, err := bannerAdapter.GetBanners(ctx, "CO")

	assert.Error(t, err)
	assert.Empty(t, banners)
}

func TestGetBannersInvalidURL(t *testing.T) {
	config.Enviroments.ProxyCmsUrl = "http://[::1]:NamedPort"
	ctx := getContext()

	bannerAdapter := NewBannerCmsRepository()

	banners, err := bannerAdapter.GetBanners(ctx, "CO")

	assert.Error(t, err)
	assert.Empty(t, banners)
}

func getContext() *gin.Context {
	gin.SetMode(gin.TestMode)
	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header), URL: &url.URL{}}
	ctx.Set("X-Correlation-ID", "test-correlation-id")
	return ctx
}
