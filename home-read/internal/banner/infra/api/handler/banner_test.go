package handler

import (
	"errors"
	"ftd-td-home-read-services/internal/banner/domain/model"
	mockInPort "ftd-td-home-read-services/test/mocks/banner/domain/ports/in"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHandler_GetMainBanners_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockBannerInPort := mockInPort.NewMockBannerInPort(ctrl)

	expectedBanners := []model.Banner{
		{ID: 1, Title: "Banner Principal"},
	}
	mockBannerInPort.EXPECT().GetMainBanners(gomock.Any()).Return(expectedBanners, nil)

	h := NewBannerHandler(mockBannerInPort)

	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.GET("/banner/main/:countryId", h.GetMainBanners)

	req, _ := http.NewRequest("GET", "/banner/main/co", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
}

func TestHandler_GetMainBanners_Error(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockBannerInPort := mockInPort.NewMockBannerInPort(ctrl)

	mockBannerInPort.EXPECT().GetMainBanners(gomock.Any()).Return(nil, errors.New("error"))

	h := NewBannerHandler(mockBannerInPort)

	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.GET("/banner/main/:countryId", h.GetMainBanners)

	req, _ := http.NewRequest("GET", "/banner/main/co", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusConflict, w.Code)
}

func TestHandler_GetSecondaryBanners_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockBannerInPort := mockInPort.NewMockBannerInPort(ctrl)

	expectedBanners := []model.Banner{
		{ID: 2, Title: "Banner Secundario"},
	}
	mockBannerInPort.EXPECT().GetSecondaryBanners(gomock.Any()).Return(expectedBanners, nil)

	h := NewBannerHandler(mockBannerInPort)

	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.GET("/banner/secondary/:countryId", h.GetSecondaryBanners)

	req, _ := http.NewRequest("GET", "/banner/secondary/co", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
}

func TestHandler_GetSecondaryBanners_Error(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockBannerInPort := mockInPort.NewMockBannerInPort(ctrl)

	mockBannerInPort.EXPECT().GetSecondaryBanners(gomock.Any()).Return(nil, errors.New("error"))

	h := NewBannerHandler(mockBannerInPort)

	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.GET("/banner/secondary/:countryId", h.GetSecondaryBanners)

	req, _ := http.NewRequest("GET", "/banner/secondary/co", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusConflict, w.Code)
}
