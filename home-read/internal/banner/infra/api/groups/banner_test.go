package groups

import (
	mockHandler "ftd-td-home-read-services/test/mocks/banner/infra/api/handler"
	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
	"testing"
)

func TestBannerGroup(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	handlerTest := mockHandler.NewMockHandler(controller)
	testGroup := NewBannerGroup(handlerTest)

	routerGroup := gin.Default()

	testGroup.Source(&routerGroup.RouterGroup)
}
