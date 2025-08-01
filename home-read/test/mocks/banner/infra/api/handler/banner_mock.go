// Code generated by MockGen. DO NOT EDIT.
// Source: banner.go
//
// Generated by this command:
//
//	mockgen -source=banner.go -destination=../../../../../test/mocks/banner/infra/api/handler/banner_mock.go
//

// Package mock_handler is a generated GoMock package.
package mock_handler

import (
	reflect "reflect"

	gin "github.com/gin-gonic/gin"
	gomock "go.uber.org/mock/gomock"
)

// MockHandler is a mock of Handler interface.
type MockHandler struct {
	ctrl     *gomock.Controller
	recorder *MockHandlerMockRecorder
	isgomock struct{}
}

// MockHandlerMockRecorder is the mock recorder for MockHandler.
type MockHandlerMockRecorder struct {
	mock *MockHandler
}

// NewMockHandler creates a new mock instance.
func NewMockHandler(ctrl *gomock.Controller) *MockHandler {
	mock := &MockHandler{ctrl: ctrl}
	mock.recorder = &MockHandlerMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockHandler) EXPECT() *MockHandlerMockRecorder {
	return m.recorder
}

// GetMainBanners mocks base method.
func (m *MockHandler) GetMainBanners(c *gin.Context) {
	m.ctrl.T.Helper()
	m.ctrl.Call(m, "GetMainBanners", c)
}

// GetMainBanners indicates an expected call of GetMainBanners.
func (mr *MockHandlerMockRecorder) GetMainBanners(c any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetMainBanners", reflect.TypeOf((*MockHandler)(nil).GetMainBanners), c)
}

// GetSecondaryBanners mocks base method.
func (m *MockHandler) GetSecondaryBanners(c *gin.Context) {
	m.ctrl.T.Helper()
	m.ctrl.Call(m, "GetSecondaryBanners", c)
}

// GetSecondaryBanners indicates an expected call of GetSecondaryBanners.
func (mr *MockHandlerMockRecorder) GetSecondaryBanners(c any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetSecondaryBanners", reflect.TypeOf((*MockHandler)(nil).GetSecondaryBanners), c)
}
