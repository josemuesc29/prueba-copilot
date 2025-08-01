package response

import "ftd-td-catalog-item-read-services/internal/structure/domain/model"

type Component struct {
	Label         string              `json:"label"`
	ComponentType model.ComponentType `json:"componentType"`
	ServiceUrl    *string             `json:"serviceUrl,omitempty"`
	RedirectUrl   *string             `json:"redirectUrl,omitempty"`
	Order         int                 `json:"order"`
}
