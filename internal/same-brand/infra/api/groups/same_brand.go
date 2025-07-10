package groups

//go:generate mockgen -source=same_brand.go -destination=../../../../../test/mocks/same-brand/infra/api/groups/same_brand_mock.go

import (
	"ftd-td-catalog-item-read-services/internal/same-brand/infra/api/handler"
	"ftd-td-catalog-item-read-services/internal/shared/middleware"

	"github.com/gin-gonic/gin"
)

const sameBrandPath = "/same-brand"

type sameBrand struct {
	sameBrandH handler.SameBrand
}

type SameBrand interface {
	Source(rg *gin.RouterGroup)
}

func NewSameBrand(sameBrandH handler.SameBrand) SameBrand {
	return &sameBrand{
		sameBrandH: sameBrandH,
	}
}

func (g sameBrand) Source(rg *gin.RouterGroup) {
	// El basePath en cmd/router/router.go es "/catalog-item/r/:countryId/v1"
	// La ruta deseada es "/catalog-item/r/:countryId/v1/item/:itemId/same-brand"
	// Por lo tanto, la ruta específica para este grupo debe ser "/item/:itemId/same-brand"

	// No se necesita un subgrupo con sameBrandPath si la ruta completa se define aquí.
	// O, si se usa sameBrandPath, debe ser parte de la construcción de la ruta final.

	// Opción 1: Definir la ruta completa relativa al basePath del router principal
	// group := rg.Group("") // No crear un nuevo subpath base como "/same-brand"
	// group.GET("/item/:itemId/same-brand", g.sameBrandH.GetItemsSameBrand, middleware.ValidateCountryID())

	// Opción 2: Usar un subpath y luego la ruta específica.
	// Si sameBrandPath = "/item" (o similar)
	// group := rg.Group("/item")
	// group.GET("/:itemId/same-brand", g.sameBrandH.GetItemsSameBrand, middleware.ValidateCountryID())

	// La constante sameBrandPath actual es "/same-brand".
	// Si rg ya es "/catalog-item/r/:countryId/v1", entonces necesitamos agregar "/item/:itemId/same-brand"
	// Esto significa que el rg.Group() no debería usar sameBrandPath como está.

	// Vamos a definir la ruta directamente desde el router group (rg) que ya tiene el basePath.
	// La ruta para el handler será "/item/:itemId/same-brand"
	// El middleware.ValidateCountryID() es importante si countryId no es validado a nivel superior.
	// Dado que :countryId está en el basePath, idealmente se valida allí o se pasa el validador al grupo principal.
	// Por ahora, lo mantenemos como está en el código original, asumiendo que el middleware puede extraerlo.
	rg.GET("/item/:itemId/same-brand", g.sameBrandH.GetItemsSameBrand, middleware.ValidateCountryID())
}
