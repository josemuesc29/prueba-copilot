package app

import (
	"context"
	"errors"
	"ftd-td-home-read-services/internal/structure/domain/model"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

type MockItemSectionRepository struct {
	mock.Mock
}

func (m *MockItemSectionRepository) GetItemSectionStructure(ctx context.Context, countryID string) ([]model.ItemSection, error) {
	args := m.Called(ctx, countryID)
	return args.Get(0).([]model.ItemSection), args.Error(1)
}

func TestItemSectionService_GetItemSectionStructure(t *testing.T) {
	mockRepo := new(MockItemSectionRepository)
	service := NewItemSectionService(mockRepo)

	ctx := context.Background()
	countryID := "AR"
	itemID := "123"

	t.Run("Success", func(t *testing.T) {
		repoResponse := []model.ItemSection{
			{
				Label:         "Item Principal",
				ComponentType: "MAIN_ITEM",
				ServiceUrl:    "/catalog-item/r/{countryId}/v1/items/{itemId}",
			},
		}
		expectedResponse := []model.ItemSection{
			{
				Label:         "Item Principal",
				ComponentType: "MAIN_ITEM",
				ServiceUrl:    "/catalog-item/r/AR/v1/items/123",
			},
		}

		mockRepo.On("GetItemSectionStructure", ctx, countryID).Return(repoResponse, nil).Once()

		result, err := service.GetItemSectionStructure(ctx, countryID, itemID)

		assert.NoError(t, err)
		assert.Equal(t, expectedResponse, result)
		mockRepo.AssertExpectations(t)
	})

	t.Run("RepositoryError", func(t *testing.T) {
		mockRepo.On("GetItemSectionStructure", ctx, countryID).Return([]model.ItemSection{}, errors.New("repo error")).Once()

		result, err := service.GetItemSectionStructure(ctx, countryID, itemID)

		assert.Error(t, err)
		assert.Nil(t, result)
		mockRepo.AssertExpectations(t)
	})
}
