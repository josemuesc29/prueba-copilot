// Code generated by MockGen. DO NOT EDIT.
// Source: bannerCms.go
//
// Generated by this command:
//
//	mockgen -source=bannerCms.go -destination=../../../../../test/mocks/banner/domain/ports/in/banner_mock.go
//

// Package mock_in is a generated GoMock package.
package mock_in

import (
	model "ftd-td-home-read-services/internal/banner/domain/model"
	reflect "reflect"

	gin "github.com/gin-gonic/gin"
	gomock "go.uber.org/mock/gomock"
)

// MockBannerInPort is a mock of BannerInPort interface.
type MockBannerInPort struct {
	ctrl     *gomock.Controller
	recorder *MockBannerInPortMockRecorder
	isgomock struct{}
}

// MockBannerInPortMockRecorder is the mock recorder for MockBannerInPort.
type MockBannerInPortMockRecorder struct {
	mock *MockBannerInPort
}

// NewMockBannerInPort creates a new mock instance.
func NewMockBannerInPort(ctrl *gomock.Controller) *MockBannerInPort {
	mock := &MockBannerInPort{ctrl: ctrl}
	mock.recorder = &MockBannerInPortMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockBannerInPort) EXPECT() *MockBannerInPortMockRecorder {
	return m.recorder
}

// GetMainBanners mocks base method.
func (m *MockBannerInPort) GetMainBanners(c *gin.Context) ([]model.Banner, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "GetMainBanners", c)
	ret0, _ := ret[0].([]model.Banner)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// GetMainBanners indicates an expected call of GetMainBanners.
func (mr *MockBannerInPortMockRecorder) GetMainBanners(c any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetMainBanners", reflect.TypeOf((*MockBannerInPort)(nil).GetMainBanners), c)
}

// GetSecondaryBanners mocks base method.
func (m *MockBannerInPort) GetSecondaryBanners(c *gin.Context) ([]model.Banner, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "GetSecondaryBanners", c)
	ret0, _ := ret[0].([]model.Banner)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// GetSecondaryBanners indicates an expected call of GetSecondaryBanners.
func (mr *MockBannerInPortMockRecorder) GetSecondaryBanners(c any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetSecondaryBanners", reflect.TypeOf((*MockBannerInPort)(nil).GetSecondaryBanners), c)
}
