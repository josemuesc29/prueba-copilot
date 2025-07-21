package middleware

import (
	"fmt"
	"ftd-td-home-read-services/internal/banner/infra/enum"
	"ftd-td-home-read-services/internal/shared/infra/api/handler/dto/response"
	"github.com/gin-gonic/gin"
)

const invalidBannerIDPrefix = "ID BANNER %s inválido "

func ValidateIDBanner() gin.HandlerFunc {
	return func(c *gin.Context) {
		bannerID := c.Param("bannerId")
		if !isValidBanner(bannerID) {
			response.BadRequest(c, fmt.Sprintf(invalidBannerIDPrefix, bannerID))
			return
		}
		c.Next()
	}
}

func isValidBanner(bannerID string) bool {
	return enum.ValidBannerNames[bannerID]
}
