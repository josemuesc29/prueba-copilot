package repository

import (
	"context"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	pkghttp "ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
	"ftd-td-catalog-item-read-services/internal/structure/domain/ports/out"
	apimodel "ftd-td-catalog-item-read-services/internal/structure/infra/adapters/http/cms/model/response"
	nethttp "net/http"
	"time"

	"github.com/jinzhu/copier"
)

const (
	retryGetItemStructure  = 0
	urlCmsGetItemStructure = "%s/%s/v1/structure/detail-product"
	getItemStructureLog    = "CmsItemStructureRepository.GetItemStructure"
)

type cmsItemStructureRepository struct {
}

func NewItemStructureRepository() out.ItemStructureRepository {
	return &cmsItemStructureRepository{}
}

func (s *cmsItemStructureRepository) GetItemStructure(ctx context.Context, countryID string) ([]model.Component, error) {
	var apiResponse apimodel.ItemStructureResponse
	var structure []model.Component

	headers := make(nethttp.Header)
	headers.Set("countryId", countryID)

	req := pkghttp.Requestor{
		HttpMethod: nethttp.MethodGet,
		MaxRetry:   retryGetItemStructure,
		URL:        fmt.Sprintf(urlCmsGetItemStructure, config.Enviroments.ProxyCmsUrl, countryID),
		Context:    ctx,
		TTLTimeOut: 5 * time.Second,
		Headers:    headers,
	}

	_, err := pkghttp.DoRequest(req, &apiResponse)
	if err != nil {
		utils.LogError(ctx, getItemStructureLog, fmt.Sprintf("Failed to get item structure from CMS: %v", err))
		return nil, err
	}

	if err := copier.Copy(&structure, &apiResponse.Data); err != nil {
		utils.LogError(ctx, getItemStructureLog, fmt.Sprintf("Error mapping item structure response: %v", err))
		return nil, err
	}

	return structure, nil
}
