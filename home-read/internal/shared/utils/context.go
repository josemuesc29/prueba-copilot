package utils

import "github.com/google/uuid"

func GetCorrelationID(correlationIDFromHeader string) string {
	if correlationIDFromHeader == "" {
		return uuid.New().String()
	} else {
		return correlationIDFromHeader
	}
}
