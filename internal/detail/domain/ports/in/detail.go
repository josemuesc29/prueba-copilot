package in

import (
	"ftd-td-catalog-item-read-services/internal/detail/domain/model"
	"github.com/gin-gonic/gin"
)

//go:generate mockgen -source=detail.go -destination=../../../../../test/mocks/detail/domain/ports/in/detail_mock.go

type DetailInPort interface {
	GetDetailProduct(c *gin.Context) (model.ItemDetail, error)
}
