package app

import (
	"context"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
	"ftd-td-catalog-item-read-services/internal/structure/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/structure/domain/ports/out"
	"sort"
)

const (
	getStructureLog = "ItemStructureService.GetItemStructure"
)

type itemStructureService struct {
	structureRepository out.ItemStructureRepository
}

func NewItemStructureService(
	structureRepository out.ItemStructureRepository,
) in.ItemStructureService {
	return &itemStructureService{
		structureRepository: structureRepository,
	}
}

func (s *itemStructureService) GetItemStructure(
	ctx context.Context,
	countryID string,
	itemID string,
) ([]model.Component, error) {
	structure, err := s.structureRepository.GetItemStructure(ctx, countryID)

	if err != nil {
		return nil, err
	}

	processedStructure := processComponents(countryID, itemID, structure)

	return processedStructure, nil
}

func processComponents(
	countryID string,
	itemID string,
	components []model.Component,
) []model.Component {
	var filteredComponents []model.Component

	for _, component := range components {
		if component.Active {
			processComponentPlaceholders(countryID, itemID, &component)
			filteredComponents = append(filteredComponents, component)
		}
	}

	sort.Slice(filteredComponents, func(i, j int) bool {
		return filteredComponents[i].Position < filteredComponents[j].Position
	})

	return filteredComponents
}

func processComponentPlaceholders(countryID string, itemID string, component *model.Component) {
	replacements := map[string]string{
		"countryId": countryID,
		"itemId":    itemID,
	}

	if component.ServiceUrl != nil {
		component.ServiceUrl = utils.ReplacePlaceholders(component.ServiceUrl, replacements)
	}
	if component.RedirectUrl != nil {
		component.RedirectUrl = utils.ReplacePlaceholders(component.RedirectUrl, replacements)
	}
}
