package utils

import "github.com/google/uuid"

func GetCorrelationID(correlationIDFromHeader string) string {
	if correlationIDFromHeader == "" {
		return uuid.New().String()
	} else {
		return correlationIDFromHeader
	}
}

// PropagateHeader asegura que un header específico del request original
// esté disponible en el contexto Gin (en ctx.Keys) para uso posterior.
func PropagateHeader(ctx *gin.Context, headerName string) {
	if ctx == nil {
		return
	}
	headerValue := ctx.GetHeader(headerName) // Lee del request original
	if headerValue != "" {
		// Establecerlo en el mapa de valores de Gin Context para que otras funciones
		// puedan acceder a él consistentemente usando ctx.GetString(headerName) o ctx.Get(headerName)
		ctx.Set(headerName, headerValue)
	}
}
