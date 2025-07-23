package repository

import (
	"context"
	"fmt"
	pkghttp "ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
	"ftd-td-catalog-item-read-services/internal/structure/domain/ports/out"
	apimodel "ftd-td-catalog-item-read-services/internal/structure/infra/adapters/cms/model/response"
	nethttp "net/http"
	"time"

	"github.com/jinzhu/copier"
)

const (
	retryGetItemStructure   = 0
	pathCmsGetItemStructure = "/v1/structures/item-detail"
	getItemStructureLog     = "CmsItemStructureRepository.GetItemStructure"
)

type cmsItemStructureRepository struct {
	cmsBaseURL string
}

func NewItemStructureRepository(cmsBaseURL string) out.ItemStructureRepository {
	return &cmsItemStructureRepository{cmsBaseURL: cmsBaseURL}
}

func (s *cmsItemStructureRepository) GetItemStructure(ctx context.Context, countryID string) ([]model.Component, error) {
	var apiResponse []apimodel.Component
	var structure []model.Component

	url := fmt.Sprintf("%s%s", s.cmsBaseURL, pathCmsGetItemStructure)
	headers := make(nethttp.Header)
	headers.Set("countryId", countryID)

	req := pkghttp.Requestor{
		HttpMethod: nethttp.MethodGet,
		MaxRetry:   retryGetItemStructure,
		URL:        url,
		Context:    ctx,
		TTLTimeOut: 5 * time.Second,
		Headers:    headers,
	}

	_, err := pkghttp.DoRequest(req, &apiResponse)
	if err != nil {
		utils.LogError(ctx, getItemStructureLog, fmt.Sprintf("Failed to get item structure from CMS: %v", err))
		return nil, err
	}

	if err := copier.Copy(&structure, &apiResponse); err != nil {
		utils.LogError(ctx, getItemStructureLog, fmt.Sprintf("Error mapping item structure response: %v", err))
		return nil, err
	}

	return structure, nil
}
