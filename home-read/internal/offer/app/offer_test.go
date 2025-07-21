package app

import (
	"encoding/json"
	"errors"
	"ftd-td-home-read-services/internal/offer/domain/model"
	sharedModel "ftd-td-home-read-services/internal/shared/domain/model"
	mockOutPort "ftd-td-home-read-services/test/mocks/offer/domain/ports/out"
	sharedOutPorts "ftd-td-home-read-services/test/mocks/shared/domain/ports/out"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestOffer_GetFlashOffer_CacheHit(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCms := mockOutPort.NewMockOfferCmsOutPort(ctrl)
	mockCatalog := sharedOutPorts.NewMockCatalogProduct(ctrl)
	mockCache := sharedOutPorts.NewMockCache(ctrl)

	o := NewOfferService(mockCms, mockCatalog, mockCache)

	c := getTestContext()

	flashOffers := []model.FlashOffer{{Id: "1"}}
	flashOffersBytes, _ := json.Marshal(flashOffers)
	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return(string(flashOffersBytes), nil)

	result, err := o.GetFlashOffer(c)
	assert.NoError(t, err)
	assert.Equal(t, flashOffers, result)
}

func TestOffer_GetFlashOffer_CacheMiss_And_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCms := mockOutPort.NewMockOfferCmsOutPort(ctrl)
	mockCatalog := sharedOutPorts.NewMockCatalogProduct(ctrl)
	mockCache := sharedOutPorts.NewMockCache(ctrl)

	o := NewOfferService(mockCms, mockCatalog, mockCache)

	c := getTestContext()

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	mockCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)
	mockCms.EXPECT().GetFlashOffer(gomock.Any(), "AR").Return([]model.FlashOffer{{Id: "1"}}, nil)
	mockCatalog.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{"1"}, "AR").
		Return([]sharedModel.ProductInformation{{ObjectID: "1"}}, nil)

	// Mapper debe ser mockeado si es posible, aquí se asume que no falla
	result, err := o.GetFlashOffer(c)
	assert.NoError(t, err)
	assert.Len(t, result, 1)
}

func TestOffer_GetFlashOffer_ErrorCms(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCms := mockOutPort.NewMockOfferCmsOutPort(ctrl)
	mockCatalog := sharedOutPorts.NewMockCatalogProduct(ctrl)
	mockCache := sharedOutPorts.NewMockCache(ctrl)

	o := NewOfferService(mockCms, mockCatalog, mockCache)

	c := getTestContext()

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	mockCms.EXPECT().GetFlashOffer(gomock.Any(), "AR").Return(nil, errors.New("cms error"))

	result, err := o.GetFlashOffer(c)
	assert.Error(t, err)
	assert.Nil(t, result)
}

func TestOffer_GetFlashOffer_ErrorCatalog(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCms := mockOutPort.NewMockOfferCmsOutPort(ctrl)
	mockCatalog := sharedOutPorts.NewMockCatalogProduct(ctrl)
	mockCache := sharedOutPorts.NewMockCache(ctrl)

	o := NewOfferService(mockCms, mockCatalog, mockCache)

	c := getTestContext()

	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", nil)
	mockCms.EXPECT().GetFlashOffer(gomock.Any(), "AR").Return([]model.FlashOffer{{Id: "1"}}, nil)
	mockCatalog.EXPECT().GetProductsInformationByObjectID(gomock.Any(), []string{"1"}, "AR").
		Return(nil, errors.New("catalog error"))

	result, err := o.GetFlashOffer(c)
	assert.Error(t, err)
	assert.Nil(t, result)
}

func TestOffer_getProductIds(t *testing.T) {
	o := &offer{}
	flashOffers := []model.FlashOffer{{Id: "1"}, {Id: "2"}}
	ids := o.getProductIds(&flashOffers)
	assert.Equal(t, []string{"1", "2"}, ids)
}

func TestOffer_findFlashOfferInCache_Error(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCache := sharedOutPorts.NewMockCache(ctrl)
	o := &offer{outPortCache: mockCache}

	c := getTestContext()
	mockCache.EXPECT().Get(gomock.Any(), gomock.Any()).Return("", errors.New("cache error"))

	var resp []model.FlashOffer
	o.findFlashOfferInCache(c, "AR", &resp)
	assert.Empty(t, resp)
}

func TestOffer_saveFlashOfferInCache_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockCache := sharedOutPorts.NewMockCache(ctrl)
	o := &offer{outPortCache: mockCache}

	c := getTestContext()
	flashOffers := []model.FlashOffer{{Id: "1"}}
	mockCache.EXPECT().Set(gomock.Any(), gomock.Any(), gomock.Any(), gomock.Any()).Return(nil)

	o.saveFlashOfferInCache(c, "AR", flashOffers)
}

func TestOffer_saveFlashOfferInCache_Empty(t *testing.T) {
	o := &offer{}
	c := &gin.Context{}
	o.saveFlashOfferInCache(c, "AR", []model.FlashOffer{})
}

func getTestContext() *gin.Context {
	gin.SetMode(gin.TestMode)

	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())
	ctx.Request = &http.Request{Header: make(http.Header)}
	ctx.Params = append(ctx.Params, gin.Param{Key: "countryId", Value: "AR"})

	return ctx
}
