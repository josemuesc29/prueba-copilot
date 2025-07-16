package mappers

import (
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"github.com/jinzhu/copier"
	log "github.com/sirupsen/logrus"
)

func MapProductInformationToSameBrandItem(sameBrand *model.SameBrandItem, productsInformation *sharedModel.ProductInformation) model.SameBrandItem {
	if productsInformation == nil {
		log.Println("productsInformation está vacío")
		return model.SameBrandItem{}
	}

	var newSameBrand model.SameBrandItem
	err := copier.Copy(&newSameBrand, productsInformation)
	if err != nil {
		log.Printf("copier error: %v", err)
		return model.SameBrandItem{}
	}

	newSameBrand.TotalStock = len(productsInformation.StoresWithStock)

	return newSameBrand
}
