package utils

import (
	"bytes"
	"context"
	"testing"

	log "github.com/sirupsen/logrus"
	"github.com/stretchr/testify/assert"
)

const (
	componentLog = "StructureService.GetStructure"
	messageLog   = "This is a test message"
)

func TestLogInfo(t *testing.T) {
	// Arrange
	var buf bytes.Buffer

	originalOutput := log.StandardLogger().Out
	defer log.SetOutput(originalOutput)

	log.SetOutput(&buf)
	log.SetFormatter(&log.TextFormatter{DisableTimestamp: true})

	ctx := context.Background()

	// Act
	LogInfo(ctx, componentLog, messageLog)

	logged := buf.String()

	// Assert
	assert.Contains(t, logged, "level=info")
	assert.Contains(t, logged, componentLog)
	assert.Contains(t, logged, messageLog)
}

func TestLogWarn(t *testing.T) {
	// Arrange
	var buf bytes.Buffer

	originalOutput := log.StandardLogger().Out
	defer log.SetOutput(originalOutput)

	log.SetOutput(&buf)
	log.SetFormatter(&log.TextFormatter{DisableTimestamp: true})

	ctx := context.Background()

	// Act
	LogWarn(ctx, componentLog, messageLog)

	logged := buf.String()

	// Assert
	assert.Contains(t, logged, "level=warning")
	assert.Contains(t, logged, componentLog)
	assert.Contains(t, logged, messageLog)
}

func TestLogError(t *testing.T) {
	// Arrange
	var buf bytes.Buffer

	originalOutput := log.StandardLogger().Out
	defer log.SetOutput(originalOutput)

	log.SetOutput(&buf)
	log.SetFormatter(&log.TextFormatter{DisableTimestamp: true})

	ctx := context.Background()

	// Act
	LogError(ctx, componentLog, messageLog)

	logged := buf.String()

	// Assert
	assert.Contains(t, logged, "level=error")
	assert.Contains(t, logged, componentLog)
	assert.Contains(t, logged, messageLog)
}
