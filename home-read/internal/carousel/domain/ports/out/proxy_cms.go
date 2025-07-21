package out

//go:generate mockgen -source=proxy_cms.go -destination=../../../../../test/mocks/carousel/domain/ports/out/proxy_cms_mock.go

import (
	"ftd-td-home-read-services/internal/carousel/domain/model"
	"github.com/gin-gonic/gin"
)

type ProxyCMS interface {
	GetSuggested(c *gin.Context, countryID string) (model.SuggestedCMS, error)
}
