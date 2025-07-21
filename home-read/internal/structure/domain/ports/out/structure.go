package out

//go:generate mockgen -source=structure.go -destination=../../../../../test/mocks/structure/domain/ports/out/structure_mock.go

import (
	"context"
	"ftd-td-home-read-services/internal/structure/domain/model"
)

type StructureRepository interface {
	GetHomeStructure(ctx context.Context, countryID string, platform model.Platform) ([]model.Section, error)
}
