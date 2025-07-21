package utils

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestStringPtrShouldReturnPointerToString(t *testing.T) {
	// Arrange
	input := "test string"

	// Act
	result := StringPtr(input)

	// Assert
	assert.NotNil(t, result)
	assert.Equal(t, input, *result)
}

func TestStringPtrShouldHandleEmptyString(t *testing.T) {
	// Arrange
	input := ""

	// Act
	result := StringPtr(input)

	// Assert
	assert.NotNil(t, result)
	assert.Equal(t, input, *result)
}

func TestStringPtrShouldHandleSpecialCharacters(t *testing.T) {
	// Arrange
	input := "special!@#$%^&*()"

	// Act
	result := StringPtr(input)

	// Assert
	assert.NotNil(t, result)
	assert.Equal(t, input, *result)
}

func TestReplacePlaceholdersShouldReplaceAllPlaceholders(t *testing.T) {
	// Arrange
	input := StringPtr("Hello, {firstName} {lastName}!")
	replacements := map[string]string{
		"firstName": "John",
		"lastName":  "Doe",
	}

	// Act
	result := ReplacePlaceholders(input, replacements)

	// Assert
	assert.NotNil(t, result)
	assert.Equal(t, "Hello, John Doe!", *result)
}

func TestReplacePlaceholdersShouldHandleNilInput(t *testing.T) {
	// Arrange
	var input *string
	replacements := map[string]string{
		"firstName": "John",
		"lastName":  "Doe",
	}

	// Act
	result := ReplacePlaceholders(input, replacements)

	// Assert
	assert.Nil(t, result)
}

func TestReplacePlaceholdersShouldHandleNoPlaceholders(t *testing.T) {
	// Arrange
	input := StringPtr("Hello, World!")
	replacements := map[string]string{
		"firstName": "John",
		"lastName":  "Doe",
	}

	// Act
	result := ReplacePlaceholders(input, replacements)

	// Assert
	assert.NotNil(t, result)
	assert.Equal(t, "Hello, World!", *result)
}
