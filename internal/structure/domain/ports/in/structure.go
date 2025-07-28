package in

//go:generate mockgen -source=structure.go -destination=../../../../../test/mocks/structure/domain/ports/in/structure_mock.go

import (
	"context"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
)

type ItemStructureService interface {
	GetItemStructure(
		ctx context.Context,
		countryID string,
		itemID string,
	) ([]model.Component, error)
}
