// Code generated by MockGen. DO NOT EDIT.
// Source: offer.go
//
// Generated by this command:
//
//	mockgen -source=offer.go -destination=../../../../../test/mocks/offer/domain/ports/in/offer_mock.go
//

// Package mock_in is a generated GoMock package.
package mock_in

import (
	model "ftd-td-home-read-services/internal/offer/domain/model"
	reflect "reflect"

	gin "github.com/gin-gonic/gin"
	gomock "go.uber.org/mock/gomock"
)

// MockOfferInPort is a mock of OfferInPort interface.
type MockOfferInPort struct {
	ctrl     *gomock.Controller
	recorder *MockOfferInPortMockRecorder
	isgomock struct{}
}

// MockOfferInPortMockRecorder is the mock recorder for MockOfferInPort.
type MockOfferInPortMockRecorder struct {
	mock *MockOfferInPort
}

// NewMockOfferInPort creates a new mock instance.
func NewMockOfferInPort(ctrl *gomock.Controller) *MockOfferInPort {
	mock := &MockOfferInPort{ctrl: ctrl}
	mock.recorder = &MockOfferInPortMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockOfferInPort) EXPECT() *MockOfferInPortMockRecorder {
	return m.recorder
}

// GetFlashOffer mocks base method.
func (m *MockOfferInPort) GetFlashOffer(c *gin.Context) ([]model.FlashOffer, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "GetFlashOffer", c)
	ret0, _ := ret[0].([]model.FlashOffer)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// GetFlashOffer indicates an expected call of GetFlashOffer.
func (mr *MockOfferInPortMockRecorder) GetFlashOffer(c any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetFlashOffer", reflect.TypeOf((*MockOfferInPort)(nil).GetFlashOffer), c)
}
