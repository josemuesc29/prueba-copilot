package http

import (
	"context"
	"encoding/json"
	"fmt"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
	"ftd-td-catalog-item-read-services/internal/structure/domain/ports/out"
	"os"
)

const (
	getItemStructureLog         = "ItemStructureRepository.GetItemStructure"
	errorGettingItemStructure   = "error getting item structure for country '%s': %w"
	errorUnmarshallingStructure = "error unmarshalling item structure for country '%s': %w"
	configPath                  = "internal/structure/infra/config/item_section_structure-%s.json"
)

type itemStructureRepository struct{}

func NewItemStructureRepository() out.ItemStructureRepository {
	return &itemStructureRepository{}
}

func (r *itemStructureRepository) GetItemStructure(ctx context.Context, countryID string) ([]model.Component, error) {
	var structure []model.Component

	filePath := fmt.Sprintf(configPath, countryID)
	file, err := os.ReadFile(filePath)
	if err != nil {
		utils.LogError(ctx, getItemStructureLog, fmt.Sprintf(errorGettingItemStructure, countryID, err))
		return nil, fmt.Errorf(errorGettingItemStructure, countryID, err)
	}

	err = json.Unmarshal(file, &structure)
	if err != nil {
		utils.LogError(ctx, getItemStructureLog, fmt.Sprintf(errorUnmarshallingStructure, countryID, err))
		return nil, fmt.Errorf(errorUnmarshallingStructure, countryID, err)
	}

	return structure, nil
}
