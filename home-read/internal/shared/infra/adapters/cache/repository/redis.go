package repository

import (
	"context"
	"time"

	"ftd-td-home-read-services/internal/shared/domain/ports/out"
	cacheClient "ftd-td-home-read-services/internal/shared/infra/adapters/cache/client"
)

type cache struct {
	redisClient *cacheClient.RedisClient
}

func NewCache(redisClient *cacheClient.RedisClient) out.Cache {
	return &cache{redisClient: redisClient}
}

func (t cache) Get(c context.Context, key string) (string, error) {
	result := t.redisClient.Client.Get(c, key)
	return result.Val(), result.Err()
}

func (t cache) Set(c context.Context, key string, value string, duration time.Duration) error {
	result := t.redisClient.Client.Set(c, key, value, duration)
	return result.Err()
}
