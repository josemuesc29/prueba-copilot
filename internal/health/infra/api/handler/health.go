package handler

//go:generate mockgen -source=health.go -destination=../../../../../test/mocks/health/infra/api/handler/health_mock.go

import (
	"github.com/gin-gonic/gin"
)

type health struct {
}

type Health interface {
	HealthCheck(c *gin.Context)
}

func NewHealth() Health {
	return &health{}
}

func (h health) HealthCheck(c *gin.Context) {
	c.JSON(200, gin.H{
		"message": "service running",
	})
}
