package repository

import (
	"fmt"
	"ftd-td-home-read-services/cmd/config"
	"ftd-td-home-read-services/internal/carousel/domain/model"
	"ftd-td-home-read-services/internal/carousel/domain/ports/out"
	"ftd-td-home-read-services/internal/carousel/infra/adapters/http/cms/model/mappers"
	"ftd-td-home-read-services/internal/carousel/infra/adapters/http/cms/model/response"
	pkgHttp "ftd-td-home-read-services/internal/shared/infra/adapters/http"
	"github.com/gin-gonic/gin"
	"net/http"
	"time"
)

const (
	retrySuggested     = 2
	urlStrapiSuggested = "%s/%s/v1/carousel/suggest"
)

type suggested struct {
}

func NewSuggested() out.ProxyCMS {
	return &suggested{}
}

func (t suggested) GetSuggested(c *gin.Context, countryID string) (model.SuggestedCMS, error) {
	var resp response.SuggestedCMS

	_, err := pkgHttp.DoRequest(
		pkgHttp.Requestor{
			HttpMethod: http.MethodGet,
			MaxRetry:   retrySuggested,
			Backoff:    5 * time.Second,
			TTLTimeOut: 5 * time.Second,
			URL:        fmt.Sprintf(urlStrapiSuggested, config.Enviroments.ProxyCmsUrl, countryID),
			Body:       nil,
			Headers: http.Header{
				"Content-Type": {"application/json"},
			},
			Context: c,
		},
		&resp)

	return mappers.SuggestedCMSToDomainSuggestedCMS(resp), err
}
