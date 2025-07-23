package in

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
