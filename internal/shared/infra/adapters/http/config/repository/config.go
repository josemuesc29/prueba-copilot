package repository

import (
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	pkgHttp "ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/config/mappers"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/config/model/response"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"net/http"
	"time"
)

const (
	retry                 = 1
	urlConfigReadProperty = "%s/config/r/%s/v1/property/%s"
	errorValidateResponse = "[%s] error in response data. Code: %s, value.active: %v, value.id: %s"
)

type Config struct {
}

func NewConfig() out.ConfigOutPort {
	return Config{}
}

func (t Config) GetConfigBestSeller(ctx *gin.Context, countryID, property string) (model.ConfigBestSeller, error) {
	var resp response.Config
	correlationID := ctx.Value(enums.HeaderCorrelationID).(string)

	url := fmt.Sprintf(urlConfigReadProperty, config.Enviroments.BalancerUrl, countryID, property)
	log.Println(fmt.Sprintf("[%s] URL Config read: %s", correlationID, url))

	_, err := pkgHttp.DoRequest(
		pkgHttp.Requestor{
			HttpMethod: http.MethodGet,
			MaxRetry:   retry,
			Backoff:    5 * time.Second,
			TTLTimeOut: 3 * time.Second,
			URL:        url,
			Body:       nil,
			Headers: http.Header{
				"Content-Type": {"application/json"},
				"Host":         {config.Enviroments.ConfigReadHostUrl},
			},
			Context: ctx,
		},
		&resp)

	if resp.Code != "OK" || !resp.Data.Active || resp.Data.ID != property {
		log.Println(fmt.Sprintf(errorValidateResponse,
			correlationID, resp.Code, resp.Data.Active, property))
		return model.ConfigBestSeller{}, fmt.Errorf(errorValidateResponse,
			correlationID, resp.Code, resp.Data.Active, property)
	}

	return mappers.MapConfigDtoToConfigBestSeller(resp), err
}
