package groups

import (
	mockHandler "ftd-td-catalog-item-read-services/test/mocks/products-related/infra/api/handler"
	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
	"testing"
)

func TestProductsRelatedHandlerTestGroup(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	mockProductsRelatedHandlerTest := mockHandler.NewMockProductsRelatedHandler(controller)

	testGroup := NewProductsRelatedGroup(mockProductsRelatedHandlerTest)

	routerGroup := gin.Default()

	testGroup.Source(&routerGroup.RouterGroup)
}
