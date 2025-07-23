package mock_groups

import (
	"reflect"

	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
)

// MockStructure is a mock of Structure interface.
type MockStructure struct {
	ctrl     *gomock.Controller
	recorder *MockStructureMockRecorder
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

// Resource mocks base method.
func (m *MockStructure) Resource(router *gin.RouterGroup) {
	m.ctrl.T.Helper()
	m.ctrl.Call(m, "Resource", router)
}

// Resource indicates an expected call of Resource.
func (mr *MockStructureMockRecorder) Resource(router interface{}) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "Resource", reflect.TypeOf((*MockStructure)(nil).Resource), router)
}
