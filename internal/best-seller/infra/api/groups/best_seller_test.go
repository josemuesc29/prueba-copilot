package groups

import (
	mockHandler "ftd-td-catalog-item-read-services/test/mocks/best-seller/infra/api/handler"
	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
	"testing"
)

func TestBestSellerGroup(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	mockCarouselHandlerTest := mockHandler.NewMockBestSeller(controller)

	testGroup := NewBestSeller(mockCarouselHandlerTest)

	routerGroup := gin.Default()

	testGroup.Source(&routerGroup.RouterGroup)
}
