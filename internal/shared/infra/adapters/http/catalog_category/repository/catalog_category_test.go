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
	responseOKCatalogCategory    = "{\n    \"code\": \"OK\",\n    \"message\": \"Success\",\n    \"data\": {\n        \"countryId\": \"AR\",\n        \"classificationId\": 4810,\n        \"classificationTypeId\": 1,\n        \"name\": \"Salud y medicamentos\",\n        \"image\": \"https://lh3.googleusercontent.com/Z4LURnHAqHElTocT5_32SyAli-xI2bUQrvxStjkSm0qDdCiAoijh62d9O6kcOuGnEO7XP0E5fTSiq7C4qqf6NvwROfDoseZ9FJkYgx6LfJ7ZRz_L\",\n        \"color\": \"#84d24c\",\n        \"secondColor\": \"#ffffff\",\n        \"active\": true,\n        \"order\": 1,\n        \"anywaySelling\": false,\n        \"division\": 0,\n        \"groupNo\": 0,\n        \"dept\": 0,\n        \"classClassification\": 0,\n        \"subclassClassification\": 0,\n        \"metaTitle\": \"Salud y medicamentos\",\n        \"metaDescription\": \"Salud y medicamentos\",\n        \"menuNavigationType\": \"DROPDOWN\",\n        \"path\": \"/salud-medicamentos\",\n        \"redirect\": false,\n        \"htmlSEO\": \"<a>Salud y medicamentos</a>\",\n        \"classificationParentId\": null,\n        \"updateDate\": null,\n        \"createDate\": null\n    }\n}"
	responseErrorCatalogCategory = "{\n    \"code\": \"Bad request\",\n    \"message\": \"Error\" \n}"
	urlCatalogCategoryMock       = "/catalog/r/%s/v1/categories/%s"
	countryID                    = "AR"
	departmentID                 = "123"
)

func Test_GetConfigBestSellerWhenSuccess(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	server := mockServer.ConfigMockSrver(responseOKCatalogCategory,
		fmt.Sprintf(urlCatalogCategoryMock, countryID, departmentID), http.MethodGet, http.StatusOK, "{}")

	defer server.Close()

	config.Enviroments.BalancerUrl = server.URL

	gin.SetMode(gin.TestMode)

	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header), URL: &url.URL{}}

	ctx.Set("X-Correlation-ID", "1291823jhau1uha")

	httpCatalogCategory := NewCatalogCategory()

	data, err := httpCatalogCategory.GetCategoryByDepartment(ctx, countryID, departmentID)

	assert.NoError(t, err)
	assert.Equal(t, data.Active, true)
	assert.Equal(t, data.Name, "Salud y medicamentos")
}

func Test_GetConfigBestSellerWhenFail(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	server := mockServer.ConfigMockSrver(responseErrorCatalogCategory,
		fmt.Sprintf(urlCatalogCategoryMock, countryID, departmentID), http.MethodGet, http.StatusOK, "{}")

	defer server.Close()

	config.Enviroments.BalancerUrl = server.URL

	gin.SetMode(gin.TestMode)

	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header), URL: &url.URL{}}

	ctx.Set("X-Correlation-ID", "1291823jhau1uha")

	httpCatalogCategory := NewCatalogCategory()

	_, err := httpCatalogCategory.GetCategoryByDepartment(ctx, countryID, departmentID)

	assert.Error(t, err)
}
