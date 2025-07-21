package groups

import (
	mockhandler "ftd-td-home-read-services/test/mocks/structure/infra/api/handler"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"go.uber.org/mock/gomock"
)

const (
	routerPath = "/home/r/:countryId/v1"
	groupPath  = "/structure"
)

func TestStructureGroup(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockStructureHandler := mockhandler.NewMockStructureHandler(ctrl)

	router := gin.Default()
	routerGroup := router.Group(routerPath)

	group := NewStructureGroup(mockStructureHandler)

	mockStructureHandler.EXPECT().GetStructure(gomock.Any()).Times(1)

	// Act
	group.Source(routerGroup)

	req, _ := http.NewRequest(http.MethodPost, routerPath+groupPath, nil)
	w := httptest.NewRecorder()

	router.ServeHTTP(w, req)

	// Assert
	assert.Equal(t, http.StatusOK, w.Code)
}
