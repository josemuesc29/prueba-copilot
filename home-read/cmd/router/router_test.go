package router

import (
	"testing"

	bannerGroup "ftd-td-home-read-services/test/mocks/banner/infra/api/groups"
	mockCarouselGroups "ftd-td-home-read-services/test/mocks/carousel/infra/api/groups"
	mockHealthGroups "ftd-td-home-read-services/test/mocks/health/infra/api/groups"
	mockOffersGroups "ftd-td-home-read-services/test/mocks/offer/infra/api/groups"
	mockStructureGroups "ftd-td-home-read-services/test/mocks/structure/infra/api/groups"

	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

func TestRouterConfigWhenSuccess(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	healthGroup := mockHealthGroups.NewMockHealthGroup(controller)
	structureGroup := mockStructureGroups.NewMockStructureGroup(controller)
	offerGroup := mockOffersGroups.NewMockGroup(controller)
	carosuselGroup := mockCarouselGroups.NewMockCarousel(controller)
	bannersGroup := bannerGroup.NewMockBannerGroup(controller)

	healthGroup.EXPECT().Source(gomock.Any()).Times(1)
	structureGroup.EXPECT().Source(gomock.Any()).Times(1)
	offerGroup.EXPECT().Source(gomock.Any()).Times(1)
	carosuselGroup.EXPECT().Source(gomock.Any()).Times(1)
	bannersGroup.EXPECT().Source(gomock.Any()).Times(1)

	router := NewRouter(healthGroup, structureGroup, offerGroup, carosuselGroup, bannersGroup)

	routerConfig := SetupRouter(router)

	assert.NotEmpty(t, routerConfig)
	assert.Equal(t, routerConfig.BasePath(), "/")
}
