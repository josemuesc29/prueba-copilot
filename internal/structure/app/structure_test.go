package app

import (
	"context"
	"errors"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
	mock_shared_ports "ftd-td-catalog-item-read-services/test/mocks/shared/domain/ports/out"
	mock_out "ftd-td-catalog-item-read-services/test/mocks/structure/domain/ports/out"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
	"testing"
)

func stringPointer(s string) *string {
	return &s
}

func TestItemStructureService_GetItemStructure_Success_From_Cache(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockRepo := mock_out.NewMockItemStructureRepository(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	service := NewItemStructureService(mockRepo, mockCache)

	countryID := "AR"
	itemID := "MLA12345"
	cacheKey := "item_structure_AR_MLA12345"
	cachedData := `[{"label":"Item Principal","componentType":"MAIN_ITEM","serviceUrl":"/catalog-item/r/AR/v1/items/MLA12345","redirectUrl":"/catalog-item/r/AR/v1/items/MLA12345","order":1}]`

	mockCache.
		EXPECT().
		Get(gomock.Any(), cacheKey).
		Return(cachedData, nil)

	expected := []model.Component{
		{
			Label:         "Item Principal",
			ComponentType: model.MainItemComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/AR/v1/items/MLA12345"),
			RedirectUrl:   stringPointer("/catalog-item/r/AR/v1/items/MLA12345"),
			Order:         1,
		},
	}

	// Act
	result, err := service.GetItemStructure(context.Background(), countryID, itemID)

	// Assert
	assert.NoError(t, err)
	assert.Equal(t, expected, result)
}

func TestItemStructureService_GetItemStructure_Success_From_Repository(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockRepo := mock_out.NewMockItemStructureRepository(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	service := NewItemStructureService(mockRepo, mockCache)

	countryID := "AR"
	itemID := "MLA12345"
	cacheKey := "item_structure_AR_MLA12345"

	repoResponse := []model.Component{
		{
			Label:         "Item Principal",
			ComponentType: model.MainItemComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/{countryId}/v1/items/{itemId}"),
			RedirectUrl:   stringPointer("/catalog-item/r/AR/v1/items/MLA12345"),
			Order:         1,
		},
	}

	expected := []model.Component{
		{
			Label:         "Item Principal",
			ComponentType: model.MainItemComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/AR/v1/items/MLA12345"),
			RedirectUrl:   stringPointer("/catalog-item/r/AR/v1/items/MLA12345"),
			Order:         1,
		},
	}

	mockCache.
		EXPECT().
		Get(gomock.Any(), cacheKey).
		Return("", errors.New("cache miss"))

	mockRepo.
		EXPECT().
		GetItemStructure(gomock.Any(), countryID).
		Return(repoResponse, nil)

	mockCache.
		EXPECT().
		Set(gomock.Any(), cacheKey, gomock.Any(), gomock.Any()).
		Return(nil)

	// Act
	result, err := service.GetItemStructure(context.Background(), countryID, itemID)

	// Assert
	assert.NoError(t, err)
	assert.Equal(t, expected, result)
}
