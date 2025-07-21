package app

import (
	"errors"
	"ftd-td-home-read-services/internal/carousel/domain/model"
	sharedModel "ftd-td-home-read-services/internal/shared/domain/model"
	"github.com/gin-gonic/gin"
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"

	mockOutPort "ftd-td-home-read-services/test/mocks/carousel/domain/ports/out"
	mockSharedOutPort "ftd-td-home-read-services/test/mocks/shared/domain/ports/out"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

var (
	countryID           = "CO"
	storeGroupID        = 64
	responseGetSuggests = model.SuggestedCMS{
		Data: []model.SuggestedData{
			{
				ID:             6,
				Type:           "UNIQUE",
				Position:       1,
				OrderingNumber: 10,
				Action:         "ver",
				DocumentID:     "DOC456",
				Sku:            "6",
				CreatedAt:      "2024-06-01T12:00:00Z",
				UpdatedAt:      "2024-06-02T12:00:00Z",
				PublishedAt:    "2024-06-03T12:00:00Z",
				Locale:         "es-CL",
				Name:           "Producto de prueba",
			},
		},
	}
	responseProductInfo = []sharedModel.ProductInformation{
		{
			Prime:               "Sí",
			CustomTag:           func() *string { s := "Etiqueta especial"; return &s }(),
			DeliveryTime:        "24h",
			Departments:         []string{"Dept1", "Dept2"},
			DesHiddenSEO:        func() *string { s := "SEO oculto"; return &s }(),
			FullPrice:           19990.0,
			Generics:            false,
			GrayDescription:     "Descripción gris",
			HasStock:            true,
			Highlight:           true,
			ID:                  "6",
			IsCupon:             0,
			LabelPum:            "Unidades a $",
			LargeDescription:    "Descripción larga del producto",
			ListUrlImages:       []string{"https://ejemplo.com/img1.jpg", "https://ejemplo.com/img2.jpg"},
			Marca:               "MarcaX",
			MeasurePum:          1.5,
			MediaDescription:    "Descripción media",
			MediaImageUrl:       "https://ejemplo.com/imagen.jpg",
			MetadesSEO:          func() *string { s := "Meta descripción SEO"; return &s }(),
			MetatituloSEO:       func() *string { s := "Meta título SEO"; return &s }(),
			ObjectID:            "6",
			OfferDescription:    "Oferta especial",
			OfferEndDate:        1710003600,
			OfferPrice:          15990.0,
			OfferPriceByCity:    []interface{}{"Oferta ciudad"},
			OfferStartDate:      1710000000,
			OfferText:           "20% OFF",
			OnlyOnline:          true,
			Outofstore:          false,
			PrimeDescription:    "Prime exclusivo",
			PrimePrice:          14990.0,
			PrimeTextDiscount:   "10% Prime",
			RequirePrescription: false,
			RmsClass:            "ClaseX",
			RmsDeparment:        "DeptX",
			RmsGroup:            "GrupoX",
			RmsSubclass:         "SubclaseX",
			Sales:               100,
			Spaces:              5,
			Status:              "A",
		},
	}
	jsonCache = "[\n    {\n        \"id\": 6,\n        \"firstDescription\": \"\",\n        \"offerText\": \"\",\n        \"type\": \"UNIQUE\",\n        \"urlImage\": \"https://lh3.googleusercontent.com/VuMG49o-wbndmKjs66Va27BkdfoqCvo8aoUIviefcoeKyBZfTRekzF8DvpqX3IucsGbzCIlRs1AEJiNbiNMBVCJOFkfD3w4IvFllIiY70_pjCjk\",\n        \"startDate\": 1747026000000,\n        \"endDate\": 1748753940000,\n        \"orderingNumber\": 1,\n        \"action\": \"SUGGESTED\",\n        \"position\": 1,\n        \"product\": {\n            \"mediaImageUrl\": \"https://lh3.googleusercontent.com/VuMG49o-wbndmKjs66Va27BkdfoqCvo8aoUIviefcoeKyBZfTRekzF8DvpqX3IucsGbzCIlRs1AEJiNbiNMBVCJOFkfD3w4IvFllIiY70_pjCjk\",\n            \"description\": \"Allegra Fexofenadina Clorhidrato 120 mg Sanofi Caja x 10 Tabletas\",\n            \"fullPrice\": 68100,\n            \"mediaDescription\": \"Allegra Fexofenadina Clorhidrato 120 mg Sanofi Caja x 10 Tabletas\",\n            \"brand\": \"2008M-0008623\",\n            \"sales\": 4002,\n            \"detailDescription\": \" \",\n            \"offerPrice\": 54480,\n            \"offerDescription\": \"\",\n            \"id\": \"1000079\",\n            \"offerText\": \"\",\n            \"idStoreGroup\": 0,\n            \"marca\": \"Allegra\",\n            \"objectID\": \"1000079\",\n            \"onlyOnline\": false,\n            \"deliveryTime\": \"35 min. promedio\",\n            \"highlight\": false,\n            \"generic\": 0,\n            \"largeDescription\": \" \",\n            \"anywaySelling\": false,\n            \"spaces\": 1,\n            \"status\": \"A\",\n            \"taxRate\": 19,\n            \"listUrlImages\": [\n                \"https://lh3.googleusercontent.com/FivWkQwjkN3ZKyBcMVaDA78D8i72m_WeJAdiRP3SA-cZZ8AkLxC8J5lj-1nlziNRqWx8dWZbhLOSvwaFW0CGz4PXeTzfNxZGg1DBWnv-xpcO_uiK\"\n            ],\n            \"measurePum\": 10,\n            \"labelPum\": \"Unidades a $\",\n            \"id_highlights\": [\n                51007\n            ],\n            \"id_suggested\": [\n                24678\n            ],\n            \"departments\": [],\n            \"subCategory\": \"Droguería\",\n            \"supplier\": \"OPELLA\",\n            \"outofstore\": false,\n            \"offerStartDate\": 1747026000000,\n            \"offerEndDate\": 1748753940000,\n            \"primePrice\": 0,\n            \"primeTextDiscount\": \"\",\n            \"primeDescription\": \"\",\n            \"rms_class\": \"ANTIHISTAMINICOS\",\n            \"rms_deparment\": \"RX SIST RESPIRATORIO\",\n            \"rms_group\": \"RX\",\n            \"rms_subclass\": \"ANTIHISTAMINICOS RX\",\n            \"without_stock\": false,\n            \"url\": \"1000079-allegra-120-mg-x-10-tabletas\"\n        }\n    }\n]"
)

func TestTestGroupWhenSuccessWithCahce(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortProxyCMS := mockOutPort.NewMockProxyCMS(controller)
	outPortCatalog := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return(jsonCache, nil)

	carouselService := NewCarousel(outPortProxyCMS, outPortCatalog, outPortCache)

	data, err := carouselService.GetSuggested(getContext(), countryID, int64(storeGroupID))

	assert.NoError(t, err)
	assert.Equal(t, data[0].Type, responseGetSuggests.Data[0].Type)
	assert.Equal(t, data[0].ID, responseGetSuggests.Data[0].ID)
}

func TestTestGroupWhenSuccessWithoutCahce(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortProxyCMS := mockOutPort.NewMockProxyCMS(controller)
	outPortCatalog := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortProxyCMS.EXPECT().GetSuggested(gomock.Any(), gomock.Any()).Return(responseGetSuggests, nil)
	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
	outPortCatalog.EXPECT().GetProductsInformationByObjectID(gomock.Any(),
		gomock.Any(), gomock.Any()).Return(responseProductInfo, nil)

	carouselService := NewCarousel(outPortProxyCMS, outPortCatalog, outPortCache)

	data, err := carouselService.GetSuggested(getContext(), countryID, int64(storeGroupID))

	assert.NoError(t, err)
	assert.Equal(t, data[0].Type, responseGetSuggests.Data[0].Type)
	assert.Equal(t, data[0].ID, responseGetSuggests.Data[0].ID)
}

func TestTestGroupWhenSuccessAndSaveCahceError(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortProxyCMS := mockOutPort.NewMockProxyCMS(controller)
	outPortCatalog := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortProxyCMS.EXPECT().GetSuggested(gomock.Any(), gomock.Any()).Return(responseGetSuggests, nil)
	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(errors.New("error"))
	outPortCatalog.EXPECT().GetProductsInformationByObjectID(gomock.Any(),
		gomock.Any(), gomock.Any()).Return(responseProductInfo, nil)

	carouselService := NewCarousel(outPortProxyCMS, outPortCatalog, outPortCache)

	data, err := carouselService.GetSuggested(getContext(), countryID, int64(storeGroupID))

	assert.NoError(t, err)
	assert.Equal(t, data[0].Type, responseGetSuggests.Data[0].Type)
	assert.Equal(t, data[0].ID, responseGetSuggests.Data[0].ID)
}

func TestTestGroupWhenSuccessAndFindCahceError(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortProxyCMS := mockOutPort.NewMockProxyCMS(controller)
	outPortCatalog := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortProxyCMS.EXPECT().GetSuggested(gomock.Any(), gomock.Any()).Return(responseGetSuggests, nil)
	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", errors.New("error"))
	outPortCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
	outPortCatalog.EXPECT().GetProductsInformationByObjectID(gomock.Any(),
		gomock.Any(), gomock.Any()).Return(responseProductInfo, nil)

	carouselService := NewCarousel(outPortProxyCMS, outPortCatalog, outPortCache)

	data, err := carouselService.GetSuggested(getContext(), countryID, int64(storeGroupID))

	assert.NoError(t, err)
	assert.Equal(t, data[0].Type, responseGetSuggests.Data[0].Type)
	assert.Equal(t, data[0].ID, responseGetSuggests.Data[0].ID)
}

func TestTestGroupWhenSuccessAndFindCahceUnmarshalError(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortProxyCMS := mockOutPort.NewMockProxyCMS(controller)
	outPortCatalog := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortProxyCMS.EXPECT().GetSuggested(gomock.Any(), gomock.Any()).Return(responseGetSuggests, nil)
	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("{", nil)
	outPortCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
	outPortCatalog.EXPECT().GetProductsInformationByObjectID(gomock.Any(),
		gomock.Any(), gomock.Any()).Return(responseProductInfo, nil)

	carouselService := NewCarousel(outPortProxyCMS, outPortCatalog, outPortCache)

	data, err := carouselService.GetSuggested(getContext(), countryID, int64(storeGroupID))

	assert.NoError(t, err)
	assert.Equal(t, data[0].Type, responseGetSuggests.Data[0].Type)
	assert.Equal(t, data[0].ID, responseGetSuggests.Data[0].ID)
}

func TestTestGroupWhenSuccessAndNotFoundProduct(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortProxyCMS := mockOutPort.NewMockProxyCMS(controller)
	outPortCatalog := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	responseGetSuggests.Data[0].Sku = "9182"
	outPortProxyCMS.EXPECT().GetSuggested(gomock.Any(), gomock.Any()).Return(responseGetSuggests, nil)
	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCatalog.EXPECT().GetProductsInformationByObjectID(gomock.Any(),
		gomock.Any(), gomock.Any()).Return(responseProductInfo, nil)

	carouselService := NewCarousel(outPortProxyCMS, outPortCatalog, outPortCache)

	data, err := carouselService.GetSuggested(getContext(), countryID, int64(storeGroupID))

	assert.NoError(t, err)
	assert.Equal(t, len(data), 0)

	responseGetSuggests.Data[0].Sku = "6"
}

func TestTestGroupWhenGetProductsInformationByObjectIDError(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortProxyCMS := mockOutPort.NewMockProxyCMS(controller)
	outPortCatalog := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortProxyCMS.EXPECT().GetSuggested(gomock.Any(), gomock.Any()).Return(responseGetSuggests, nil)
	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCatalog.EXPECT().GetProductsInformationByObjectID(gomock.Any(),
		gomock.Any(), gomock.Any()).Return(responseProductInfo, errors.New("error"))

	carouselService := NewCarousel(outPortProxyCMS, outPortCatalog, outPortCache)

	data, err := carouselService.GetSuggested(getContext(), countryID, int64(storeGroupID))

	assert.NoError(t, err)
	assert.Equal(t, len(data), 0)
}

func TestTestGroupWhenGetSuggestedError(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortProxyCMS := mockOutPort.NewMockProxyCMS(controller)
	outPortCatalog := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortProxyCMS.EXPECT().GetSuggested(gomock.Any(), gomock.Any()).Return(responseGetSuggests, errors.New("error"))
	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)

	carouselService := NewCarousel(outPortProxyCMS, outPortCatalog, outPortCache)

	_, err := carouselService.GetSuggested(getContext(), countryID, int64(storeGroupID))

	assert.Error(t, err)
}

func getContext() *gin.Context {
	gin.SetMode(gin.TestMode)

	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header), URL: &url.URL{}}

	ctx.Set("X-Correlation-ID", "1291823jhau1uha")

	return ctx
}
