package http

import (
	"context"
	"encoding/json"
	"fmt"
	"ftd-td-home-read-services/internal/shared/utils"
	"ftd-td-home-read-services/internal/structure/domain/model"
	"ftd-td-home-read-services/internal/structure/domain/ports/out"
	"os"
)

const (
	getItemSectionStructureLog    = "ItemSectionRepository.GetItemSectionStructure"
	configPath                    = "internal/structure/infra/config/item_section_structure-%s.json"
	errorOpeningConfigFile        = "error opening item section structure config file for country %s: %v"
	errorDecodingConfigFile       = "error decoding item section structure config file for country %s: %v"
)

type itemSectionRepository struct{}

func NewItemSectionRepository() out.ItemSectionRepository {
	return &itemSectionRepository{}
}

func (r *itemSectionRepository) GetItemSectionStructure(
	ctx context.Context,
	countryID string,
) ([]model.ItemSection, error) {
	filePath := fmt.Sprintf(configPath, countryID)
	file, err := os.Open(filePath)
	if err != nil {
		utils.LogError(ctx, getItemSectionStructureLog, fmt.Sprintf(errorOpeningConfigFile, countryID, err))
		return nil, err
	}
	defer file.Close()

	var structure []model.ItemSection
	if err := json.NewDecoder(file).Decode(&structure); err != nil {
		utils.LogError(ctx, getItemSectionStructureLog, fmt.Sprintf(errorDecodingConfigFile, countryID, err))
		return nil, err
	}

	return structure, nil
}
