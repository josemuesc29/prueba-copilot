package groups

import (
	mockHandler "ftd-td-catalog-item-read-services/test/mocks/same-brand/infra/api/handler"
	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
	"testing"
)

func TestSameBrandGroup(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	mockSameBrandHandlerTest := mockHandler.NewMockSameBrand(controller)

	testGroup := NewSameBrand(mockSameBrandHandlerTest)

	routerGroup := gin.Default()

	testGroup.Source(&routerGroup.RouterGroup)
}
