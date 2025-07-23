package app

import (
	"context"
	"errors"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

type mockItemStructureRepository struct {
	mock.Mock
}

func (m *mockItemStructureRepository) GetItemStructure(ctx context.Context, countryID string) ([]model.Component, error) {
	args := m.Called(ctx, countryID)
	return args.Get(0).([]model.Component), args.Error(1)
}

type mockCacheRepository struct {
	mock.Mock
}

func (m *mockCacheRepository) Get(ctx context.Context, key string) (string, error) {
	args := m.Called(ctx, key)
	return args.String(0), args.Error(1)
}

func (m *mockCacheRepository) Set(ctx context.Context, key string, value string, ttl time.Duration) error {
	args := m.Called(ctx, key, value, ttl)
	return args.Error(0)
}

func (m *mockCacheRepository) Delete(ctx context.Context, key string) error {
	args := m.Called(ctx, key)
	return args.Error(0)
}

func stringPointer(s string) *string {
	return &s
}

func TestItemStructureService_GetItemStructure_Success_From_Cache(t *testing.T) {
	// Arrange
	mockRepo := new(mockItemStructureRepository)
	mockCache := new(mockCacheRepository)
	service := NewItemStructureService(mockRepo, mockCache)

	countryID := "AR"
	itemID := "MLA12345"
	cacheKey := "item_structure_AR"
	cachedData := `[{"label":"Item Principal","componentType":"MAIN_ITEM","serviceUrl":"/catalog-item/r/AR/v1/items/MLA12345","position":1,"active":true}]`

	mockCache.On("Get", mock.Anything, cacheKey).Return(cachedData, nil)

	expectedResponse := []model.Component{
		{
			Label:         "Item Principal",
			ComponentType: model.MainItemComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/AR/v1/items/MLA12345"),
			Position:      1,
			Active:        true,
		},
	}

	// Act
	result, err := service.GetItemStructure(context.Background(), countryID, itemID)

	// Assert
	assert.NoError(t, err)
	assert.Equal(t, expectedResponse, result)
	mockCache.AssertExpectations(t)
	mockRepo.AssertNotCalled(t, "GetItemStructure")
}

func TestItemStructureService_GetItemStructure_Success_From_Repository(t *testing.T) {
	// Arrange
	mockRepo := new(mockItemStructureRepository)
	mockCache := new(mockCacheRepository)
	service := NewItemStructureService(mockRepo, mockCache)

	countryID := "AR"
	itemID := "MLA12345"
	cacheKey := "item_structure_AR"

	mockResponse := []model.Component{
		{
			Label:         "Item Principal",
			ComponentType: model.MainItemComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/{countryId}/v1/items/{itemId}"),
			Position:      1,
			Active:        true,
		},
	}

	expectedResponse := []model.Component{
		{
			Label:         "Item Principal",
			ComponentType: model.MainItemComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/AR/v1/items/MLA12345"),
			Position:      1,
			Active:        true,
		},
	}

	mockCache.On("Get", mock.Anything, cacheKey).Return("", errors.New("cache miss"))
	mockRepo.On("GetItemStructure", mock.Anything, countryID).Return(mockResponse, nil)
	mockCache.On("Set", mock.Anything, cacheKey, mock.AnythingOfType("string"), mock.AnythingOfType("time.Duration")).Return(nil)

	// Act
	result, err := service.GetItemStructure(context.Background(), countryID, itemID)

	// Assert
	assert.NoError(t, err)
	assert.Equal(t, expectedResponse, result)

	time.Sleep(10 * time.Millisecond) // Give goroutine time to run

	mockCache.AssertExpectations(t)
	mockRepo.AssertExpectations(t)
}
