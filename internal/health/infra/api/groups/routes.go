package groups

//go:generate mockgen -source=routes.go -destination=../../../../../test/mocks/health/infra/api/groups/health_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/health/infra/api/handler"
	"ftd-td-catalog-item-read-services/internal/shared/middleware"

	"github.com/gin-gonic/gin"
)

const healthPath = "/health"

type healthGroup struct {
	healthH handler.Health
}

type HealthGroup interface {
	Source(rg *gin.RouterGroup)
}

func NewHealthGroup(healthH handler.Health) HealthGroup {
	return &healthGroup{
		healthH: healthH,
	}
}

func (g healthGroup) Source(rg *gin.RouterGroup) {
	health := rg.Group(healthPath)

	health.GET("", g.healthH.HealthCheck, middleware.ValidateCountryID())
}
