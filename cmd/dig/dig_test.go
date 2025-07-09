package dig

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestDigConfigWhenSuccess(t *testing.T) {

	container := BuildContainer()

	assert.NotEmpty(t, container)
}
