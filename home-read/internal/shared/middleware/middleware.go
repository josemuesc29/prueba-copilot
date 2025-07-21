package middleware

import (
	"fmt"
	"ftd-td-home-read-services/internal/shared/infra/api/handler/dto/response"
	"strings"

	"github.com/gin-gonic/gin"
)

const invalidCountryID = "País %s inválido, debe ser uno de los siguientes: AR, CO, VE."
const invalidSource = "SOURCE %s inválido, debe ser uno de los siguientes: WEB,RESPONSIVE,IOS,ANDROID."

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

func ValidateSOURCE() gin.HandlerFunc {
	return func(c *gin.Context) {
		source := c.GetHeader("SOURCE")
		if !isValidSource(source) {
			response.BadRequest(c, fmt.Sprintf(invalidSource, source))
			return
		}
		c.Next()
	}
}

func isValidSource(source string) bool {
	validSources := map[string]bool{
		"WEB":        true,
		"RESPONSIVE": true,
		"IOS":        true,
		"ANDROID":    true,
	}
	return validSources[source]
}
