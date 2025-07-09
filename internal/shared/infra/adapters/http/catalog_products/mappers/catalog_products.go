package mappers

import (
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/catalog_products/model/response"
	"ftd-td-catalog-item-read-services/internal/shared/utils"
	"github.com/jinzhu/copier"
)

func CatalogProductInformationToCarouselProductInformation(cpInformation *response.CatalogProductInformation) model.ProductInformation {
	var rs model.ProductInformation

	if cpInformation == nil {
		return model.ProductInformation{}
	}

	cpInformation.Spaces = int(utils.AnyToInt64(cpInformation.Spaces))

	cpInformation.Sales = utils.AnyToInt64(cpInformation.Sales)
	cpInformation.Highlight = utils.AnyToBool(cpInformation.Highlight)
	cpInformation.Generics = utils.AnyToBool(cpInformation.Generics)
	cpInformation.RequirePrescription = utils.AnyToBool(cpInformation.RequirePrescription)

	err := copier.Copy(&rs, &cpInformation)

	if err != nil {
		return model.ProductInformation{}
	}

	return rs
}

func CatalogProductsInformationToCarouselProductInformationList(cpsInformations []response.CatalogProductInformation) []model.ProductInformation {
	var rs []model.ProductInformation

	for _, cpInformation := range cpsInformations {
		rs = append(rs, CatalogProductInformationToCarouselProductInformation(&cpInformation))
	}

	return rs
}
