package repository

import (
	"context"
	"ftd-td-home-read-services/cmd/config"
	"ftd-td-home-read-services/internal/structure/domain/model"
	mockserver "ftd-td-home-read-services/test/mocks/server"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
)

const (
	countryID      = "AR"
	cmsUrlMock     = "/" + countryID + "/v1/structures/home"
	platform       = model.Android
	jsonResponseOk = `{
		"code": "200",
		"message": "OK",
		"data": %s
	}`
	jsonData = `[
		{
			"id": 1,
			"title": "SECCION 1",
			"components": [
				{
					"componentType": "MAIN_BANNER",
					"enableFor": ["IOS", "ANDROID", "RESPONSIVE"],
					"redirectUrl": null,
					"serviceUrl": "/home/r/{countryId}/v1/banners/main",
					"label": null,
					"labelWeb": null,
					"active": true,
					"visibleFor": ["ANONYMOUS", "LOGGED_IN"],
					"position": 1
				},
				{
					"componentType": "SECONDARY_BANNER",
					"enableFor": ["IOS", "ANDROID", "RESPONSIVE"],
					"redirectUrl": null,
					"serviceUrl": "/home/r/{countryId}/v1/banners/secundary",
					"label": null,
					"labelWeb": null,
					"active": true,
					"visibleFor": ["ANONYMOUS", "LOGGED_IN"],
					"position": 2
				}
			]
		}
	]`
	headerCorrelationValue = "1291823jhau1uha"
)

/*func TestGetHomeStructureShouldReturnComponents(t *testing.T) {
	// Arrange
	server, ctx := setUpServer(http.StatusOK, fmt.Sprintf(jsonResponseOk, jsonData))

	defer server.Close()

	repository := NewStructureRepository()

	// Act
	structure, err := repository.GetHomeStructure(ctx, countryID, platform)

	// Assert
	assert.NoError(t, err)
	assert.Len(t, structure, 1)
	assert.Len(t, structure[0].Components, 2)
}*/

func TestGetHomeStructureShouldReturnFallbackComponentsWhenCmsReturnsError(t *testing.T) {
	// Arrange
	server, ctx := setUpServer(http.StatusInternalServerError, "{}")
	defer server.Close()

	repository := NewStructureRepository()

	// Act
	structure, err := repository.GetHomeStructure(ctx, countryID, platform)

	// Assert
	assert.NoError(t, err)
	assert.NotEmpty(t, structure)
}

func setUpServer(statusCode int, jsonResponse string) (*httptest.Server, context.Context) {
	server := mockserver.ConfigMockSrver(
		jsonResponse,
		cmsUrlMock,
		http.MethodGet,
		statusCode,
		"{}",
	)

	config.Enviroments.ProxyCmsUrl = server.URL

	ctx := context.Background()

	return server, ctx
}
