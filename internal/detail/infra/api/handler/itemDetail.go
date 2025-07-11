package handler

import (
	"fmt"
	"ftd-td-catalog-item-read-services/internal/detail/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/detail/infra/api/handler/mapper"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
)

const (
	service = "handler.ItemDetail"
)

type handler struct {
	detailInPort in.DetailInPort
}

type Handler interface {
	GetProductDetail(c *gin.Context)
}

func NewDetailHandler(detailInPort in.DetailInPort) Handler {
	return &handler{
		detailInPort: detailInPort,
	}
}

func (h *handler) GetProductDetail(c *gin.Context) {
	var correlationID = utils.GetCorrelationID(c.Request.Header.Get(enums.HeaderCorrelationID))

	c.Set(enums.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums.HeaderCorrelationID, correlationID)
	log.Printf(enums.LogFormat, correlationID, service, enums.ReadDataFromContext)

	itemDetail, err := h.detailInPort.GetDetailProduct(c)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, service, fmt.Sprintf("Error in GetDetailProduct: %v", err))
		response.ConflictError(c, "No fue posible obtener el detalle del producto")
		return
	}

	// TotalStock is a placeholder value for demonstration purposes.
	itemDetail.TotalStock = 20
	response.Ok(c, mapper.GetItemDetailResponseFromItemDetail(itemDetail))

}
