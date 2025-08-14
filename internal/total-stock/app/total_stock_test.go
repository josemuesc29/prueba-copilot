package app_test

import (
	"encoding/json"
	"errors"
	"ftd-td-catalog-item-read-services/cmd/config"
	"testing"
	"time"

	"ftd-td-catalog-item-read-services/internal/total-stock/app"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	mock_shared_ports "ftd-td-catalog-item-read-services/test/mocks/shared/domain/ports/out"
	mock_ports "ftd-td-catalog-item-read-services/test/mocks/total-stock/domain/ports/out"

	"github.com/gin-gonic/gin"
	"go.uber.org/mock/gomock"
	"github.com/stretchr/testify/assert"
)

func TestTotalStockService_GetTotalStockByItem_FromCache(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockRepo := mock_ports.NewMockTotalStockOutPort(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	service := app.NewTotalStockService(mockRepo, mockCache)

	c, _ := gin.CreateTestContext(nil)
	c.Set(enums.HeaderCorrelationID, "test-id")

	countryID := "AR"
	itemID := "123"
	storeIDs := []string{"1", "2"}
	expectedStock := int64(50)
	cachedValue, _ := json.Marshal(expectedStock)

	mockCache.EXPECT().Get(gomock.Any(), "total_stock_AR_123_1,2").Return(string(cachedValue), nil)

	// Act
	stock, err := service.GetTotalStockByItem(c, countryID, itemID, storeIDs)

	// Assert
	assert.NoError(t, err)
	assert.Equal(t, expectedStock, stock)
}

func TestTotalStockService_GetTotalStockByItem_FromRepo(t *testing.T) {
	// Arrange
	config.Enviroments.RedisTotalStockTTL = 10 // Set TTL for test
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockRepo := mock_ports.NewMockTotalStockOutPort(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	service := app.NewTotalStockService(mockRepo, mockCache)

	c, _ := gin.CreateTestContext(nil)
	c.Set(enums.HeaderCorrelationID, "test-id")

	countryID := "AR"
	itemID := "123"
	storeIDs := []string{"1", "2"}
	expectedStock := int64(75)
	stockToCache, _ := json.Marshal(expectedStock)

	mockCache.EXPECT().Get(gomock.Any(), "total_stock_AR_123_1,2").Return("", nil)
	mockRepo.EXPECT().GetStockByItemAndStores(countryID, itemID, storeIDs).Return(expectedStock, nil)
	mockCache.EXPECT().Set(gomock.Any(), "total_stock_AR_123_1,2", string(stockToCache), 10*time.Minute).Return(nil)

	// Act
	stock, err := service.GetTotalStockByItem(c, countryID, itemID, storeIDs)

	// Assert
	assert.NoError(t, err)
	assert.Equal(t, expectedStock, stock)
}

func TestTotalStockService_GetTotalStockByItem_RepoError(t *testing.T) {
	// Arrange
	ctrl := gomock.NewController(t)
	defer ctrl.Finish()

	mockRepo := mock_ports.NewMockTotalStockOutPort(ctrl)
	mockCache := mock_shared_ports.NewMockCache(ctrl)
	service := app.NewTotalStockService(mockRepo, mockCache)

	c, _ := gin.CreateTestContext(nil)
	c.Set(enums.HeaderCorrelationID, "test-id")

	countryID := "AR"
	itemID := "123"
	storeIDs := []string{"1", "2"}
	repoError := errors.New("database error")

	mockCache.EXPECT().Get(gomock.Any(), "total_stock_AR_123_1,2").Return("", nil)
	mockRepo.EXPECT().GetStockByItemAndStores(countryID, itemID, storeIDs).Return(int64(0), repoError)

	// Act
	stock, err := service.GetTotalStockByItem(c, countryID, itemID, storeIDs)

	// Assert
	assert.Error(t, err)
	assert.Equal(t, repoError, err)
	assert.Equal(t, int64(0), stock)
}
