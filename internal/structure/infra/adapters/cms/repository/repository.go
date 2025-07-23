package repository

import (
	"context"
	"ftd-td-catalog-item-read-services/internal/structure/domain/model"
	"ftd-td-catalog-item-read-services/internal/structure/domain/ports/out"
)

type itemStructureRepository struct{}

func NewItemStructureRepository() out.ItemStructureRepository {
	return &itemStructureRepository{}
}

func stringPointer(s string) *string {
	return &s
}

func (r *itemStructureRepository) GetItemStructure(ctx context.Context, countryID string) ([]model.Component, error) {
	// In a real scenario, this would fetch from a CMS.
	// For this implementation, we return a hardcoded structure.
	structure := []model.Component{
		{
			Label:         "Item Principal",
			ComponentType: model.MainItemComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/{countryId}/v1/items/{itemId}"),
			Position:      1,
			Active:        true,
		},
		{
			Label:         "SEO",
			ComponentType: model.ItemSeoComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/{countryId}/v1/items/{itemId}/seo"),
			Position:      2,
			Active:        true,
		},
		{
			Label:         "Productos Relacionados",
			ComponentType: model.ProductRelatedComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/{countryId}/v1/items/{itemId}/related"),
			Position:      3,
			Active:        true,
		},
		{
			Label:         "Reseñas",
			ComponentType: model.BazaarvoiceComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/{countryId}/v1/items/{itemId}/reviews"),
			Position:      4,
			Active:        true,
		},
		{
			Label:         "Misma Marca",
			ComponentType: model.SameBrandComponentType,
			ServiceUrl:    stringPointer("/catalog-item/r/{countryId}/v1/items/{itemId}/same-brand"),
			Position:      5,
			Active:        true,
		},
	}

	return structure, nil
}
