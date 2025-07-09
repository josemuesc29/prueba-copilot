package mappers

import (
	"ftd-td-catalog-item-read-services/internal/best-seller/domain/model"
	"ftd-td-catalog-item-read-services/internal/best-seller/infra/api/handler/dto/response"
	"github.com/jinzhu/copier"
)

func ModelBestSellerDepartmentListToBestSellerDepartmentDtoList(suggestedModel []model.BestSellerDepartment) []response.BestSellerDepartment {
	var rs []response.BestSellerDepartment

	for _, sug := range suggestedModel {
		var rsSuggest response.BestSellerDepartment

		err := copier.Copy(&rsSuggest, &sug)
		if err != nil {
			rsSuggest = response.BestSellerDepartment{}
		}

		rs = append(rs, rsSuggest)
	}

	return rs
}
