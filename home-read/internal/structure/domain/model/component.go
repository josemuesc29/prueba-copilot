package model

type Component struct {
	Type        ComponentType `json:"componentType"`
	EnableFor   []Platform    `json:"enableFor"`
	RedirectUrl *string       `json:"redirectUrl"`
	ServiceUrl  *string       `json:"serviceUrl"`
	Label       *string       `json:"label"`
	LabelColor  *string       `json:"labelColor"`
	Active      bool          `json:"active"`
	VisibleFor  []UserType    `json:"visibleFor"`
	Position    int           `json:"position"`
}

type ComponentType string

const (
	ComponentMainBanner                       ComponentType = "MAIN_BANNER"
	ComponentLogin                            ComponentType = "LOGIN"
	ComponentTrends                           ComponentType = "TRENDS"
	ComponentSuggests                         ComponentType = "SUGGESTS"
	ComponentSecondaryBanner                  ComponentType = "SECONDARY_BANNER"
	ComponentFlashOffers                      ComponentType = "FLASH_OFFERS"
	ComponentLastPurchases                    ComponentType = "LAST_PURCHASES"
	ComponentFavorites                        ComponentType = "FAVORITES"
	ComponentMiddleBanner                     ComponentType = "MIDDLE_BANNER"
	ComponentRecentlyViewed                   ComponentType = "RECENTLY_VIEWED"
	ComponentSocialMediaVideos                ComponentType = "SOCIAL_MEDIA_VIDEOS"
	ComponentGroupCategories                  ComponentType = "GROUP_CATEGORIES"
	ComponentBannerBestSellersByCategoryLeft  ComponentType = "BANNER_BEST_SELLERS_BY_CATEGORY_LEFT"
	ComponentCollageBanner                    ComponentType = "COLLAGE_BANNER"
	ComponentCarouselBanner                   ComponentType = "CAROUSEL_BANNER"
	ComponentBestSellersByCategory            ComponentType = "BEST_SELLERS_BY_CATEGORY"
	ComponentBannerBestSellersByCategoryRight ComponentType = "BANNER_BEST_SELLERS_BY_CATEGORY_RIGHT"
	ComponentStores                           ComponentType = "STORES"
	// Item Section Components
	MainItem        ComponentType = "MAIN_ITEM"
	ItemSEO         ComponentType = "ITEM_SEO"
	ProductRelated  ComponentType = "PRODUCT-RELATED"
	Bazaarvoice     ComponentType = "BAZAARVOICE"
	SameBrand       ComponentType = "SAME-BRAND"
)

type UserType string

const (
	UserTypeLoggedIn  UserType = "LOGGED_IN"
	UserTypeAnonymous UserType = "ANONYMOUS"
)
