package in

import "github.com/gin-gonic/gin"

type SeoInPort interface {
	GetProductSeo(c *gin.Context) (string, error)
}
