package out

//go:generate mockgen -source=config.go -destination=../../../../../test/mocks/shared/domain/ports/out/config_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"github.com/gin-gonic/gin"
)

type ConfigOutPort interface {
	GetConfigBestSeller(ctx *gin.Context, countryID, property string) (model.ConfigBestSeller, error)
	GetConfigRelatedProducts(ctx *gin.Context, country, key string) (model.ConfigBestSeller, error)
}
