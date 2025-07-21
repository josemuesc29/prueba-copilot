package config

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestLoadEnviromentsShouldLoadDefaultValues(t *testing.T) {
	// Act
	LoadEnviroments()

	// Assert
	assert.Equal(t, "http://localhost:8081/crm", Enviroments.CrmApiUrl)
	assert.Equal(t, "http://localhost:8082/proxy-cms", Enviroments.ProxyCmsUrl)
}
