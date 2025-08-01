package repository

import (
	"context"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestCmsItemStructureRepository_GetItemStructure_Success(t *testing.T) {
	// Arrange
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		assert.Equal(t, "/AR/v1/structure/detail-product", r.URL.Path)
		assert.Equal(t, "AR", r.Header.Get("countryId"))
		w.WriteHeader(http.StatusOK)
		_, err := fmt.Fprintln(w, `{
			"data": [{
				"label": "Test Component",
				"componentType": "MAIN_ITEM",
				"serviceUrl": "example",
				"redirectUrl": "example",
				"order": 1
			}]
		}`)
		assert.NoError(t, err)
	}))
	defer server.Close()

	config.Enviroments.ProxyCmsUrl = server.URL

	repo := NewItemStructureRepository()

	// Act
	result, err := repo.GetItemStructure(context.Background(), "AR")

	// Assert
	assert.NoError(t, err)
	assert.NotNil(t, result)
	assert.Len(t, result, 1)
	assert.Equal(t, "Test Component", result[0].Label)
	assert.Equal(t, model.MainItemComponentType, result[0].ComponentType)
}

func TestCmsItemStructureRepository_GetItemStructure_Error(t *testing.T) {
	// Arrange
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusInternalServerError)
	}))
	defer server.Close()

	repo := NewItemStructureRepository()

	// Act
	result, err := repo.GetItemStructure(context.Background(), "AR")

	// Assert
	assert.Error(t, err)
	assert.Nil(t, result)
}
