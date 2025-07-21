package handler

import (
	"errors"
	"ftd-td-home-read-services/internal/offer/domain/model"
	mockInPort "ftd-td-home-read-services/test/mocks/offer/domain/ports/in"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHandler_GetFlashOffer_Success(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockOfferInPort := mockInPort.NewMockOfferInPort(ctrl)

	expectedOffers := []model.FlashOffer{
		{Id: "1", Type: "A", Position: 2, RedirectUrl: "url", StartDate: "2024-01-01", EndDate: "2024-01-02", AvailableStockFlash: 10},
	}
	mockOfferInPort.EXPECT().GetFlashOffer(gomock.Any()).Return(expectedOffers, nil)

	h := NewOfferHandler(mockOfferInPort)

	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.GET("/offer/flash", h.GetFlashOffer)

	req, _ := http.NewRequest("GET", "/offer/flash", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	// Puedes agregar más asserts sobre el body si lo deseas
}

func TestHandler_GetFlashOffer_Error(t *testing.T) {
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()
	mockOfferInPort := mockInPort.NewMockOfferInPort(ctrl)

	mockOfferInPort.EXPECT().GetFlashOffer(gomock.Any()).Return(nil, errors.New("some error"))

	h := NewOfferHandler(mockOfferInPort)

	gin.SetMode(gin.TestMode)
	r := gin.New()
	r.GET("/offer/flash", h.GetFlashOffer)

	req, _ := http.NewRequest("GET", "/offer/flash", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	assert.Equal(t, http.StatusConflict, w.Code)
	// Puedes agregar más asserts sobre el body si lo deseas
}
