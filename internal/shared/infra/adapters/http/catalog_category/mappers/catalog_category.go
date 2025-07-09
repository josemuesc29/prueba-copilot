package mappers

import (
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/catalog_category/model/response"
	"github.com/jinzhu/copier"
)

func MapCatalogCategoryDtoToCatalogCategory(catalogCategory response.CatalogCategory) model.CatalogCategory {
	var rs model.CatalogCategory

	err := copier.Copy(&rs, &catalogCategory.Data)
	if err != nil {
		return model.CatalogCategory{}
	}
	return rs
}
