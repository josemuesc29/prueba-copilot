package client

import (
	"github.com/stretchr/testify/assert"
	"os"
	"testing"
)

func TestNewRedisClient_Fail(t *testing.T) {
	err := os.Setenv("REDIS_HOST", "localhost")
	if err != nil {
		return
	}

	err = os.Setenv("REDIS_PORT", "9999")
	if err != nil {
		return
	}

	client, err := NewRedisClient()
	assert.Error(t, err)
	assert.Nil(t, client)
}
