package out

import (
	"context"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
)

type ItemStructureRepository interface {
	GetItemStructure(ctx context.Context, countryID string) ([]model.Component, error)
}
