package out

import "ftd-td-catalog-item-read-services/internal/shared/domain/model"

//go:generate mockgen -source=item_seo.go -destination=../../../../../test/mocks/shared/domain/ports/out/item_seo_mock.go

type ItemSeo interface {
	GetItemSeo(itemID int64, countryID string) (*model.ItemSeo, error)
}
