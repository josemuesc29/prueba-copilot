// Code generated by MockGen. DO NOT EDIT.
// Source: ftd-td-catalog-item-read-services/internal/shared/domain/ports/out (interfaces: CatalogProduct)
//
// Generated by this command:
//
//	mockgen -destination=../../../../../test/mocks/shared/domain/catalog_product_mock.go -package=domain ftd-td-catalog-item-read-services/internal/shared/domain/ports/out CatalogProduct
//

// Package domain is a generated GoMock package.
package domain

import (
	model "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	reflect "reflect"

	gin "github.com/gin-gonic/gin"
	gomock "go.uber.org/mock/gomock"
)

// MockCatalogProduct is a mock of CatalogProduct interface.
type MockCatalogProduct struct {
	ctrl     *gomock.Controller
	recorder *MockCatalogProductMockRecorder
	isgomock struct{}
}

// MockCatalogProductMockRecorder is the mock recorder for MockCatalogProduct.
type MockCatalogProductMockRecorder struct {
	mock *MockCatalogProduct
}

// NewMockCatalogProduct creates a new mock instance.
func NewMockCatalogProduct(ctrl *gomock.Controller) *MockCatalogProduct {
	mock := &MockCatalogProduct{ctrl: ctrl}
	mock.recorder = &MockCatalogProductMockRecorder{mock}
	return mock
}

// EXPECT returns an object that allows the caller to indicate expected use.
func (m *MockCatalogProduct) EXPECT() *MockCatalogProductMockRecorder {
	return m.recorder
}

// GetProductInformation mocks base method.
func (m *MockCatalogProduct) GetProductInformation(c *gin.Context, productID string) (model.ProductInformation, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "GetProductInformation", c, productID)
	ret0, _ := ret[0].(model.ProductInformation)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// GetProductInformation indicates an expected call of GetProductInformation.
func (mr *MockCatalogProductMockRecorder) GetProductInformation(c, productID any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetProductInformation", reflect.TypeOf((*MockCatalogProduct)(nil).GetProductInformation), c, productID)
}

// GetProductsInformationByObjectID mocks base method.
func (m *MockCatalogProduct) GetProductsInformationByObjectID(c *gin.Context, products []string, countryID string) ([]model.ProductInformation, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "GetProductsInformationByObjectID", c, products, countryID)
	ret0, _ := ret[0].([]model.ProductInformation)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// GetProductsInformationByObjectID indicates an expected call of GetProductsInformationByObjectID.
func (mr *MockCatalogProductMockRecorder) GetProductsInformationByObjectID(c, products, countryID any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetProductsInformationByObjectID", reflect.TypeOf((*MockCatalogProduct)(nil).GetProductsInformationByObjectID), c, products, countryID)
}

// GetProductsInformationByQuery mocks base method.
func (m *MockCatalogProduct) GetProductsInformationByQuery(c *gin.Context, params, countryID string) ([]model.ProductInformation, error) {
	m.ctrl.T.Helper()
	ret := m.ctrl.Call(m, "GetProductsInformationByQuery", c, params, countryID)
	ret0, _ := ret[0].([]model.ProductInformation)
	ret1, _ := ret[1].(error)
	return ret0, ret1
}

// GetProductsInformationByQuery indicates an expected call of GetProductsInformationByQuery.
func (mr *MockCatalogProductMockRecorder) GetProductsInformationByQuery(c, params, countryID any) *gomock.Call {
	mr.mock.ctrl.T.Helper()
	return mr.mock.ctrl.RecordCallWithMethodType(mr.mock, "GetProductsInformationByQuery", reflect.TypeOf((*MockCatalogProduct)(nil).GetProductsInformationByQuery), c, params, countryID)
}
