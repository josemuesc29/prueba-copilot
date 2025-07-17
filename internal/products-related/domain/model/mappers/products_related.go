package mappers

import (
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"github.com/jinzhu/copier"
	log "github.com/sirupsen/logrus"
)

func MapProductInformationToProductsRelatedItem(productsRelated *model.ProductsRelatedItem, productsInformation *sharedModel.ProductInformation) model.ProductsRelatedItem {
	if productsInformation == nil {
		log.Println("productsInformation está vacío")
		return model.ProductsRelatedItem{}
	}

	var newProductRelated model.ProductsRelatedItem
	err := copier.Copy(&newProductRelated, productsInformation)
	if err != nil {
		log.Printf("copier error: %v", err)
		return model.ProductsRelatedItem{}
	}

	newProductRelated.TotalStock = len(productsInformation.StoresWithStock)

	return newProductRelated
}
