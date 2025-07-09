package out

//go:generate mockgen -source=cache.go -destination=../../../../../test/mocks/shared/domain/ports/out/cache_mock.go

import (
	"context"
	"time"
)

type Cache interface {
	Get(c context.Context, key string) (string, error)
	Set(c context.Context, key string, value string, duration time.Duration) error
}
