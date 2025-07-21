package groups

import (
	mockHandler "ftd-td-home-read-services/test/mocks/offer/infra/api/handler"
	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
	"testing"
)

func TestOfferGroup(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	handlerTest := mockHandler.NewMockHandler(controller)
	testGroup := NewOfferGroup(handlerTest)

	routerGroup := gin.Default()

	testGroup.Source(&routerGroup.RouterGroup)
}
