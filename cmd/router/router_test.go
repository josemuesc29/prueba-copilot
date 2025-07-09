package router

import (
	"testing"

	mockBestSellerGroups "ftd-td-catalog-item-read-services/test/mocks/best-seller/infra/api/groups"
	mockHealthGroups "ftd-td-catalog-item-read-services/test/mocks/health/infra/api/groups"

	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

func TestRouterConfigWhenSuccess(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	healthGroup := mockHealthGroups.NewMockHealthGroup(controller)
	bestSellerGroup := mockBestSellerGroups.NewMockBestSeller(controller)

	healthGroup.EXPECT().Source(gomock.Any()).Times(1)
	bestSellerGroup.EXPECT().Source(gomock.Any()).Times(1)

	router := NewRouter(healthGroup, bestSellerGroup)

	routerConfig := SetupRouter(router)

	assert.NotEmpty(t, routerConfig)
	assert.Equal(t, routerConfig.BasePath(), "/")
}
