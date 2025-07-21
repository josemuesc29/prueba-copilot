package repository

import (
	"context"
	"encoding/json"
	"fmt"
	"ftd-td-home-read-services/internal/shared/utils"
	"ftd-td-home-read-services/internal/structure/domain/model"
	"ftd-td-home-read-services/internal/structure/domain/ports/out"
	apimodel "ftd-td-home-read-services/internal/structure/infra/adapters/http/cms/model"
	"os"

	"github.com/jinzhu/copier"
)

const (
	retryGetHomeStructure  = 0
	urlCmsGetHomeStructure = "%s/%s/v1/structures/home"
	getHomeStructureLog    = "CmsStructureRepository.GetHomeStructure"
)

type cmsStructureRepository struct {
}

func NewStructureRepository() out.StructureRepository {
	return &cmsStructureRepository{}
}

func (s cmsStructureRepository) GetHomeStructure(
	ctx context.Context,
	countryID string,
	platform model.Platform,
) ([]model.Section, error) {
	// homeStructure, err := getHomeStructure(ctx, countryID, platform)
	homeStructure, err := getFallbackResponse(ctx, countryID, platform)

	var structure []model.Section

	if err == nil {
		err = copier.Copy(&structure, homeStructure)
		if err != nil {
			return nil, err
		}
	}

	return structure, err
}

/* func getHomeStructure(ctx context.Context, countryID string, platform model.Platform) ([]apimodel.Section, error) {
	var response sharedentity.ApiResponse[[]apimodel.Section]

	request := pkghttp.Requestor{
		HttpMethod: http.MethodGet,
		MaxRetry:   retryGetHomeStructure,
		Backoff:    0 * time.Second,
		TTLTimeOut: 5 * time.Second,
		URL:        fmt.Sprintf(urlCmsGetHomeStructure, config.Enviroments.ProxyCmsUrl, countryID),
		Body:       nil,
		Context:    ctx,
	}

	_, err := pkghttp.DoRequest(request, &response)

	if err != nil {
		utils.LogError(ctx, getHomeStructureLog, fmt.Sprintf("Failed to get home structure config from CMS: %v", err))
		utils.LogInfo(ctx, getHomeStructureLog, "Using fallback response")

		return getFallbackResponse(ctx, countryID, platform)
	}

	return response.Data, nil
} */

func getFallbackResponse(ctx context.Context, countryID string, platform model.Platform) ([]apimodel.Section, error) {
	fileName := fmt.Sprintf("fallback_home_structure_response-%s-%s.json", countryID, model.GetPlatformType(platform))
	path := utils.BuildPathFromProjectRoot("internal/structure/infra/config/" + fileName)
	data, err := os.ReadFile(path)

	if err != nil {
		utils.LogError(ctx, getHomeStructureLog, fmt.Sprintf("Error reading fallback structure: %v", err))
		return nil, fmt.Errorf("failed to read fallback structure: %v", err)
	}

	var response []apimodel.Section
	err = json.Unmarshal(data, &response)

	if err != nil {
		utils.LogError(ctx, getHomeStructureLog, fmt.Sprintf("Error unmarshaling fallback structure: %v", err))
		return nil, fmt.Errorf("failed to unmarshal fallback structure: %v", err)
	}

	return response, nil
}
