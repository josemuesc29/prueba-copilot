package utils

import (
	"path/filepath"
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestBuildPathFromProjectRootShouldReturnCorrectPath(t *testing.T) {
	// Arrange
	relativePath := "test/folder/file.txt"

	// Act
	result := BuildPathFromProjectRoot(relativePath)

	// Assert
	assert.True(t, strings.HasSuffix(result, filepath.Clean(relativePath)))
	assert.True(t, filepath.IsAbs(result))
}

func TestBuildPathFromProjectRootShouldHandleEmptyRelativePath(t *testing.T) {
	// Arrange
	relativePath := ""

	// Act
	result := BuildPathFromProjectRoot(relativePath)

	// Assert
	assert.NotEmpty(t, result)
	assert.True(t, filepath.IsAbs(result))
}

func TestBuildPathFromProjectRootShouldNormalizePath(t *testing.T) {
	// Arrange
	relativePath := "test/../folder/./file.txt"

	// Act
	result := BuildPathFromProjectRoot(relativePath)

	// Assert
	expectedPath := filepath.Clean("folder/file.txt")
	assert.True(t, strings.HasSuffix(result, expectedPath))
	assert.True(t, filepath.IsAbs(result))
}
