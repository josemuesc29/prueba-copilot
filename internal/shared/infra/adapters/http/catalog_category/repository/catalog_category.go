package repository

import (
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	pkgHttp "ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/catalog_category/mappers"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/catalog_category/model/response"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
	"net/http"
	"time"
)

const (
	retry                 = 1
	urlCatalogCategory    = "%s/catalog/r/%s/v1/categories/%s"
	errorValidateResponse = "[%s] error in response data. Code: %s, value.active: %v"
)

type catalogCategory struct {
}

func NewCatalogCategory() out.CatalogCategoryOutPort {
	return catalogCategory{}
}

func (t catalogCategory) GetCategoryByDepartment(ctx *gin.Context, countryID, departmentID string) (model.CatalogCategory, error) {
	var resp response.CatalogCategory
	correlationID := ctx.Value(enums.HeaderCorrelationID).(string)

	url := fmt.Sprintf(urlCatalogCategory, config.Enviroments.BalancerUrl, countryID, departmentID)
	log.Println(fmt.Sprintf("[%s] URL Catalog category: %s", correlationID, url))

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
				"Host":         {config.Enviroments.CatalogCategoryHostUrl},
			},
			Context: ctx,
		},
		&resp)

	if resp.Code != "OK" || !resp.Data.Active {
		log.Println(fmt.Sprintf(errorValidateResponse,
			correlationID, resp.Code, resp.Data.Active))
		return model.CatalogCategory{}, fmt.Errorf(errorValidateResponse,
			correlationID, resp.Code, resp.Data.Active)
	}

	return mappers.MapCatalogCategoryDtoToCatalogCategory(resp), err
}
