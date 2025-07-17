package mappers

import (
	"ftd-td-catalog-item-read-services/internal/products-related/domain/model"
	"ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler/dto/response"
	"github.com/jinzhu/copier"
	log "github.com/sirupsen/logrus"
)

func ModelProductsRelatedItemListToProductsRelatedItemDtoList(items []model.ProductsRelatedItem) []response.ProductsRelatedResponse {
	var dtoList []response.ProductsRelatedResponse

	err := copier.Copy(&dtoList, &items)
	if err != nil {

		log.Println("Error copying items:", err)
		return []response.ProductsRelatedResponse{}
	}

	return dtoList
}
