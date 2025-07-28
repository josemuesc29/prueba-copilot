package groups

import (
	mockHandler "ftd-td-catalog-item-read-services/test/mocks/structure/infra/api/handler"
	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
	"testing"
)

func TestStructureGroup(t *testing.T) {
	controller := gomock.NewController(t)
	defer controller.Finish()

	mockStructureHandlerTest := mockHandler.NewMockItemStructureHandler(controller)

	testGroup := NewStructureGroup(mockStructureHandlerTest)

	routerGroup := gin.Default()

	testGroup.Source(&routerGroup.RouterGroup)
}
