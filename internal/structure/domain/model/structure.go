package model

type Component struct {
	ID            int           `json:"id"`
	Label         string        `json:"label"`
	ComponentType ComponentType `json:"componentType"`
	ServiceUrl    *string       `json:"serviceUrl,omitempty"`
	RedirectUrl   *string       `json:"redirectUrl,omitempty"`
	Order         int           `json:"order"`
}

type ComponentType string

const (
	MainItemComponentType       ComponentType = "MAIN_ITEM"
	ItemSeoComponentType        ComponentType = "ITEM_SEO"
	ProductRelatedComponentType ComponentType = "PRODUCT-RELATED"
	BazaarvoiceComponentType    ComponentType = "BAZAARVOICE"
	SameBrandComponentType      ComponentType = "SAME-BRAND"
)
