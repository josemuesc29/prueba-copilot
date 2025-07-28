package response

import "ftd-td-catalog-item-read-services/internal/structure/domain/model"

type ItemStructureResponse struct {
	Code    string      `json:"code"`
	Message string      `json:"message"`
	Data    []Component `json:"data"`
}

type Component struct {
	Label         string              `json:"label"`
	ComponentType model.ComponentType `json:"componentType"`
	ServiceUrl    *string             `json:"serviceUrl,omitempty"`
	RedirectUrl   *string             `json:"redirectUrl,omitempty"`
	Order         int                 `json:"order"`
}
