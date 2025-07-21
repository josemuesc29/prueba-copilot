package repository

import (
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"net/http/httptest"
	"testing"
	"time"

	cacheClient "ftd-td-home-read-services/internal/shared/infra/adapters/cache/client"
	"github.com/go-redis/redismock/v9"
)

var (
	key   = "mykey"
	value = "myvalue"
)

func TestGetRedis_WhenSucces(t *testing.T) {
	client, mock := redismock.NewClientMock()
	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())

	mock.ExpectGet(key).SetVal(value)

	redisClient := &cacheClient.RedisClient{Client: client}

	cacheRepo := NewCache(redisClient)

	result, err := cacheRepo.Get(ctx, key)

	assert.Nil(t, err)
	assert.Equal(t, value, result)
}

func TestSetRedis_WhenSucces(t *testing.T) {
	client, mock := redismock.NewClientMock()
	ctx, _ := gin.CreateTestContext(httptest.NewRecorder())

	mock.ExpectSet(key, value, 10*time.Second).SetVal("OK")

	redisClient := &cacheClient.RedisClient{Client: client}

	cacheRepo := NewCache(redisClient)

	err := cacheRepo.Set(ctx, key, value, 10*time.Second)

	assert.Nil(t, err)
}
