package handler

// TODO: Update mockgen source to products_related.go once port interface is updated
//go:generate mockgen -source=products_related.go -destination=../../../../../test/mocks/products-related/infra/api/handler/products_related_mock.go

import (
	"fmt"
	// Import for the new port (will be created in a later step)
	productsRelatedPortsIn "ftd-td-catalog-item-read-services/internal/products-related/domain/ports/in"
	"ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"
	"ftd-td-catalog-item-read-services/internal/shared/infra/api/handler/dto/response" // Reusing generic responses
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"net/http"

	"github.com/gin-gonic/gin"
	log "github.com/sirupsen/logrus"
)

const (
	getRelatedItemsHandlerLog = "ProductsRelatedHandler.GetRelatedItems"
	serviceProductsRelated    = "service ProductsRelated"
	countryIDParam            = "countryId"
	itemIDParam               = "itemId"
	queryParamNearbyStores    = "nearby-stores"
	queryParamCity            = "city"
	queryParamQuery           = "query"        // Query for Algolia
	queryParamIndexName       = "index-name"   // Optional index name for Algolia
	queryParamAlgoliaParams   = "params"       // Other Algolia specific parameters
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

	countryID := c.Param(countryIDParam)
	itemID := c.Param(itemIDParam)

	if countryID == "" {
		log.Printf(enums.LogFormat, correlationID, getRelatedItemsHandlerLog, "countryId is required")
		response.BadRequest(c, "countryId is required in path")
		return
	}
	if itemID == "" {
		log.Printf(enums.LogFormat, correlationID, getRelatedItemsHandlerLog, "itemId is required")
		response.BadRequest(c, "itemId is required in path")
		return
	}

	// Extract query parameters
	nearbyStores := c.Query(queryParamNearbyStores)
	city := c.Query(queryParamCity)
	queryAlgolia := c.Query(queryParamQuery)
	indexName := c.Query(queryParamIndexName)
	algoliaParams := c.Query(queryParamAlgoliaParams)

	// The 'query' parameter (queryAlgolia here) is crucial for Algolia search.
	// It might be optional if algoliaParams contains a full query definition or if related items are fetched differently.
	// For now, we'll assume it can be empty and the service layer will handle it.

	log.Printf(enums.LogFormat, correlationID, getRelatedItemsHandlerLog,
		fmt.Sprintf("Calling application service with countryID: %s, itemID: %s, nearbyStores: %s, city: %s, query: %s, indexName: %s, params: %s",
			countryID, itemID, nearbyStores, city, queryAlgolia, indexName, algoliaParams))

	data, err := h.portProductsRelated.GetRelatedItems(c, countryID, itemID, nearbyStores, city, queryAlgolia, indexName, algoliaParams)
	if err != nil {
		log.Printf(enums.LogFormat, correlationID, getRelatedItemsHandlerLog, fmt.Sprintf("Error from service: %v", err))
		// Consider different error types, e.g., NotFound vs ServerError
		response.ServerError(c, err.Error()) // Assuming generic server error for now
		return
	}

	log.Printf(enums.LogFormat, correlationID, getRelatedItemsHandlerLog, "Successfully retrieved related items from service")
	c.JSON(http.StatusOK, data) // Send the data from the service directly
}
