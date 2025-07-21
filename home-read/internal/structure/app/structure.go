package app

import (
	"context"
	"encoding/json"
	"fmt"
	"ftd-td-home-read-services/cmd/config"
	sharedout "ftd-td-home-read-services/internal/shared/domain/ports/out"
	"ftd-td-home-read-services/internal/shared/utils"
	"ftd-td-home-read-services/internal/structure/domain/model"
	"ftd-td-home-read-services/internal/structure/domain/ports/in"
	"ftd-td-home-read-services/internal/structure/domain/ports/out"
	"slices"
	"sort"
	"time"
)

const (
	homeStructureCacheKey = "home_structure_%s_%s"
	getStructureLog       = "StructureService.GetStructure"
)

type structureService struct {
	structureRepository out.StructureRepository
	customerRepository  out.CustomerRepository
	cacheRepository     sharedout.Cache
}

func NewStructureService(
	structureRepository out.StructureRepository,
	customerRepository out.CustomerRepository,
	cacheRepository sharedout.Cache,
) in.StructureService {
	return &structureService{
		structureRepository: structureRepository,
		customerRepository:  customerRepository,
		cacheRepository:     cacheRepository,
	}
}

func (s *structureService) GetStructure(
	ctx context.Context,
	countryID string,
	platform model.Platform,
	customer *model.Customer,
) ([]model.Section, error) {
	structure, err := s.getHomeStructure(ctx, countryID, platform)

	if err != nil {
		return nil, err
	}

	customer = s.resolveCustomer(ctx, countryID, customer)
	processedStructure := processSections(countryID, structure, platform, customer)

	return processedStructure, nil
}

func (s *structureService) resolveCustomer(ctx context.Context, countryID string, customer *model.Customer) *model.Customer {
	if customer == nil || customer.ID == "" {
		return nil
	}

	if customer.FirstName != "" {
		return customer
	}

	fetchedCustomer, err := s.customerRepository.GetCustomerByID(ctx, countryID, customer.ID)

	if err != nil {
		return nil
	}

	return &fetchedCustomer
}

func (s *structureService) getHomeStructure(
	ctx context.Context,
	countryID string,
	platform model.Platform,
) ([]model.Section, error) {
	cacheKey := fmt.Sprintf(homeStructureCacheKey, countryID, model.GetPlatformType(platform))
	structure, err := s.getHomeStructureFromCache(ctx, cacheKey)

	if err == nil && structure != nil {
		return structure, nil
	}

	structure, err = s.structureRepository.GetHomeStructure(ctx, countryID, platform)

	if err == nil {
		go s.saveHomeStructureToCache(ctx, cacheKey, structure)
	}

	return structure, err
}

func (s *structureService) getHomeStructureFromCache(ctx context.Context, cacheKey string) ([]model.Section, error) {
	cachedData, err := s.cacheRepository.Get(ctx, cacheKey)

	if err != nil || cachedData == "" {
		utils.LogWarn(ctx, getStructureLog, fmt.Sprintf("Cache miss for key %s: %v", cacheKey, err))
		return nil, err
	}

	var structure []model.Section

	if err := json.Unmarshal([]byte(cachedData), &structure); err != nil {
		utils.LogError(ctx, getStructureLog, fmt.Sprintf("Error unmarshalling home structure cached data: %v", err))
		return nil, err
	}

	return structure, nil
}

func (s *structureService) saveHomeStructureToCache(ctx context.Context, cacheKey string, structure []model.Section) {
	if data, err := json.Marshal(structure); err == nil {
		cacheTtl := time.Duration(config.Enviroments.CacheHomeStructureTTL) * time.Minute

		err = s.cacheRepository.Set(ctx, cacheKey, string(data), cacheTtl)

		if err != nil {
			utils.LogError(ctx, getStructureLog, fmt.Sprintf("Error saving home structure to cache: %v", err))
		}
	}
}

func processSections(
	countryID string,
	sections []model.Section,
	platform model.Platform,
	customer *model.Customer,
) []model.Section {
	var filteredSections []model.Section

	for _, component := range sections {
		components := processComponents(countryID, component.Components, platform, customer)

		if len(components) > 0 {
			filteredSections = append(filteredSections, model.Section{
				ID:         component.ID,
				Title:      component.Title,
				Components: components,
			})
		}
	}

	return filteredSections
}

func processComponents(
	countryID string,
	components []model.Component,
	platform model.Platform,
	customer *model.Customer,
) []model.Component {
	var filteredComponents []model.Component

	for _, component := range components {
		if isComponentEligible(component, platform, customer) {
			processComponentPlaceholders(countryID, &component, customer)
			filteredComponents = append(filteredComponents, component)
		}
	}

	sort.Slice(filteredComponents, func(i, j int) bool {
		return filteredComponents[i].Position < filteredComponents[j].Position
	})

	return filteredComponents
}

func isComponentEligible(component model.Component, platform model.Platform, customer *model.Customer) bool {
	return component.Active &&
		(platform == model.Web || slices.Contains(component.EnableFor, platform)) &&
		isComponentVisibleForCustomer(component, customer)
}

func isComponentVisibleForCustomer(component model.Component, customer *model.Customer) bool {
	isLoggedIn := customer != nil

	return (isLoggedIn && slices.Contains(component.VisibleFor, model.UserTypeLoggedIn)) ||
		(!isLoggedIn && slices.Contains(component.VisibleFor, model.UserTypeAnonymous))
}

func processComponentPlaceholders(countryID string, component *model.Component, customer *model.Customer) {
	if customer == nil {
		customer = &model.Customer{}
	}

	replacements := map[string]string{
		"firstName": customer.FirstName,
		"lastName":  customer.LastName,
		"countryId": countryID,
	}

	component.Label = utils.ReplacePlaceholders(component.Label, replacements)
	component.ServiceUrl = utils.ReplacePlaceholders(component.ServiceUrl, replacements)
	component.RedirectUrl = utils.ReplacePlaceholders(component.RedirectUrl, replacements)
}
