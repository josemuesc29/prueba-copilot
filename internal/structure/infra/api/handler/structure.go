package handler

import (
	"fmt"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"ftd-td-catalog-item-read-services/internal/structure/domain/ports/in"
	"github.com/gin-gonic/gin"
)

const (
	getItemSectionStructureLog  = "ItemSectionHandler.GetItemSectionStructure"
	internalErrorItemSectionMsg = "Ocurrió un error al intentar obtener la estructura de la sección del ítem para el país '%s'"
)

type ItemSectionHandler interface {
	GetItemSectionStructure(c *gin.Context)
}

type itemSectionHandler struct {
	service in.ItemSectionService
}

func NewItemSectionHandler(port in.ItemSectionService) ItemSectionHandler {
	return &itemSectionHandler{service: port}
}

func (h itemSectionHandler) GetItemSectionStructure(c *gin.Context) {
	var header request.GetStructureHeaders
	countryID := c.Param("countryId")
	itemID := c.Param("itemId")

	if err := c.ShouldBindHeader(&header); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	var correlationID = utils.GetCorrelationID(header.CorrelationID)
	c.Set(enums.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums.HeaderCorrelationID, correlationID)

	data, err := h.service.GetItemSectionStructure(c, countryID, itemID)

	if err != nil {
		utils.LogError(c, getItemSectionStructureLog, fmt.Sprintf(internalErrorLog, countryID, err))
		response.ServerError(c, fmt.Sprintf(internalErrorItemSectionMsg, countryID))
		return
	}

	response.Ok(c, &data)
}
