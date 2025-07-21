package enum

const (
	BannerMain                      = "MAIN_BANNER"
	BannerSecondary                 = "SECONDARY_BANNER"
	BannerMiddle                    = "MIDDLE_BANNER"
	BannerBestSellerCategoriesLeft  = "BANNER_BEST_SELLERS_BY_CATEGORY_LEFT"
	BannerBestSellerCategoriesRight = "BANNER_BEST_SELLERS_BY_CATEGORY_RIGHT"
	BannerCarouselFirst             = "CAROUSEL_BANNER_FIRST"
	BannerCarouselSecond            = "CAROUSEL_BANNER_SECOND"
	BannerCollage                   = "COLLAGE_BANNER"
)

var ValidBannerNames = map[string]bool{
	BannerMain:                      true,
	BannerSecondary:                 true,
	BannerMiddle:                    true,
	BannerBestSellerCategoriesLeft:  true,
	BannerBestSellerCategoriesRight: true,
	BannerCarouselFirst:             true,
	BannerCarouselSecond:            true,
	BannerCollage:                   true,
}
