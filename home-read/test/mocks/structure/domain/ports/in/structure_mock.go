// Code generated by MockGen. DO NOT EDIT.
// Source: structure.go
//
// Generated by this command:
//
//	mockgen -source=structure.go -destination=../../../../../test/mocks/structure/domain/ports/in/structure_mock.go
//

// Package mock_in is a generated GoMock package.
package mock_in

import (
	context "context"
	model "ftd-td-home-read-services/internal/structure/domain/model"
	reflect "reflect"

	gomock "go.uber.org/mock/gomock"
)

// MockStructureService is a mock of StructureService interface.
type MockStructureService struct {
	ctrl     *gomock.Controller
	recorder *MockStructureServiceMockRecorder
	isgomock struct{}
}

// MockStructureServiceMockRecorder is the mock recorder for MockStructureService.
type MockStructureServiceMockRecorder struct {
	mock *MockStructureService
}

// NewMockStructureService creates a new mock instance.
func NewMockStructureService(ctrl *gomock.Controller) *MockStructureService {
	mock := &MockStructureService{ctrl: ctrl}
	mock.recorder = &MockStructureServiceMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockStructureService) EXPECT() *MockStructureServiceMockRecorder {
	return m.recorder
}

// GetStructure mocks base method.
func (m *MockStructureService) GetStructure(ctx context.Context, countryID string, platform model.Platform, customer *model.Customer) ([]model.Section, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "GetStructure", ctx, countryID, platform, customer)
	ret0, _ := ret[0].([]model.Section)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// GetStructure indicates an expected call of GetStructure.
func (mr *MockStructureServiceMockRecorder) GetStructure(ctx, countryID, platform, customer any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetStructure", reflect.TypeOf((*MockStructureService)(nil).GetStructure), ctx, countryID, platform, customer)
}
