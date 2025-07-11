package mapper

import (
	"ftd-td-catalog-item-read-services/internal/detail/domain/model"
	"ftd-td-catalog-item-read-services/internal/detail/infra/api/handler/dto/response"
	"github.com/jinzhu/copier"
)

func GetItemDetailResponseFromItemDetail(itemDetail model.ItemDetail) response.ItemDetailResponse {
	var responseSr response.ItemDetailResponse

	err := copier.Copy(&responseSr, &itemDetail)
	if err != nil {
		responseSr = response.ItemDetailResponse{}
	}
	return responseSr
}
