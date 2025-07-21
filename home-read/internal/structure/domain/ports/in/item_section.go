package in

import (
	"context"
	"ftd-td-home-read-services/internal/structure/domain/model"
)

type ItemSectionService interface {
	GetItemSectionStructure(
		ctx context.Context,
		countryID string,
		itemID string,
	) ([]model.ItemSection, error)
}
