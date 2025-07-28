package app

import (
	"context"
	"encoding/json"
	"fmt"
	"ftd-td-catalog-item-read-services/cmd/config"
	sharedout "ftd-td-catalog-item-read-services/internal/shared/domain/ports/out"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
	"ftd-td-catalog-item-read-services/internal/structure/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/structure/domain/ports/out"
	"sort"
	"time"
)

const (
	getStructureLog       = "ItemStructureService.GetItemStructure"
	itemStructureCacheKey = "item_structure_%s_%s"
)

type itemStructureService struct {
	structureRepository out.ItemStructureRepository
	cacheRepository     sharedout.Cache
}

func NewItemStructureService(
	structureRepository out.ItemStructureRepository,
	cacheRepository sharedout.Cache,
) in.ItemStructureService {
	return &itemStructureService{
		structureRepository: structureRepository,
		cacheRepository:     cacheRepository,
	}
}

func (s *itemStructureService) GetItemStructure(
	ctx context.Context,
	countryID string,
	itemID string,
) ([]model.Component, error) {
	structure, err := s.getItemStructure(ctx, countryID, itemID)

	if err != nil {
		return nil, err
	}

	processedStructure := processComponents(countryID, itemID, structure)

	return processedStructure, nil
}

func (s *itemStructureService) getItemStructure(
	ctx context.Context,
	countryID string,
	itemID string,
) ([]model.Component, error) {
	cacheKey := fmt.Sprintf(itemStructureCacheKey, countryID, itemID)
	structure, err := s.getItemStructureFromCache(ctx, cacheKey)

	if err == nil && structure != nil {
		return structure, nil
	}

	structure, err = s.structureRepository.GetItemStructure(ctx, countryID)

	if err == nil {
		s.saveItemStructureToCache(ctx, cacheKey, structure)
	}

	return structure, err
}

func (s *itemStructureService) getItemStructureFromCache(ctx context.Context, cacheKey string) ([]model.Component, error) {
	cachedData, err := s.cacheRepository.Get(ctx, cacheKey)

	if err != nil || cachedData == "" {
		utils.LogWarn(ctx, getStructureLog, fmt.Sprintf("Cache miss for key %s: %v", cacheKey, err))
		return nil, err
	}

	var structure []model.Component

	if err := json.Unmarshal([]byte(cachedData), &structure); err != nil {
		utils.LogError(ctx, getStructureLog, fmt.Sprintf("Error unmarshalling item structure cached data: %v", err))
		return nil, err
	}

	return structure, nil
}

func (s *itemStructureService) saveItemStructureToCache(ctx context.Context, cacheKey string, structure []model.Component) {
	if data, err := json.Marshal(structure); err == nil {
		cacheTtl := time.Duration(config.Enviroments.RedisItemStructureTTL) * time.Minute

		err = s.cacheRepository.Set(ctx, cacheKey, string(data), cacheTtl)

		if err != nil {
			utils.LogError(ctx, getStructureLog, fmt.Sprintf("Error saving item structure to cache: %v", err))
		}
	}
}

func processComponents(
	countryID string,
	itemID string,
	components []model.Component,
) []model.Component {
	var filteredComponents []model.Component

	for _, component := range components {
		processComponentPlaceholders(countryID, itemID, &component)
		filteredComponents = append(filteredComponents, component)
	}

	sort.Slice(filteredComponents, func(i, j int) bool {
		return filteredComponents[i].Order < filteredComponents[j].Order
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
