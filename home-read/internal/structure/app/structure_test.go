package app

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"ftd-td-home-read-services/internal/shared/utils"
	"ftd-td-home-read-services/internal/structure/domain/model"
	sharedmockout "ftd-td-home-read-services/test/mocks/shared/domain/ports/out"
	mockout "ftd-td-home-read-services/test/mocks/structure/domain/ports/out"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

const (
	countryID              = "AR"
	platform               = model.Android
	headerCorrelationValue = "1291823jhau1uha"
)

var (
	homeStructureCacheKeyMock = fmt.Sprintf("home_structure_%s_%s", countryID, model.GetPlatformType(platform))
	repositoryResponseMobile  = []model.Section{
		{
			ID:    1,
			Title: "SECCION 1",
			Components: []model.Component{
				{
					Type:       model.ComponentMainBanner,
					EnableFor:  []model.Platform{model.Ios, model.Android},
					ServiceUrl: utils.StringPtr("/home/r/{countryId}/v1/banners/main"),
					Active:     false,
					VisibleFor: []model.UserType{model.UserTypeLoggedIn, model.UserTypeAnonymous},
					Position:   1,
				},
				{
					Type:        model.ComponentSuggests,
					EnableFor:   []model.Platform{model.Ios, model.Android, model.Responsive},
					RedirectUrl: nil,
					ServiceUrl:  utils.StringPtr("/home/r/{countryId}/v1/suggests"),
					Label:       utils.StringPtr("<font color='#418fde'>Hola {firstName}, </font>esto te puede interesar"),
					LabelColor:  utils.StringPtr("#418FDE"),
					Active:      true,
					VisibleFor:  []model.UserType{model.UserTypeLoggedIn, model.UserTypeAnonymous},
					Position:    2,
				},
				{
					Type:       model.ComponentSecondaryBanner,
					EnableFor:  []model.Platform{model.Ios},
					Active:     true,
					VisibleFor: []model.UserType{model.UserTypeLoggedIn, model.UserTypeAnonymous},
					Position:   3,
				},
				{
					Type:       model.ComponentFavorites,
					EnableFor:  []model.Platform{model.Ios, model.Android, model.Responsive},
					Active:     true,
					VisibleFor: []model.UserType{model.UserTypeLoggedIn},
					Position:   4,
				},
			},
		},
	}
	repositoryResponseWeb = []model.Section{
		{
			ID:    1,
			Title: "SECCION 1",
			Components: []model.Component{
				{
					Type:       model.ComponentMainBanner,
					ServiceUrl: utils.StringPtr("/home/r/{countryId}/v1/banners/main"),
					Active:     false,
					VisibleFor: []model.UserType{model.UserTypeLoggedIn, model.UserTypeAnonymous},
					Position:   1,
				},
				{
					Type:        model.ComponentSuggests,
					RedirectUrl: nil,
					ServiceUrl:  utils.StringPtr("/home/r/{countryId}/v1/suggests"),
					Label:       utils.StringPtr("<font color='#418fde'>Hola {firstName}, </font>esto te puede interesar"),
					LabelColor:  utils.StringPtr("#418FDE"),
					Active:      true,
					VisibleFor:  []model.UserType{model.UserTypeLoggedIn, model.UserTypeAnonymous},
					Position:    2,
				},
				{
					Type:       model.ComponentFavorites,
					Active:     true,
					VisibleFor: []model.UserType{model.UserTypeLoggedIn},
					Position:   4,
				},
			},
		},
	}

	loggedInCustomer = &model.Customer{ID: "123", FirstName: "John"}
)

func TestGetStructureShouldReturnActiveComponentsForWebAndLoggedInUser(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	wg := createWaitGroup()
	defer wg.Wait()

	mockStructureRepo := mockout.NewMockStructureRepository(ctrl)
	mockCustomerRepo := mockout.NewMockCustomerRepository(ctrl)
	mockCacheRepo := sharedmockout.NewMockCache(ctrl)

	service := NewStructureService(mockStructureRepo, mockCustomerRepo, mockCacheRepo)

	platform := model.Web
	homeStructureCacheKeyMock := fmt.Sprintf("home_structure_%s_%s", countryID, model.GetPlatformType(platform))
	ctx := context.Background()

	mockCacheRepo.EXPECT().Get(ctx, homeStructureCacheKeyMock).Return("", nil).Times(1)
	mockStructureRepo.EXPECT().GetHomeStructure(ctx, countryID, platform).Return(repositoryResponseWeb, nil).Times(1)
	mockCacheRepo.EXPECT().Set(gomock.Any(), homeStructureCacheKeyMock, gomock.Any(), gomock.Any()).
		Do(func(context.Context, string, string, time.Duration) { wg.Done() })

	// Act
	structure, err := service.GetStructure(ctx, countryID, platform, loggedInCustomer)

	// Assert
	assert.NoError(t, err)
	assert.Len(t, structure, 1)
	assert.Len(t, structure[0].Components, 2)
	assert.True(t, structure[0].Components[0].Active)
	assert.True(t, strings.Contains(*structure[0].Components[0].ServiceUrl, countryID))
	assert.True(t, strings.Contains(*structure[0].Components[0].Label, loggedInCustomer.FirstName))
}

func TestGetStructureShouldReturnAnonymousComponentsWhenCustomerIsNil(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	wg := createWaitGroup()
	defer wg.Wait()

	mockStructureRepo := mockout.NewMockStructureRepository(ctrl)
	mockCustomerRepo := mockout.NewMockCustomerRepository(ctrl)
	mockCacheRepo := sharedmockout.NewMockCache(ctrl)

	service := NewStructureService(mockStructureRepo, mockCustomerRepo, mockCacheRepo)

	ctx := context.Background()

	mockCacheRepo.EXPECT().Get(ctx, homeStructureCacheKeyMock).Return("", nil).Times(1)
	mockStructureRepo.EXPECT().GetHomeStructure(ctx, countryID, platform).Return(repositoryResponseMobile, nil).Times(1)
	mockCacheRepo.EXPECT().Set(gomock.Any(), homeStructureCacheKeyMock, gomock.Any(), gomock.Any()).
		Do(func(context.Context, string, string, time.Duration) { wg.Done() })

	// Act
	structure, err := service.GetStructure(ctx, countryID, platform, nil)

	// Assert
	assert.NoError(t, err)
	assert.Len(t, structure, 1)
	assert.Len(t, structure[0].Components, 1)
	assert.True(t, structure[0].Components[0].Active)
	assert.True(t, strings.Contains(*structure[0].Components[0].ServiceUrl, countryID))
}

func TestGetStructureShouldGetCustomerAndReturnComponentsWhenOnlyCustomerID(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	wg := createWaitGroup()
	defer wg.Wait()

	mockStructureRepo := mockout.NewMockStructureRepository(ctrl)
	mockCustomerRepo := mockout.NewMockCustomerRepository(ctrl)
	mockCacheRepo := sharedmockout.NewMockCache(ctrl)

	service := NewStructureService(mockStructureRepo, mockCustomerRepo, mockCacheRepo)

	ctx := context.Background()

	customerRequest := &model.Customer{ID: "123"}
	customerResponse := &model.Customer{ID: "123", FirstName: "John"}

	mockCacheRepo.EXPECT().Get(ctx, homeStructureCacheKeyMock).Return("", nil).Times(1)
	mockStructureRepo.EXPECT().GetHomeStructure(ctx, countryID, platform).Return(repositoryResponseMobile, nil).Times(1)
	mockCacheRepo.EXPECT().Set(gomock.Any(), homeStructureCacheKeyMock, gomock.Any(), gomock.Any()).
		Do(func(context.Context, string, string, time.Duration) { wg.Done() })

	mockCustomerRepo.EXPECT().
		GetCustomerByID(ctx, countryID, customerRequest.ID).
		Return(*customerResponse, nil).Times(1)

	// Act
	structure, err := service.GetStructure(ctx, countryID, platform, customerRequest)

	// Assert
	assert.NoError(t, err)
	assert.Len(t, structure, 1)
	assert.Len(t, structure[0].Components, 2)
	assert.True(t, structure[0].Components[0].Active)
	assert.True(t, strings.Contains(*structure[0].Components[0].ServiceUrl, countryID))
	assert.True(t, strings.Contains(*structure[0].Components[0].Label, customerResponse.FirstName))
}

func TestGetStructureShouldGetNilCustomerAndReturnComponentsWhenCustomerRepositoryReturnsError(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	wg := createWaitGroup()
	defer wg.Wait()

	mockStructureRepo := mockout.NewMockStructureRepository(ctrl)
	mockCustomerRepo := mockout.NewMockCustomerRepository(ctrl)
	mockCacheRepo := sharedmockout.NewMockCache(ctrl)

	service := NewStructureService(mockStructureRepo, mockCustomerRepo, mockCacheRepo)

	ctx := context.Background()
	customerRequest := &model.Customer{ID: "123"}

	mockCacheRepo.EXPECT().Get(ctx, homeStructureCacheKeyMock).Return("", nil).Times(1)
	mockStructureRepo.EXPECT().GetHomeStructure(ctx, countryID, platform).Return(repositoryResponseMobile, nil).Times(1)
	mockCacheRepo.EXPECT().Set(gomock.Any(), homeStructureCacheKeyMock, gomock.Any(), gomock.Any()).
		Do(func(context.Context, string, string, time.Duration) { wg.Done() })

	mockCustomerRepo.EXPECT().
		GetCustomerByID(ctx, countryID, customerRequest.ID).
		Return(model.Customer{}, errors.New("repository error")).Times(1)

	// Act
	structure, err := service.GetStructure(ctx, countryID, platform, customerRequest)

	// Assert
	assert.NoError(t, err)
	assert.NoError(t, err)
	assert.Len(t, structure, 1)
	assert.Len(t, structure[0].Components, 1)
	assert.True(t, structure[0].Components[0].Active)
	assert.True(t, strings.Contains(*structure[0].Components[0].ServiceUrl, countryID))
}

func TestGetStructureShouldReturnFromCacheIfPresent(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockStructureRepo := mockout.NewMockStructureRepository(ctrl)
	mockCustomerRepo := mockout.NewMockCustomerRepository(ctrl)
	mockCacheRepo := sharedmockout.NewMockCache(ctrl)

	service := NewStructureService(mockStructureRepo, mockCustomerRepo, mockCacheRepo)

	ctx := context.Background()
	cachedComponents := repositoryResponseMobile
	cachedJSON, _ := json.Marshal(cachedComponents)

	mockCacheRepo.EXPECT().Get(ctx, homeStructureCacheKeyMock).Return(string(cachedJSON), nil).Times(1)

	// Act
	structure, err := service.GetStructure(ctx, countryID, platform, loggedInCustomer)

	// Assert
	assert.NoError(t, err)
	assert.Len(t, structure, 1)
	assert.Len(t, structure[0].Components, 2)
	assert.True(t, structure[0].Components[0].Active)
	assert.True(t, strings.Contains(*structure[0].Components[0].ServiceUrl, countryID))
	assert.True(t, strings.Contains(*structure[0].Components[0].Label, loggedInCustomer.FirstName))
}

func TestGetStructureShouldReturnComponentsEvenWhenCacheIsCorrupted(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	wg := createWaitGroup()
	defer wg.Wait()

	mockStructureRepo := mockout.NewMockStructureRepository(ctrl)
	mockCustomerRepo := mockout.NewMockCustomerRepository(ctrl)
	mockCacheRepo := sharedmockout.NewMockCache(ctrl)

	service := NewStructureService(mockStructureRepo, mockCustomerRepo, mockCacheRepo)

	ctx := context.Background()
	corruptedCache := "not-json"

	mockCacheRepo.EXPECT().Get(ctx, homeStructureCacheKeyMock).Return(corruptedCache, nil).Times(1)
	mockStructureRepo.EXPECT().GetHomeStructure(ctx, countryID, platform).Return(repositoryResponseMobile, nil).Times(1)
	mockCacheRepo.EXPECT().Set(gomock.Any(), homeStructureCacheKeyMock, gomock.Any(), gomock.Any()).
		Do(func(context.Context, string, string, time.Duration) { wg.Done() })

	// Act
	structure, err := service.GetStructure(ctx, countryID, platform, loggedInCustomer)

	// Assert
	assert.NoError(t, err)
	assert.Len(t, structure, 1)
	assert.Len(t, structure[0].Components, 2)
	assert.True(t, structure[0].Components[0].Active)
	assert.True(t, strings.Contains(*structure[0].Components[0].ServiceUrl, countryID))
	assert.True(t, strings.Contains(*structure[0].Components[0].Label, loggedInCustomer.FirstName))
}

func TestGetStructureShouldReturnComponentsEvenWhenCacheSetFails(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	wg := createWaitGroup()
	defer wg.Wait()

	mockStructureRepo := mockout.NewMockStructureRepository(ctrl)
	mockCustomerRepo := mockout.NewMockCustomerRepository(ctrl)
	mockCacheRepo := sharedmockout.NewMockCache(ctrl)

	service := NewStructureService(mockStructureRepo, mockCustomerRepo, mockCacheRepo)

	ctx := context.Background()

	mockCacheRepo.EXPECT().Get(ctx, homeStructureCacheKeyMock).Return("", nil).Times(1)
	mockStructureRepo.EXPECT().GetHomeStructure(ctx, countryID, platform).Return(repositoryResponseMobile, nil).Times(1)
	mockCacheRepo.EXPECT().Set(gomock.Any(), homeStructureCacheKeyMock, gomock.Any(), gomock.Any()).
		DoAndReturn(func(context.Context, string, string, time.Duration) error {
			wg.Done()
			return errors.New("Save cache error")
		})

	// Act
	structure, err := service.GetStructure(ctx, countryID, platform, loggedInCustomer)

	// Assert
	assert.NoError(t, err)
	assert.Len(t, structure, 1)
	assert.Len(t, structure[0].Components, 2)
	assert.True(t, structure[0].Components[0].Active)
	assert.True(t, strings.Contains(*structure[0].Components[0].ServiceUrl, countryID))
	assert.True(t, strings.Contains(*structure[0].Components[0].Label, loggedInCustomer.FirstName))
}

func TestGetStructureShouldReturnErrorWhenRepositoryReturnsError(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockStructureRepo := mockout.NewMockStructureRepository(ctrl)
	mockCustomerRepo := mockout.NewMockCustomerRepository(ctrl)
	mockCacheRepo := sharedmockout.NewMockCache(ctrl)

	service := NewStructureService(mockStructureRepo, mockCustomerRepo, mockCacheRepo)

	ctx := context.Background()
	repositoryError := errors.New("repository error")

	mockCacheRepo.EXPECT().Get(ctx, homeStructureCacheKeyMock).Return("", nil).Times(1)
	mockStructureRepo.EXPECT().GetHomeStructure(ctx, countryID, platform).Return(nil, repositoryError).Times(1)

	// Act
	structure, err := service.GetStructure(ctx, countryID, platform, loggedInCustomer)

	// Assert
	assert.Error(t, err)
	assert.Nil(t, structure)
	assert.Equal(t, repositoryError.Error(), err.Error())
}

func createWaitGroup() *sync.WaitGroup {
	var wg sync.WaitGroup
	wg.Add(1)

	return &wg
}
