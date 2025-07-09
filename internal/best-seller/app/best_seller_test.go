package app

import (
	"errors"
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	mockOutPort "ftd-td-catalog-item-read-services/test/mocks/best-seller/domain/ports/out"
	mockSharedOutPort "ftd-td-catalog-item-read-services/test/mocks/shared/domain/ports/out"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
	"net/http"
	"net/http/httptest"
	"net/url"
	"testing"
)

var (
	countryID               = "AR"
	departmentID            = "12345"
	storeID                 = "67890"
	jsonCache               = "[\n        {\n            \"mediaImageUrl\": \"https://lh3.googleusercontent.com/exTrlxb4DDDXvPfaRL_RJL0cncl69eWW1vUf2J_DiIf2b7mstsK1Haj7vrfmhI7dcKUV5dsVC15tvm1llwJ_4DdNDBV8tvpP5A\",\n            \"description\": \"CREMA CALENDULA EWE X 60 GR\",\n            \"fullPrice\": 7960,\n            \"mediaDescription\": \"CREMA CALENDULA EWE X 60 GR\",\n            \"brand\": \"\",\n            \"sales\": 0,\n            \"detailDescription\": \"CREMA CALENDULA EWE X 60 GR \",\n            \"offerPrice\": 0,\n            \"offerDescription\": \"\",\n            \"id\": \"300561299\",\n            \"offerText\": \"\",\n            \"idStoreGroup\": 26,\n            \"marca\": \"\",\n            \"objectID\": \"300561299\",\n            \"onlyOnline\": false,\n            \"deliveryTime\": \"\",\n            \"highlight\": false,\n            \"generic\": false,\n            \"largeDescription\": \"\",\n            \"anywaySelling\": false,\n            \"spaces\": 1,\n            \"status\": \"\",\n            \"taxRate\": 0,\n            \"listUrlImages\": [],\n            \"measurePum\": 0,\n            \"labelPum\": \"\",\n            \"id_highlights\": null,\n            \"id_suggested\": null,\n            \"departments\": null,\n            \"subCategory\": \"\",\n            \"supplier\": \"\",\n            \"outofstore\": false,\n            \"offerStartDate\": 0,\n            \"offerEndDate\": 0,\n            \"primePrice\": 0,\n            \"primeTextDiscount\": \"\",\n            \"primeDescription\": \"\",\n            \"rms_class\": \"BEBE BANO\",\n            \"rms_deparment\": \"BEBE CUID PERSONAL\",\n            \"rms_group\": \"HIGIENE DEL BEBE\",\n            \"rms_subclass\": \"BEBE JABON/GEL BANO\",\n            \"without_stock\": false,\n            \"url\": \"\"\n        }\n    ]"
	responseCatalogCtaegory = sharedModel.CatalogCategory{
		CountryID: "AR",
		Name:      "Cuidado Personal",
		Active:    true,
		Order:     10,
	}
	responseConfigBestSeller = sharedModel.ConfigBestSeller{
		ObjectID:         "BEST_SELLER_AR",
		AlgoliaRecommend: false,
		CountItems:       1,
		QueryProducts:    "query",
	}
	responseBestSellerByDepartment = []model.BestSellerDepartmentEntity{
		{
			CountryID:        countryID,
			ItemBestSellerID: "300561299",
			DepartmentID:     departmentID,
			ItemID:           "300561299",
		},
	}
	responseProductsInformation = []sharedModel.ProductInformation{
		{
			Prime:               "Sí",
			RMSProvider:         "ProveedorX",
			AnywaySelling:       true,
			Barcode:             "1234567890123",
			Brand:               "MarcaX",
			Categorie:           "CategoríaX",
			CustomTag:           nil,
			DeliveryTime:        "24h",
			Departments:         []string{"Depto1", "Depto2"},
			DesHiddenSEO:        nil,
			FullPrice:           15000.0,
			Generics:            false,
			GrayDescription:     "Descripción gris",
			HasStock:            true,
			Highlight:           true,
			ID:                  "300561299",
			IDOffersGroup:       []int{1, 2},
			IDHighlights:        []int{3, 4},
			IDSuggested:         []int{5, 6},
			IsCupon:             0,
			LabelPum:            "kg",
			LargeDescription:    "Descripción larga del producto",
			ListUrlImages:       []string{"img1.jpg", "img2.jpg"},
			Marca:               "MarcaX",
			MeasurePum:          1.5,
			MediaDescription:    "Descripción media",
			MediaImageUrl:       "https://example.com/img.jpg",
			MetadesSEO:          nil,
			MetatituloSEO:       nil,
			ObjectID:            "300561299",
			OfferDescription:    "Oferta especial",
			OfferEndDate:        1710009999,
			OfferPrice:          12000.0,
			OfferPriceByCity:    []interface{}{},
			OfferStartDate:      1710000000,
			OfferText:           "¡Aprovecha!",
			OnlyOnline:          true,
			Outofstore:          false,
			PrimeDescription:    "Prime desc",
			PrimePrice:          11000.0,
			PrimeTextDiscount:   "10% dcto prime",
			RequirePrescription: false,
			Sales:               100,
			Spaces:              5,
			Status:              "activo",
			SubCategory:         "SubcatX",
			Supplier:            "ProveedorX",
			TaxRate:             19,
			URL:                 "https://example.com/prod-1",
			UrlCanonical:        nil,
		},
	}
)

func TestGetBestSellerDepartmentWhenSuccessWithCahce(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return(jsonCache, nil)

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	data, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.NoError(t, err)
	assert.Equal(t, data[0].ID, "300561299")
	assert.Equal(t, data[0].Brand, "")
}

func TestGetBestSellerDepartmentWhenSuccessFromDB(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)
	outPortConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseConfigBestSeller, nil)
	outPortBestSellerDB.EXPECT().GetBestSellerDepartment(gomock.Any(), gomock.Any()).Return(&responseBestSellerByDepartment, nil)
	outPortCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseProductsInformation, nil)

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	data, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.NoError(t, err)
	assert.Equal(t, data[0].ID, "300561299")
	assert.Equal(t, data[0].Brand, "MarcaX")
}

func TestGetBestSellerDepartmentWhenGetCategoryByDepartmentFail(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(sharedModel.CatalogCategory{}, errors.New("error"))

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	_, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.Error(t, err)
}

func TestGetBestSellerDepartmentWhenGetCategoryByDepartmentNameIsEmpty(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	responseCatalogCtaegory.Name = ""

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	_, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.Error(t, err)

	responseCatalogCtaegory.Name = "Cuidado Personal"
}

func TestGetBestSellerDepartmentWhenSaveInCacheFail(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(errors.New("error"))
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)
	outPortConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseConfigBestSeller, nil)
	outPortBestSellerDB.EXPECT().GetBestSellerDepartment(gomock.Any(), gomock.Any()).Return(&responseBestSellerByDepartment, nil)
	outPortCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseProductsInformation, nil)

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	data, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.NoError(t, err)
	assert.Equal(t, data[0].ID, "300561299")
	assert.Equal(t, data[0].Brand, "MarcaX")
}

func TestGetBestSellerDepartmentWhenSuccesWithCahceFail(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", errors.New("error"))
	outPortCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)
	outPortConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseConfigBestSeller, nil)
	outPortBestSellerDB.EXPECT().GetBestSellerDepartment(gomock.Any(), gomock.Any()).Return(&responseBestSellerByDepartment, nil)
	outPortCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseProductsInformation, nil)

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	data, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.NoError(t, err)
	assert.Equal(t, data[0].ID, "300561299")
	assert.Equal(t, data[0].Brand, "MarcaX")
}

func TestGetBestSellerDepartmentWhenSuccesWithCahceFailUnmarshal(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("{", nil)
	outPortCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)
	outPortConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseConfigBestSeller, nil)
	outPortBestSellerDB.EXPECT().GetBestSellerDepartment(gomock.Any(), gomock.Any()).Return(&responseBestSellerByDepartment, nil)
	outPortCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseProductsInformation, nil)

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	data, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.NoError(t, err)
	assert.Equal(t, data[0].ID, "300561299")
	assert.Equal(t, data[0].Brand, "MarcaX")
}

func TestGetBestSellerDepartmentWhenSuccesButProductsNotFound(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	responseProductsInformation[0].ObjectID = "120"

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)
	outPortConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseConfigBestSeller, nil)
	outPortBestSellerDB.EXPECT().GetBestSellerDepartment(gomock.Any(), gomock.Any()).Return(&responseBestSellerByDepartment, nil)
	outPortCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseProductsInformation, nil)

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	_, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.NoError(t, err)

	responseProductsInformation[0].ObjectID = "300561299"
}

func TestGetBestSellerDepartmentWhenGetProductsInformationByObjectIDFail(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)
	outPortConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseConfigBestSeller, nil)
	outPortBestSellerDB.EXPECT().GetBestSellerDepartment(gomock.Any(), gomock.Any()).Return(&responseBestSellerByDepartment, nil)
	outPortCatalogProduct.EXPECT().GetProductsInformationByObjectID(gomock.Any(), gomock.Any(), gomock.Any()).Return([]sharedModel.ProductInformation{}, errors.New("error"))

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	_, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.Error(t, err)
}

func TestGetBestSellerDepartmentWhenGetBestSellersInAlgoliaByQuery(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)
	outPortConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseConfigBestSeller, nil)
	outPortBestSellerDB.EXPECT().GetBestSellerDepartment(gomock.Any(), gomock.Any()).Return(&[]model.BestSellerDepartmentEntity{}, nil)
	outPortCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseProductsInformation, nil)

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	data, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.NoError(t, err)
	assert.Equal(t, data[0].ID, "300561299")
	assert.Equal(t, data[0].Brand, "MarcaX")
}

func TestGetBestSellerDepartmentWhenGetBestSellersInAlgoliaByQueryFail(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)
	outPortConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseConfigBestSeller, nil)
	outPortBestSellerDB.EXPECT().GetBestSellerDepartment(gomock.Any(), gomock.Any()).Return(&[]model.BestSellerDepartmentEntity{}, nil)
	outPortCatalogProduct.EXPECT().GetProductsInformationByQuery(gomock.Any(), gomock.Any(), gomock.Any()).Return([]sharedModel.ProductInformation{}, errors.New("error"))

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	_, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.Error(t, err)
}

func TestGetBestSellerDepartmentWhenGetBestSellerDepartmentFail(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)
	outPortConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseConfigBestSeller, nil)
	outPortBestSellerDB.EXPECT().GetBestSellerDepartment(gomock.Any(), gomock.Any()).Return(&[]model.BestSellerDepartmentEntity{}, errors.New("error"))

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	_, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.Error(t, err)
}

func TestGetBestSellerDepartmentWhenGetAlgoliaRecommendSuccess(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	responseConfigBestSeller.AlgoliaRecommend = true

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)
	outPortConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseConfigBestSeller, nil)

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	data, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.NoError(t, err)
	assert.Equal(t, len(data), 0)
	responseConfigBestSeller.AlgoliaRecommend = false
}

func TestGetBestSellerDepartmentWhenGetConfigBestSellerFail(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	outPortBestSellerDB := mockOutPort.NewMockBestSellerOutPort(controller)
	outPortConfig := mockSharedOutPort.NewMockConfigOutPort(controller)
	outPortCatalogCategory := mockSharedOutPort.NewMockCatalogCategoryOutPort(controller)
	outPortCatalogProduct := mockSharedOutPort.NewMockCatalogProduct(controller)
	outPortCache := mockSharedOutPort.NewMockCache(controller)

	responseConfigBestSeller.AlgoliaRecommend = true

	outPortCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	outPortCatalogCategory.EXPECT().GetCategoryByDepartment(gomock.Any(), gomock.Any(), gomock.Any()).Return(responseCatalogCtaegory, nil)
	outPortConfig.EXPECT().GetConfigBestSeller(gomock.Any(), gomock.Any(), gomock.Any()).Return(sharedModel.ConfigBestSeller{}, errors.New("error"))

	bestSellerService := NewBestSeller(outPortBestSellerDB, outPortConfig, outPortCatalogCategory,
		outPortCatalogProduct, outPortCache)

	_, err := bestSellerService.GetBestSellerDepartment(getContext(), countryID, departmentID, storeID)

	assert.Error(t, err)
}

func getContext() *gin.Context {
	gin.SetMode(gin.TestMode)

	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header), URL: &url.URL{}}

	ctx.Set("X-Correlation-ID", "1291823jhau1uha")

	return ctx
}
