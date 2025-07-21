package app

import (
	"context"
	"fmt"
	"ftd-td-home-read-services/internal/shared/utils"
	"ftd-td-home-read-services/internal/structure/domain/model"
	"ftd-td-home-read-services/internal/structure/domain/ports/in"
	"ftd-td-home-read-services/internal/structure/domain/ports/out"
)

const (
	getItemSectionStructureLog = "ItemSectionService.GetItemSectionStructure"
)

type itemSectionService struct {
	itemSectionRepository out.ItemSectionRepository
}

func NewItemSectionService(
	itemSectionRepository out.ItemSectionRepository,
) in.ItemSectionService {
	return &itemSectionService{
		itemSectionRepository: itemSectionRepository,
	}
}

func (s *itemSectionService) GetItemSectionStructure(
	ctx context.Context,
	countryID string,
	itemID string,
) ([]model.ItemSection, error) {
	structure, err := s.itemSectionRepository.GetItemSectionStructure(ctx, countryID)
	if err != nil {
		utils.LogError(ctx, getItemSectionStructureLog, fmt.Sprintf("Error getting item section structure for country '%s': %v", countryID, err))
		return nil, err
	}

	processedStructure := processItemSections(countryID, itemID, structure)

	return processedStructure, nil
}

func processItemSections(
	countryID string,
	itemID string,
	sections []model.ItemSection,
) []model.ItemSection {
	var processedSections []model.ItemSection

	for _, section := range sections {
		processItemSectionPlaceholders(countryID, itemID, &section)
		processedSections = append(processedSections, section)
	}

	return processedSections
}

func processItemSectionPlaceholders(countryID string, itemID string, section *model.ItemSection) {
	replacements := map[string]string{
		"countryId": countryID,
		"itemId":    itemID,
	}

	serviceUrl := utils.ReplacePlaceholders(&section.ServiceUrl, replacements)
	if serviceUrl != nil {
		section.ServiceUrl = *serviceUrl
	}

	redirectUrl := utils.ReplacePlaceholders(&section.RedirectUrl, replacements)
	if redirectUrl != nil {
		section.RedirectUrl = *redirectUrl
	}
}
