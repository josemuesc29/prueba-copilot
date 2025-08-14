package request

import model "ftd-td-catalog-item-read-services/internal/shared/domain/model/enums"

type SeoHeaders struct {
	Source        model.Source `header:"source" binding:"required,oneof=ANDROID IOS RESPONSIVE WEB"`
	CorrelationID string       `header:"X-Correlation-Id"`
}
