package utils

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

const (
	correlationID = "test-correlation-id"
)

func TestGetCorrelationID_WhenSuccess(t *testing.T) {
	result := GetCorrelationID(correlationID)

	assert.Equal(t, result, correlationID)
}

func TestGetCorrelationID_WhenEmpty(t *testing.T) {
	result := GetCorrelationID("")

	assert.NotEqual(t, result, "")
}
