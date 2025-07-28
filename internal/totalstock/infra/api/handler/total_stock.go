package handler

import (
	"net/http"
	"strings"

	"github.com/fatihtelis/total-stock-app/internal/totalstock/domain/model"
	"github.com/fatihtelis/total-stock-app/internal/totalstock/domain/ports/in"
	"github.com/gin-gonic/gin"
)

type TotalStockHandler struct {
	useCase in.TotalStockUseCase
}

func NewTotalStockHandler(useCase in.TotalStockUseCase) *TotalStockHandler {
	return &TotalStockHandler{
		useCase: useCase,
	}
}

func (h *TotalStockHandler) GetTotalStock(c *gin.Context) {
	countryID := c.Param("countryId")
	itemID := c.Param("itemId")
	storeIDsStr := c.Query("storeIds")

	if storeIDsStr == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "storeIds is required"})
		return
	}
	storeIDs := strings.Split(storeIDsStr, ",")

	totalStock, err := h.useCase.GetTotalStock(c.Request.Context(), countryID, itemID, storeIDs)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, model.TotalStock{TotalStock: totalStock})
}
