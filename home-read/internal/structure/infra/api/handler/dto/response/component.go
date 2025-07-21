package response

type Component struct {
	Type        string  `json:"componentType"`
	Label       *string `json:"label"`
	LabelColor  *string `json:"labelColor"`
	RedirectUrl *string `json:"redirectUrl"`
	ServiceUrl  *string `json:"serviceUrl"`
	Position    int     `json:"position"`
}
