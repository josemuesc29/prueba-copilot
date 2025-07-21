package handler

import (
	"errors"
	"net/http"
	"testing"

	"ftd-td-home-read-services/internal/carousel/domain/model"
	mockInPort "ftd-td-home-read-services/test/mocks/carousel/domain/ports/in"

	"net/http/httptest"

	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
)

var responseGetSuggested = []model.Suggested{{
	ID:               1,
	FirstDescription: "First Description",
	OfferText:        "Offer Text",
	Type:             "Type",
	UrlImage:         "http://example.com/image.jpg",
	OrderingNumber:   1,
	Action:           "Action",
	Position:         1,
	Product: model.Product{
		MediaImageUrl:     "https://example.com/image.jpg",
		Description:       "Descripción del producto",
		FullPrice:         10000.0,
		MediaDescription:  "Descripción media",
		Brand:             "MarcaX",
		Sales:             123,
		DetailDescription: "Detalle del producto",
		OfferPrice:        8000.0,
		OfferDescription:  "¡Oferta especial!",
		ID:                "PROD123",
		OfferText:         "20% OFF",
		IdStoreGroup:      1,
		Marca:             "MarcaX",
		ObjectID:          "OBJ123",
		OnlyOnline:        true,
		DeliveryTime:      "24h",
		Highlight:         true,
		Generic:           false,
		LargeDescription:  "Descripción larga",
		AnywaySelling:     false,
		Spaces:            5,
		Status:            "A",
		TaxRate:           19,
		ListUrlImages:     []string{"https://example.com/img1.jpg", "https://example.com/img2.jpg"},
		MeasurePum:        10,
		LabelPum:          "Unidades a $",
		HighlightsID:      []int{1, 2, 3},
		SuggestedID:       []int{4, 5},
		Departments:       []string{"Dept1", "Dept2"},
		SubCategory:       "SubCat",
		Supplier:          "ProveedorX",
		Outofstore:        false,
		OfferStartDate:    1710000000,
		OfferEndDate:      1710003600,
		PrimePrice:        7500.0,
		PrimeTextDiscount: "10% Prime",
		PrimeDescription:  "Prime exclusivo",
		RmsClass:          "ClaseX",
		RmsDeparment:      "DeptX",
		RmsGroup:          "GrupoX",
		RmsSubclass:       "SubclaseX",
		WithoutStock:      false,
		URL:               "producto-x",
	},
},
}

func TestCarouselHandlerWhenSuccess(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	inPortTest := mockInPort.NewMockCarousel(controller)

	inPortTest.EXPECT().GetSuggested(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseGetSuggested, nil)

	carouselH := NewCarousel(inPortTest)

	carouselH.GetSuggested(getContext())
}

func TestCarouselHandlerWhenFailByNotPathParam(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	inPortTest := mockInPort.NewMockCarousel(controller)

	carouselH := NewCarousel(inPortTest)

	context := getContext()
	context.Params = []gin.Param{}

	carouselH.GetSuggested(context)
}

func TestCarouselHandlerWhenFailQueryParams(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	inPortTest := mockInPort.NewMockCarousel(controller)

	carouselH := NewCarousel(inPortTest)

	context := getContext()
	context.Request, _ = http.NewRequest(http.MethodGet, "http://localhost:8080/home/r/CO/v1/carousel/suggest?idStoreGroup=19281jasj281", nil)

	carouselH.GetSuggested(context)
}

func TestCarouselHandlerWhenFailGetSuggested(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	inPortTest := mockInPort.NewMockCarousel(controller)

	inPortTest.EXPECT().GetSuggested(gomock.Any(), gomock.Any(), gomock.Any()).Return([]model.Suggested{}, errors.New("test error"))

	crouselH := NewCarousel(inPortTest)

	crouselH.GetSuggested(getContext())
}

func getContext() *gin.Context {
	context, _ := gin.CreateTestContext(httptest.NewRecorder())

	context.Request, _ = http.NewRequest(http.MethodGet, "http://localhost:8080/home/r/CO/v1/carousel/suggest?key=19281jasj281", nil)

	context.Params = []gin.Param{
		{Key: "countryId", Value: "123"},
	}

	context.Request = context.Request.WithContext(context)

	return context
}
