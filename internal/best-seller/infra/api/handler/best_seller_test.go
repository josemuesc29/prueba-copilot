package handler

import (
	"errors"
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/model"
	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
	"net/http"
	"net/http/httptest"
	"testing"

	mockInPorts "ftd-td-catalog-item-read-services/test/mocks/best-seller/domain/ports/in"
)

var (
	responseGetBestSellers = []model.BestSellerDepartment{
		{
			MediaImageUrl:     "https://example.com/image.jpg",
			Description:       "Descripción de prueba",
			FullPrice:         199.99,
			MediaDescription:  "Media descripción",
			Brand:             "MarcaX",
			Sales:             150,
			DetailDescription: "Detalle de prueba",
			OfferPrice:        149.99,
			OfferDescription:  "Oferta especial",
			ID:                "item123",
			OfferText:         "¡Descuento!",
			IdStoreGroup:      42,
			Marca:             "MarcaX",
			ObjectID:          "obj-456",
			OnlyOnline:        true,
			DeliveryTime:      "24h",
			Highlight:         true,
			Generic:           false,
			LargeDescription:  "Descripción larga de prueba",
			AnywaySelling:     false,
			Spaces:            5,
			Status:            "activo",
			TaxRate:           19,
			ListUrlImages:     []string{"https://example.com/img1.jpg", "https://example.com/img2.jpg"},
			MeasurePum:        1.5,
			LabelPum:          "kg",
			HighlightsID:      []int{1, 2, 3},
			SuggestedID:       []int{4, 5},
			Departments:       []string{"depto1", "depto2"},
			SubCategory:       "subcat1",
			Supplier:          "ProveedorX",
			Outofstore:        false,
			OfferStartDate:    1718000000,
			OfferEndDate:      1719000000,
			PrimePrice:        139.99,
			PrimeTextDiscount: "Prime descuento",
			PrimeDescription:  "Prime descripción",
			RmsClass:          "A",
			RmsDeparment:      "B",
			RmsGroup:          "C",
			RmsSubclass:       "D",
			WithoutStock:      false,
			URL:               "https://example.com/producto",
		},
	}
)

func TestBestSellerHandlerWhenSuccess(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	inPortTest := mockInPorts.NewMockBestSeller(controller)

	inPortTest.EXPECT().GetBestSellerDepartment(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(responseGetBestSellers, nil)

	bestSellerH := NewBestSeller(inPortTest)

	bestSellerH.GetBestSellerDepartment(getContext())
}

func TestBestSellerHandlerWhenFailByNotPathParam(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	inPortTest := mockInPorts.NewMockBestSeller(controller)

	bestSellerH := NewBestSeller(inPortTest)

	context := getContext()
	context.Params = []gin.Param{}

	bestSellerH.GetBestSellerDepartment(context)
}

func TestBestSellerHandlerWhenFailGetBestSellerDepartment(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	inPortTest := mockInPorts.NewMockBestSeller(controller)

	inPortTest.EXPECT().GetBestSellerDepartment(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return([]model.BestSellerDepartment{}, errors.New("test error"))

	bestSellerH := NewBestSeller(inPortTest)

	bestSellerH.GetBestSellerDepartment(getContext())
}

func getContext() *gin.Context {
	context, _ := gin.CreateTestContext(httptest.NewRecorder())

	context.Request, _ = http.NewRequest(http.MethodGet, "http://localhost:8080/catalog-item/r/CO/v1/items/best-seller/department/12?storeId=19281jasj281", nil)

	context.Params = []gin.Param{
		{Key: "countryId", Value: "123"},
		{Key: "departmentId", Value: "123"},
	}

	context.Request = context.Request.WithContext(context)

	return context
}
