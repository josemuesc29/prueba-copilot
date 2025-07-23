package router

import (
	"testing"

	mockBestSellerGroups "ftd-td-catalog-item-read-services/test/mocks/best-seller/infra/api/groups"
	mockDetailBrandGroups "ftd-td-catalog-item-read-services/test/mocks/detail/infra/api/groups"
	mockHealthGroups "ftd-td-catalog-item-read-services/test/mocks/health/infra/api/groups"
	mockProductsRelatedGroups "ftd-td-catalog-item-read-services/test/mocks/products-related/infra/api/groups"
	mockSameBrandGroups "ftd-td-catalog-item-read-services/test/mocks/same-brand/infra/api/groups"
	mockStructureGroups "ftd-td-catalog-item-read-services/test/mocks/structure/infra/api/groups"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

func TestRouterConfigWhenSuccess(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	healthGroup := mockHealthGroups.NewMockHealthGroup(controller)
	bestSellerGroup := mockBestSellerGroups.NewMockBestSeller(controller)
	sameBrandGroup := mockSameBrandGroups.NewMockSameBrand(controller)
	productsRelatedGroup := mockProductsRelatedGroups.NewMockProductsRelatedGroup(controller)
	detailGroup := mockDetailBrandGroups.NewMockGroup(controller)
	structureGroup := mockStructureGroups.NewMockStructure(controller)

	healthGroup.EXPECT().Source(gomock.Any()).Times(1)
	bestSellerGroup.EXPECT().Source(gomock.Any()).Times(1)
	sameBrandGroup.EXPECT().Source(gomock.Any()).Times(1)
	productsRelatedGroup.EXPECT().Source(gomock.Any()).Times(1)
	detailGroup.EXPECT().Source(gomock.Any()).Times(1)
	structureGroup.EXPECT().Resource(gomock.Any()).Times(1)

	router := NewRouter(healthGroup, bestSellerGroup, sameBrandGroup, productsRelatedGroup, detailGroup, structureGroup)

	routerConfig := SetupRouter(router)

	assert.NotEmpty(t, routerConfig)
	assert.Equal(t, routerConfig.BasePath(), "/")
}
