package model

type ItemSection struct {
	Label         string        `json:"label"`
	ComponentType ComponentType `json:"componentType"`
	ServiceUrl    string        `json:"serviceUrl"`
	RedirectUrl   string        `json:"redirectUrl"`
}
