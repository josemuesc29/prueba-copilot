package out

import (
	"context"
	"ftd-td-home-read-services/internal/structure/domain/model"
)

type ItemSectionRepository interface {
	GetItemSectionStructure(
		ctx context.Context,
		countryID string,
	) ([]model.ItemSection, error)
}
