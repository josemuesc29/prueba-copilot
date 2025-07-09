package out

//go:generate mockgen -source=catalog_category.go -destination=../../../../../test/mocks/shared/domain/ports/out/catalog_category_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/shared/domain/model"
	"github.com/gin-gonic/gin"
)

type CatalogCategoryOutPort interface {
	GetCategoryByDepartment(ctx *gin.Context, countryID, departmentID string) (model.CatalogCategory, error)
}
