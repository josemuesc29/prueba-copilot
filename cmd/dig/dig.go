package dig

import (
	"ftd-td-catalog-item-read-services/cmd/router"
	bestSellerService "ftd-td-catalog-item-read-services/internal/best-seller/app"
	bestSellerRepository "ftd-td-catalog-item-read-services/internal/best-seller/infra/adapters/database/repository"
	bestSellerGroup "ftd-td-catalog-item-read-services/internal/best-seller/infra/api/groups"
	bestSellerHanlder "ftd-td-catalog-item-read-services/internal/best-seller/infra/api/handler"
	healthGroup "ftd-td-catalog-item-read-services/internal/health/infra/api/groups"
	healthHanlder "ftd-td-catalog-item-read-services/internal/health/infra/api/handler"
	// Products Related (replaces SameBrand)
	productsRelatedService "ftd-td-catalog-item-read-services/internal/products-related/app"
	productsRelatedGroup "ftd-td-catalog-item-read-services/internal/products-related/infra/api/groups"
	productsRelatedHandler "ftd-td-catalog-item-read-services/internal/products-related/infra/api/handler"

	cacheClient "ftd-td-catalog-item-read-services/internal/shared/infra/adapters/cache/client"
	sharedCacheRepository "ftd-td-catalog-item-read-services/internal/shared/infra/adapters/cache/repository"
	configDatabase "ftd-td-catalog-item-read-services/internal/shared/infra/adapters/database/config"
	sharedCatalogCategoryRepository "ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/catalog_category/repository"
	sharedCatalogProductsRepository "ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/catalog_products/repository"
	sharedConfigRepository "ftd-td-catalog-item-read-services/internal/shared/infra/adapters/http/config/repository"
	"go.uber.org/dig"
)

func BuildContainer() *dig.Container {
	container := dig.New()

	providers := []interface{}{
		// Handlers
		healthHanlder.NewHealth,
		bestSellerHanlder.NewBestSeller,
		productsRelatedHandler.NewProductsRelatedHandler, // Replaced NewSameBrand

		// Groups
		healthGroup.NewHealthGroup,
		bestSellerGroup.NewBestSeller,
		productsRelatedGroup.NewProductsRelatedGroup, // Replaced NewSameBrand

		// Database
		configDatabase.NewPostgresConnection,

		// Repository
		bestSellerRepository.NewBestSellerRepository,
		sharedConfigRepository.NewConfig,
		sharedCatalogCategoryRepository.NewCatalogCategory,
		sharedCatalogProductsRepository.NewCatalogProduct,
		sharedCacheRepository.NewCache,

		// Services
		bestSellerService.NewBestSeller,
		productsRelatedService.NewProductsRelated, // Replaced NewSameBrand

		// Router
		router.NewRouter,

		// Redis
		cacheClient.NewRedisClient,
	}

	for _, p := range providers {
		if err := container.Provide(p); err != nil {
			panic("failed to provide dependency: " + err.Error())
		}
	}

	return container
}
