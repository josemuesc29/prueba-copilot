package config

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestLoadEnviromentsShouldLoadDefaultValues(t *testing.T) {
	// Act
	LoadEnviroments()

	// Assert
	assert.Equal(t, "ftd-td-catalog-read-services.dev.farmatodo.com", Enviroments.CatalogCategoryHostUrl)
}
