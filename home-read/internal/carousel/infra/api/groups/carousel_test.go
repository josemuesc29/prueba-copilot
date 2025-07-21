package groups

import (
	mockHandler "ftd-td-home-read-services/test/mocks/carousel/infra/api/handler"
	"testing"

	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
)

func TestCarouselGroup(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	mockCarouselHandlerTest := mockHandler.NewMockCarousel(controller)

	testGroup := NewCarousel(mockCarouselHandlerTest)

	routerGroup := gin.Default()

	testGroup.Source(&routerGroup.RouterGroup)
}
