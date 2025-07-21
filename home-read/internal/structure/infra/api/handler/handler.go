package handler

//go:generate mockgen -source=handler.go -destination=../../../../../test/mocks/structure/infra/api/handler/structure_mock.go

import (
	"fmt"
	"ftd-td-home-read-services/internal/shared/domain/model/enums"
	"ftd-td-home-read-services/internal/shared/infra/api/handler/dto/response"
	"ftd-td-home-read-services/internal/shared/utils"
	"ftd-td-home-read-services/internal/structure/domain/ports/in"
	reqdto "ftd-td-home-read-services/internal/structure/infra/api/handler/dto/request"
	resdto "ftd-td-home-read-services/internal/structure/infra/api/handler/dto/response"

	"github.com/gin-gonic/gin"
	"github.com/jinzhu/copier"
)

const (
	getStructureLog  = "StructureHandler.GetStructure"
	internalErrorLog = "Error getting home structure for country '%s': %v"
	internalErrorMsg = "Ocurrió un error al intentar obtener la estructura del home para el país '%s'"
)

type handler struct {
	service in.StructureService
}

type StructureHandler interface {
	GetStructure(c *gin.Context)
}

func NewStructureHandler(port in.StructureService) StructureHandler {
	return &handler{service: port}
}

func (h handler) GetStructure(c *gin.Context) {
	var header reqdto.GetStructureHeaders
	var request reqdto.GetStructureRequest
	var result []resdto.Section

	countryID := c.Param("countryId")

	if err := c.ShouldBindHeader(&header); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	if err := c.ShouldBindJSON(&request); err != nil {
		response.BadRequest(c, err.Error())
		return
	}

	var correlationID = utils.GetCorrelationID(header.CorrelationID)
	c.Set(enums.HeaderCorrelationID, correlationID)
	c.Writer.Header().Set(enums.HeaderCorrelationID, correlationID)

	data, err := h.service.GetStructure(c, countryID, header.Platform, request.Customer)

	if err != nil {
		utils.LogError(c, getStructureLog, fmt.Sprintf(internalErrorLog, countryID, err))
		response.ServerError(c, fmt.Sprintf(internalErrorMsg, countryID))
		return
	}

	err = copier.Copy(&result, data)
	if err != nil {
		return
	}

	response.Ok(c, &result)
}
