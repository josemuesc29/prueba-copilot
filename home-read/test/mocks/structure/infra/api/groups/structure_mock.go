// Code generated by MockGen. DO NOT EDIT.
// Source: routes.go
//
// Generated by this command:
//
//	mockgen -source=routes.go -destination=../../../../../test/mocks/structure/infra/api/groups/structure_mock.go
//

// Package mock_groups is a generated GoMock package.
package mock_groups

import (
	reflect "reflect"

	gin "github.com/gin-gonic/gin"
	gomock "go.uber.org/mock/gomock"
)

// MockStructureGroup is a mock of StructureGroup interface.
type MockStructureGroup struct {
	ctrl     *gomock.Controller
	recorder *MockStructureGroupMockRecorder
	isgomock struct{}
}

// MockStructureGroupMockRecorder is the mock recorder for MockStructureGroup.
type MockStructureGroupMockRecorder struct {
	mock *MockStructureGroup
}

// NewMockStructureGroup creates a new mock instance.
func NewMockStructureGroup(ctrl *gomock.Controller) *MockStructureGroup {
	mock := &MockStructureGroup{ctrl: ctrl}
	mock.recorder = &MockStructureGroupMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockStructureGroup) EXPECT() *MockStructureGroupMockRecorder {
	return m.recorder
}

// Source mocks base method.
func (m *MockStructureGroup) Source(rg *gin.RouterGroup) {
	m.ctrl.T.Helper()
	m.ctrl.Call(m, "Source", rg)
}

// Source indicates an expected call of Source.
func (mr *MockStructureGroupMockRecorder) Source(rg any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Source", reflect.TypeOf((*MockStructureGroup)(nil).Source), rg)
}
