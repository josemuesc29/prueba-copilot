package dig

import (
	"ftd-td-home-read-services/cmd/router"
	appCarousel "ftd-td-home-read-services/internal/carousel/app"
	carouselRepository "ftd-td-home-read-services/internal/carousel/infra/adapters/http/cms/repository"
	carouselGroup "ftd-td-home-read-services/internal/carousel/infra/api/groups"
	carouselhHandler "ftd-td-home-read-services/internal/carousel/infra/api/handler"
	healthGroup "ftd-td-home-read-services/internal/health/infra/api/groups"
	healthHanlder "ftd-td-home-read-services/internal/health/infra/api/handler"
	appOffer "ftd-td-home-read-services/internal/offer/app"
	offerRepository "ftd-td-home-read-services/internal/offer/infra/adapters/http/cms/repository"
	offerGroup "ftd-td-home-read-services/internal/offer/infra/api/groups"
	offerHandler "ftd-td-home-read-services/internal/offer/infra/api/handler"
	cacheClient "ftd-td-home-read-services/internal/shared/infra/adapters/cache/client"
	cacheRepository "ftd-td-home-read-services/internal/shared/infra/adapters/cache/repository"
	catalogProductRepository "ftd-td-home-read-services/internal/shared/infra/adapters/http/catalog_products/repository"
	appStructure "ftd-td-home-read-services/internal/structure/app"
	structureRepository "ftd-td-home-read-services/internal/structure/infra/adapters/http/cms/repository"
	crmRepository "ftd-td-home-read-services/internal/structure/infra/adapters/http/crm/repository"
	structureGroup "ftd-td-home-read-services/internal/structure/infra/api/groups"
	structureHandler "ftd-td-home-read-services/internal/structure/infra/api/handler"
	bannerRepository "ftd-td-home-read-services/internal/banner/infra/adapters/http/cms/repository"
	bannerGroup "ftd-td-home-read-services/internal/banner/infra/api/groups"
	bannerHandler "ftd-td-home-read-services/internal/banner/infra/api/handler"
	appBanner "ftd-td-home-read-services/internal/banner/app"
	"go.uber.org/dig"
)

func BuildContainer() *dig.Container {
	container := dig.New()

	providers := []interface{}{
		// Handlers
		healthHanlder.NewHealth,
		structureHandler.NewStructureHandler,
		offerHandler.NewOfferHandler,
		carouselhHandler.NewCarousel,
		bannerHandler.NewBannerHandler,

		// Groups
		healthGroup.NewHealthGroup,
		structureGroup.NewStructureGroup,
		offerGroup.NewOfferGroup,
		carouselGroup.NewCarousel,
		bannerGroup.NewBannerGroup,

		// Repository
		structureRepository.NewStructureRepository,
		crmRepository.NewCustomerRepository,
		offerRepository.NewOfferCmsRepository,
		carouselRepository.NewSuggested,
		catalogProductRepository.NewCatalogProduct,
		cacheRepository.NewCache,
		bannerRepository.NewBannerCmsRepository,

		// Services
		appStructure.NewStructureService,
		appOffer.NewOfferService,
		appCarousel.NewCarousel,
		appBanner.NewBannerService,

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
