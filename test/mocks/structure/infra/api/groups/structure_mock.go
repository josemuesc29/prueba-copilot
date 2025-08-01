// Code generated by MockGen. DO NOT EDIT.
// Source: structure.go
//
// Generated by this command:
//
//	mockgen -source=structure.go -destination=../../../../../test/mocks/structure/infra/api/groups/structure_mock.go
//

// Package mock_groups is a generated GoMock package.
package mock_groups

import (
	reflect "reflect"

	gin "github.com/gin-gonic/gin"
	gomock "go.uber.org/mock/gomock"
)

// MockStructure is a mock of Structure interface.
type MockStructure struct {
	ctrl     *gomock.Controller
	recorder *MockStructureMockRecorder
	isgomock struct{}
}

// MockStructureMockRecorder is the mock recorder for MockStructure.
type MockStructureMockRecorder struct {
	mock *MockStructure
}

// NewMockStructure creates a new mock instance.
func NewMockStructure(ctrl *gomock.Controller) *MockStructure {
	mock := &MockStructure{ctrl: ctrl}
	mock.recorder = &MockStructureMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockStructure) EXPECT() *MockStructureMockRecorder {
	return m.recorder
}

// Source mocks base method.
func (m *MockStructure) Source(router *gin.RouterGroup) {
	m.ctrl.T.Helper()
	m.ctrl.Call(m, "Source", router)
}

// Source indicates an expected call of Source.
func (mr *MockStructureMockRecorder) Source(router any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Source", reflect.TypeOf((*MockStructure)(nil).Source), router)
}
