package app

import (
	"encoding/json"
	"errors"
	"ftd-td-home-read-services/internal/banner/domain/model"
	mockOutPort "ftd-td-home-read-services/test/mocks/banner/domain/ports/out"
	sharedOutPorts "ftd-td-home-read-services/test/mocks/shared/domain/ports/out"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestBanner_GetMainBanners_CacheHit(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCms := mockOutPort.NewMockBannerCmsOutPort(ctrl)
	mockCache := sharedOutPorts.NewMockCache(ctrl)

	svc := NewBannerService(mockCms, mockCache)

	c := getTestContext()
	banners := []model.Banner{{ID: 1, Category: "main"}}
	bannersBytes, _ := json.Marshal(banners)
	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return(string(bannersBytes), nil)

	result, err := svc.GetMainBanners(c)
	assert.NoError(t, err)
	assert.Equal(t, banners, result)
}

func TestBanner_GetMainBanners_CacheMiss_And_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCms := mockOutPort.NewMockBannerCmsOutPort(ctrl)
	mockCache := sharedOutPorts.NewMockCache(ctrl)

	svc := NewBannerService(mockCms, mockCache)

	c := getTestContext()

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	mockCms.EXPECT().GetBanners(gomock.Any(), "AR").Return([]model.Banner{
		{ID: 1, Category: "main"},
		{ID: 2, Category: "secondary"},
	}, nil)
	mockCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)

	result, err := svc.GetMainBanners(c)
	assert.NoError(t, err)
	assert.Len(t, result, 1)
	assert.Equal(t, "main", result[0].Category)
}

func TestBanner_GetMainBanners_CmsError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCms := mockOutPort.NewMockBannerCmsOutPort(ctrl)
	mockCache := sharedOutPorts.NewMockCache(ctrl)

	svc := NewBannerService(mockCms, mockCache)

	c := getTestContext()

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	mockCms.EXPECT().GetBanners(gomock.Any(), "AR").Return(nil, errors.New("cms error"))

	result, err := svc.GetMainBanners(c)
	assert.Error(t, err)
	assert.Nil(t, result)
}

func TestBanner_GetSecondaryBanners_CacheHit(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCms := mockOutPort.NewMockBannerCmsOutPort(ctrl)
	mockCache := sharedOutPorts.NewMockCache(ctrl)

	svc := NewBannerService(mockCms, mockCache)

	c := getTestContext()
	banners := []model.Banner{{ID: 2, Category: "secondary"}}
	bannersBytes, _ := json.Marshal(banners)
	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return(string(bannersBytes), nil)

	result, err := svc.GetSecondaryBanners(c)
	assert.NoError(t, err)
	assert.Equal(t, banners, result)
}

func TestBanner_GetSecondaryBanners_CacheMiss_And_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCms := mockOutPort.NewMockBannerCmsOutPort(ctrl)
	mockCache := sharedOutPorts.NewMockCache(ctrl)

	svc := NewBannerService(mockCms, mockCache)

	c := getTestContext()

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	mockCms.EXPECT().GetBanners(gomock.Any(), "AR").Return([]model.Banner{
		{ID: 1, Category: "main"},
		{ID: 2, Category: "secondary"},
	}, nil)
	mockCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)

	result, err := svc.GetSecondaryBanners(c)
	assert.NoError(t, err)
	assert.Len(t, result, 1)
	assert.Equal(t, "secondary", result[0].Category)
}

func TestBanner_GetSecondaryBanners_CmsError(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCms := mockOutPort.NewMockBannerCmsOutPort(ctrl)
	mockCache := sharedOutPorts.NewMockCache(ctrl)

	svc := NewBannerService(mockCms, mockCache)

	c := getTestContext()

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	mockCms.EXPECT().GetBanners(gomock.Any(), "AR").Return(nil, errors.New("cms error"))

	result, err := svc.GetSecondaryBanners(c)
	assert.Error(t, err)
	assert.Nil(t, result)
}

func getTestContext() *gin.Context {
	gin.SetMode(gin.TestMode)
	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header)}
	ctx.Params = append(ctx.Params, gin.Param{Key: "countryId", Value: "AR"})
	return ctx
}
