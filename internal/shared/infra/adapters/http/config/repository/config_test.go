package repository

import (
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	mockServer "ftd-td-catalog-item-read-services/test/mocks/server"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"
)

const (
	responseOKConfigBestSeller = "{\n    \"code\": \"OK\",\n    \"message\": \"Success\",\n    \"data\": {\n        \"id\": \"BEST-SELLERS.CONFIG\",\n        \"country\": \"AR\",\n        \"value\": {\n            \"objectID\": \"BEST-SELLERS.CONFIG\",\n            \"countItems\": 20.0,\n            \"queryProducts\": \"hitsPerPage=%s&filters=departments:'%s' AND outofstore:false AND stores_with_stock:%s&page=0&clickAnalytics=true&enablePersonalization=true\",\n            \"algoliaRecommend\": false\n        },\n        \"active\": true\n    }\n}"
	urlConfigBestSellerMock    = "/config/r/%s/v1/property/%s"
	countryID                  = "CO"
	property                   = "BEST-SELLERS.CONFIG"
)

func Test_GetConfigBestSellerWhenSuccess(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	server := mockServer.ConfigMockSrver(responseOKConfigBestSeller,
		fmt.Sprintf(urlConfigBestSellerMock, countryID, property), http.MethodGet, http.StatusOK, "{}")

	defer server.Close()

	config.Enviroments.BalancerUrl = server.URL

	gin.SetMode(gin.TestMode)

	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header), URL: &url.URL{}}

	ctx.Set("X-Correlation-ID", "1291823jhau1uha")

	httpConfig := NewConfig()

	data, err := httpConfig.GetConfigBestSeller(ctx, countryID, property)

	assert.NoError(t, err)
	assert.Equal(t, data.AlgoliaRecommend, false)
	assert.Equal(t, data.ObjectID, "BEST-SELLERS.CONFIG")
}

func Test_GetConfigBestSellerWhenFail(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	server := mockServer.ConfigMockSrver(responseOKConfigBestSeller,
		fmt.Sprintf(urlConfigBestSellerMock, countryID, property), http.MethodGet, http.StatusOK, "{}")

	defer server.Close()

	config.Enviroments.BalancerUrl = server.URL

	gin.SetMode(gin.TestMode)

	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header), URL: &url.URL{}}

	ctx.Set("X-Correlation-ID", "1291823jhau1uha")

	httpConfig := NewConfig()

	_, err := httpConfig.GetConfigBestSeller(ctx, countryID, "INVALID-PROPERTY")

	assert.Error(t, err)
}
