package handler

//go:generate mockgen -source=products_related.go -destination=../../../../../test/mocks/products-related/infra/api/handler/products_related_mock.go
//go:generate mockgen -destination=../../../../../test/mocks/shared/domain/cache_mock.go -package=domain ftd-td-catalog-item-read-services/internal/shared/domain/ports/out Cache
//go:generate mockgen -destination=../../../../../test/mocks/shared/domain/catalog_product_mock.go -package=domain ftd-td-catalog-item-read-services/internal/shared/domain/ports/out CatalogProduct
//go:generate mockgen -destination=../../../../../test/mocks/shared/domain/config_out_port_mock.go -package=domain ftd-td-catalog-item-read-services/internal/shared/domain/ports/out ConfigOutPort

import (
	"fmt"
	productsRelatedPortsIn "ftd-td-catalog-item-read-services/internal/products-related/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler/dto/request"
	"ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler/mappers"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	sharedResponse "ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response" // Renamed to avoid conflict
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"net/http"

	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
)

const (
	getRelatedItemsHandlerLog = "ProductsRelatedHandler.GetRelatedItems"
)

type productsRelatedHandler struct {
	portProductsRelated productsRelatedPortsIn.ProductsRelated
}

type ProductsRelatedHandler interface {
	GetRelatedItems(c *gin.Context)
}

func NewProductsRelatedHandler(port productsRelatedPortsIn.ProductsRelated) ProductsRelatedHandler {
	return &productsRelatedHandler{portProductsRelated: port}
}

func (h *productsRelatedHandler) GetRelatedItems(c *gin.Context) {
	correlationID := utils.GetCorrelationID(c.Request.Header.Get(string(enums.HeaderCorrelationID)))
	c.Set(string(enums.HeaderCorrelationID), correlationID)
	c.Writer.Header().Set(string(enums.HeaderCorrelationID), correlationID)

	log.Printf(enums.LogFormat, correlationID, getRelatedItemsHandlerLog, "Processing request")

	var reqDto request.ProductsRelatedRequestDto

	// Bind URI parameters (countryId, itemId)
	if err := c.ShouldBindUri(&reqDto); err != nil {
		log.Printf(enums.LogFormat, correlationID, getRelatedItemsHandlerLog, fmt.Sprintf("Error binding URI parameters: %v", err))
		sharedResponse.BadRequest(c, fmt.Sprintf("Invalid path parameters: %s", err.Error()))
		return
	}

	log.Printf(enums.LogFormat, correlationID, getRelatedItemsHandlerLog,
		fmt.Sprintf("Calling application service with DTO: %+v", reqDto))

	domainResponse, err := h.portProductsRelated.GetRelatedItems(
		c,
		reqDto.CountryID,
		reqDto.ItemID,
	)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, getRelatedItemsHandlerLog, fmt.Sprintf("Error from application service: %v", err))
		sharedResponse.ServerError(c, err.Error()) // Assuming generic server error
		return
	}

	responseDto := mappers.ModelProductsRelatedItemListToProductsRelatedItemDtoList(domainResponse)

	log.Printf(enums.LogFormat, correlationID, getRelatedItemsHandlerLog, "Successfully retrieved and mapped related items")
	c.JSON(http.StatusOK, responseDto)
}
