package response

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

type genericResponse struct {
	Code    string `json:"code"`
	Message string `json:"message"`
	Data    any    `json:"data"`
}

func Ok(c *gin.Context, data any) {
	buildHttpResponse(c, http.StatusOK, "Success", data)
}

func OK(c *gin.Context, message string) {
	buildHttpResponse(c, http.StatusOK, message, nil)
}

func BadRequest(c *gin.Context, message string) {
	buildHttpErrorResponse(c, http.StatusBadRequest, message, nil)
}

func ServerError(c *gin.Context, message string) {
	buildHttpErrorResponse(c, http.StatusInternalServerError, message, nil)
}

func ConflictError(c *gin.Context, message string) {
	buildHttpErrorResponse(c, http.StatusConflict, message, nil)
}

func buildHttpResponse(c *gin.Context, code int, message string, data any) {
	c.JSON(code, buildGenericResponse(code, message, data))
}

func buildHttpErrorResponse(c *gin.Context, code int, message string, data any) {
	c.AbortWithStatusJSON(code, buildGenericResponse(code, message, data))
}

func buildGenericResponse(code int, message string, data any) genericResponse {
	if message == "" {
		message = http.StatusText(code)
	}

	return genericResponse{
		Code:    http.StatusText(code),
		Message: message,
		Data:    data,
	}
}
