package client

import (
	"context"
	"ftd-td-catalog-item-read-services/cmd/config"
	"time"

	"github.com/redis/go-redis/v9"
	log "github.com/sirupsen/logrus"
)

type RedisClient struct {
	Client *redis.Client
}

func NewRedisClient() (*RedisClient, error) {
	client := redis.NewClient(&redis.Options{
		Addr:         config.Enviroments.RedisHost + ":" + config.Enviroments.RedisPort,
		Password:     "",
		DB:           0,
		ReadTimeout:  5 * time.Second,
		WriteTimeout: 10 * time.Second,
		PoolSize:     10,
		PoolTimeout:  30 * time.Second,
	})

	_, err := client.Ping(context.Background()).Result()
	if err != nil {
		return nil, err
	}

	log.Println("Connected to redis")
	return &RedisClient{Client: client}, nil
}
