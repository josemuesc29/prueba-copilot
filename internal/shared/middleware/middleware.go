package middleware

import (
	"fmt"
	"ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response"
	"strings"

	"github.com/gin-gonic/gin"
)

const invalidCountryID = "País %s inválido, debe ser uno de los siguientes: AR, CO, VE."

func ValidateCountryID() gin.HandlerFunc {
	return func(c *gin.Context) {
		countryID := c.Param("countryId")
		if !isValidCountry(countryID) {
			response.BadRequest(c, fmt.Sprintf(invalidCountryID, countryID))
			return
		}
		c.Next()
	}
}

func isValidCountry(countryID string) bool {
	validCountries := map[string]bool{
		"ve": true,
		"co": true,
		"ar": true,
	}
	return validCountries[strings.ToLower(countryID)]
}
