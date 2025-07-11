package mappers

import (
	"fmt"
	"ftd-td-catalog-item-read-services/internal/same-brand/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"github.com/jinzhu/copier"
	log "github.com/sirupsen/logrus"
)

func MapProductInformationToSameBrandItem(productsInformation *sharedModel.ProductInformation) (model.SameBrandItem, error) {
	var sameBrandItem model.SameBrandItem
	if productsInformation == nil {
		return sameBrandItem, fmt.Errorf("productsInformation está vacío")
	}

	err := copier.Copy(&sameBrandItem, productsInformation)

	if err != nil {
		log.Printf("copier error: %v", err)
		return sameBrandItem, fmt.Errorf("error al copiar los datos del producto: %w", err)
	}

	// Restaurar lógica de cálculo para TotalStock
	if productsInformation.StoresWithStock != nil {
		sameBrandItem.TotalStock = len(productsInformation.StoresWithStock)
	} else {
		sameBrandItem.TotalStock = 0
	}

	return sameBrandItem, nil
}
