package utils

import (
	"context"
	"ftd-td-home-read-services/internal/shared/domain/model/enums"

	log "github.com/sirupsen/logrus"
)

func LogInfo(ctx context.Context, component, message string) {
	log.Infof(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), component, message)
}

func LogWarn(ctx context.Context, component, message string) {
	log.Warnf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), component, message)
}

func LogError(ctx context.Context, component, message string) {
	log.Errorf(enums.LogFormat, ctx.Value(enums.HeaderCorrelationID), component, message)
}
