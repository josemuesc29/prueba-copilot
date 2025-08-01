// Code generated by MockGen. DO NOT EDIT.
// Source: same_brand.go
//
// Generated by this command:
//
//	mockgen -source=same_brand.go -destination=../../../../../test/mocks/same-brand/domain/ports/in/same_brand_mock.go
//

// Package mock_in is a generated GoMock package.
package mock_in

import (
	model "ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	reflect "reflect"

	gin "github.com/gin-gonic/gin"
	gomock "go.uber.org/mock/gomock"
)

// MockSameBrand is a mock of SameBrand interface.
type MockSameBrand struct {
	ctrl     *gomock.Controller
	recorder *MockSameBrandMockRecorder
	isgomock struct{}
}

// MockSameBrandMockRecorder is the mock recorder for MockSameBrand.
type MockSameBrandMockRecorder struct {
	mock *MockSameBrand
}

// NewMockSameBrand creates a new mock instance.
func NewMockSameBrand(ctrl *gomock.Controller) *MockSameBrand {
	mock := &MockSameBrand{ctrl: ctrl}
	mock.recorder = &MockSameBrandMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockSameBrand) EXPECT() *MockSameBrandMockRecorder {
	return m.recorder
}

// GetItemsBySameBrand mocks base method.
func (m *MockSameBrand) GetItemsBySameBrand(c *gin.Context, countryID, itemID, source, nearbyStores, storeId, city string) ([]model.SameBrandItem, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "GetItemsBySameBrand", c, countryID, itemID, source, nearbyStores, storeId, city)
	ret0, _ := ret[0].([]model.SameBrandItem)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// GetItemsBySameBrand indicates an expected call of GetItemsBySameBrand.
func (mr *MockSameBrandMockRecorder) GetItemsBySameBrand(c, countryID, itemID, source, nearbyStores, storeId, city any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetItemsBySameBrand", reflect.TypeOf((*MockSameBrand)(nil).GetItemsBySameBrand), c, countryID, itemID, source, nearbyStores, storeId, city)
}
