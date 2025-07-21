package request

import "ftd-td-home-read-services/internal/structure/domain/model"

type GetStructureHeaders struct {
	Platform      model.Platform `header:"source" binding:"required,oneof=ANDROID IOS RESPONSIVE WEB"`
	CorrelationID string         `header:"X-Correlation-ID"`
}

type GetStructureRequest struct {
	Customer *model.Customer `json:"customer"`
}
