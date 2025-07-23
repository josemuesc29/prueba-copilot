package app

import (
	"context"
	"errors"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
	"testing"

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

func stringPointer(s string) *string {
	return &s
}

func TestItemStructureService_GetItemStructure_Success(t *testing.T) {
	// Arrange
	mockRepo := new(mockItemStructureRepository)
	service := NewItemStructureService(mockRepo)

	countryID := "AR"
	itemID := "MLA12345"

	mockResponse := []model.Component{
		{
			Label:         "Item Principal",
			ComponentType: model.MainItemComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/{countryId}/v1/items/{itemId}"),
			Position:      1,
			Active:        true,
		},
		{
			Label:         "Inactivo",
			ComponentType: "INACTIVE",
			Position:      2,
			Active:        false,
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

	mockRepo.On("GetItemStructure", mock.Anything, countryID).Return(mockResponse, nil)

	// Act
	result, err := service.GetItemStructure(context.Background(), countryID, itemID)

	// Assert
	assert.NoError(t, err)
	assert.Equal(t, expectedResponse, result)
	mockRepo.AssertExpectations(t)
}

func TestItemStructureService_GetItemStructure_RepositoryError(t *testing.T) {
	// Arrange
	mockRepo := new(mockItemStructureRepository)
	service := NewItemStructureService(mockRepo)

	countryID := "AR"
	itemID := "MLA12345"
	repoError := errors.New("repository error")

	mockRepo.On("GetItemStructure", mock.Anything, countryID).Return([]model.Component{}, repoError)

	// Act
	result, err := service.GetItemStructure(context.Background(), countryID, itemID)

	// Assert
	assert.Error(t, err)
	assert.Nil(t, result)
	assert.Equal(t, repoError, err)
	mockRepo.AssertExpectations(t)
}
