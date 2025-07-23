package handler

import (
	"fmt"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"ftd-td-catalog-item-read-services/internal/structure/domain/ports/in"
	resdto "ftd-td-catalog-item-read-services/internal/structure/infra/api/handler/dto/response"

	"github.com/gin-gonic/gin"
	"github.com/jinzhu/copier"
)

const (
	getStructureLog  = "ItemStructureHandler.GetStructure"
	internalErrorLog = "Error getting item structure for country '%s': %v"
	internalErrorMsg = "Ocurrió un error al intentar obtener la estructura del item para el país '%s'"
)

type handler struct {
	service in.ItemStructureService
}

type ItemStructureHandler interface {
	GetStructure(c *gin.Context)
}

func NewItemStructureHandler(port in.ItemStructureService) ItemStructureHandler {
	return &handler{service: port}
}

func (h handler) GetStructure(c *gin.Context) {
	var result []resdto.Component

	countryID := c.Param("countryId")
	itemID := c.Param("itemId")
	correlationID := c.GetHeader(enums.HeaderCorrelationID)
	c.Set(enums.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums.HeaderCorrelationID, correlationID)

	data, err := h.service.GetItemStructure(c, countryID, itemID)

	if err != nil {
		utils.LogError(c, getStructureLog, fmt.Sprintf(internalErrorLog, countryID, err))
		response.ServerError(c, fmt.Sprintf(internalErrorMsg, countryID))
		return
	}

	err = copier.Copy(&result, data)
	if err != nil {
		response.ServerError(c, fmt.Sprintf(internalErrorMsg, countryID))
		return
	}

	response.Ok(c, &result)
}
