package out

//go:generate mockgen -source=structure.go -destination=../../../../../test/mocks/structure/domain/ports/out/structure_mock.go

import (
	"context"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
)

type ItemStructureRepository interface {
	GetItemStructure(ctx context.Context, countryID string) ([]model.Component, error)
}
