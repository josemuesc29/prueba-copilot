package http

import (
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"
	"time"

	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/test/mocks/server"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
)

const (
	responseOK = `{
		"message": "ok"
    }`
	responseError = `{
		"message": "not found"
	}`
	urlStrapiSuggestedMock = "/test"
)

var (
	request = Requestor{
		HttpMethod: http.MethodGet,
		MaxRetry:   1,
		Backoff:    1 * time.Second,
		TTLTimeOut: 5 * time.Second,
	}
)

type responseStructOk struct {
	Message string `json:"message"`
}

func TestDoRequest_WhenSuccess(t *testing.T) {
	var response responseStructOk
	server := mocks_server.ConfigMockSrver(responseOK,
		urlStrapiSuggestedMock, http.MethodGet, http.StatusOK, responseError)

	defer server.Close()

	request.URL = server.URL + urlStrapiSuggestedMock

	code, err := DoRequest(request, &response)

	assert.NoError(t, err)
	assert.Equal(t, code, http.StatusOK)
}

func TestDoRequest_WhenSuccessWithPatchMethod(t *testing.T) {
	var response responseStructOk
	server := mocks_server.ConfigMockSrver(responseOK,
		urlStrapiSuggestedMock, http.MethodPatch, http.StatusOK, responseError)

	defer server.Close()

	gin.SetMode(gin.TestMode)

	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header), URL: &url.URL{}}

	ctx.Set("X-Correlation-ID", "1291823jhau1uha")

	request.Context = ctx
	request.URL = server.URL + urlStrapiSuggestedMock
	request.HttpMethod = http.MethodPatch

	code, err := DoRequest(request, &response)

	assert.NoError(t, err)
	assert.Equal(t, code, http.StatusOK)
	request.HttpMethod = http.MethodGet
}

func TestDoRequest_WhenFailBodyEncode(t *testing.T) {
	var response responseStructOk
	var body any

	server := mocks_server.ConfigMockSrver(responseOK,
		urlStrapiSuggestedMock, http.MethodGet, http.StatusOK, responseError)

	defer server.Close()

	body = make(chan int)

	request.URL = server.URL + urlStrapiSuggestedMock
	request.Body = &body

	code, err := DoRequest(request, &response)

	assert.Error(t, err)
	assert.Contains(t, err.Error(), "json: unsupported type: chan int")
	assert.Equal(t, code, http.StatusUnprocessableEntity)
	request.Body = nil
}

func TestDoRequest_WhenFailNewReques(t *testing.T) {
	var response responseStructOk

	server := mocks_server.ConfigMockSrver(responseOK,
		urlStrapiSuggestedMock, http.MethodPost, http.StatusOK, responseError)

	defer server.Close()

	request.URL = urlStrapiSuggestedMock + "	"
	request.HttpMethod = http.MethodPost

	code, err := DoRequest(request, &response)

	assert.Error(t, err)
	assert.Contains(t, err.Error(), "invalid control character in URL")
	assert.Equal(t, code, http.StatusUnprocessableEntity)
}

func TestDoRequest_WhenFailWithRetry(t *testing.T) {
	var response responseStructOk
	server := mocks_server.ConfigMockSrver(responseOK,
		urlStrapiSuggestedMock, http.MethodPut, http.StatusOK, responseError)

	defer server.Close()

	request.URL = config.Enviroments.CatalogProductsUrl + urlStrapiSuggestedMock
	request.HttpMethod = http.MethodPut

	code, err := DoRequest(request, &response)

	assert.Error(t, err)
	assert.Equal(t, code, http.StatusInternalServerError)
	request.HttpMethod = http.MethodGet
}

func TestDoRequest_WhenFailsWithHttpMethodNotAllowed(t *testing.T) {
	var response responseStructOk
	server := mocks_server.ConfigMockSrver(responseOK,
		urlStrapiSuggestedMock, http.MethodPatch, http.StatusOK, responseError)

	defer server.Close()

	request.URL = server.URL + urlStrapiSuggestedMock
	request.HttpMethod = http.MethodOptions

	code, err := DoRequest(request, &response)

	assert.Error(t, err)
	assert.Equal(t, err.Error(), errorMsgMethodHttpNotAllowed)
	assert.Equal(t, code, http.StatusUnprocessableEntity)
	request.HttpMethod = http.MethodGet
}

func TestDoRequest_WhenFailStatusCodeResponseError(t *testing.T) {
	var response responseStructOk
	server := mocks_server.ConfigMockSrver(responseOK,
		urlStrapiSuggestedMock, http.MethodPost, http.StatusOK, responseError)

	defer server.Close()

	request.URL = server.URL + urlStrapiSuggestedMock

	code, err := DoRequest(request, &response)

	assert.Error(t, err)
	assert.Equal(t, err.Error(), responseError)
	assert.Equal(t, code, http.StatusNotFound)
}

func TestDoRequest_WhenFailUnmarshalResponse(t *testing.T) {
	var response responseStructOk
	server := mocks_server.ConfigMockSrver("{",
		urlStrapiSuggestedMock, http.MethodGet, http.StatusOK, responseError)

	defer server.Close()

	request.URL = server.URL + urlStrapiSuggestedMock

	code, err := DoRequest(request, &response)

	assert.Error(t, err)
	assert.Equal(t, err.Error(), "unexpected end of JSON input")
	assert.Equal(t, code, http.StatusOK)
}
