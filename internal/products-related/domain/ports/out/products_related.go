package out

//go:generate mockgen -source=same_brand.go -destination=../../../../../test/mocks/same-brand/domain/ports/out/same_brand_mock.go

import (
	"context"
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
)

type SameBrandRepository interface {
	GetItemBrand(ctx context.Context, itemID string) (string, error)
	GetItemsBySameBrand(ctx context.Context, countryID, brand, city string) ([]model.SameBrandItem, error)
}
