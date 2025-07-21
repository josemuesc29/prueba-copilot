package in

//go:generate mockgen -source=structure.go -destination=../../../../../test/mocks/structure/domain/ports/in/structure_mock.go

import (
	"context"
	"ftd-td-home-read-services/internal/structure/domain/model"
)

type StructureService interface {
	GetStructure(
		ctx context.Context,
		countryID string,
		platform model.Platform,
		customer *model.Customer,
	) ([]model.Section, error)
}
