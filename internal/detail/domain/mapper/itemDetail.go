package mapper

import (
	"fmt"
	"ftd-td-catalog-item-read-services/internal/detail/domain/model"
	sharedModel "ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"github.com/jinzhu/copier"
	log "github.com/sirupsen/logrus"
)

func ItemDetailFromProductInformation(itemDetail *model.ItemDetail, productsInformation *sharedModel.ProductInformation) error {
	if itemDetail == nil || productsInformation == nil {
		return fmt.Errorf("flashOffers o productsInformation están vacíos")
	}

	err := copier.Copy(&itemDetail, &productsInformation)

	if err != nil {
		log.Printf("copier error: %v", err)
		return fmt.Errorf("error al copiar los datos del producto: %w", err)
	}

	return nil
}
