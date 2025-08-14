package client

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestNewPostgresConnection(t *testing.T) {
	_, err := NewPostgresConnection()
	assert.Error(t, err)
}
