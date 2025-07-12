package com.imaginamos.farmatodo.model.home

enum class BannerTypeEnum

(var bannerType: String?) {
    ADVERTISING_LEFT("ADVERTISING_LEFT"),
    STATIC_BANNER("STATIC_BANNER"),
    ADVERTISING_RIGHT("ADVERTISING_RIGHT"),
    ADVERTISING("ADVERTISING"),
    MAIN_BANNER("MAIN_BANNER"),
    NEW_SERVICES("NEW_SERVICES"),
    MAIN_BANNER2("MAIN_BANNER2"),
    ADVERTISING_CATEGORY("ADVERTISING_CATEGORY");

    override fun toString(): String {
        return "BannerTypeEnum(bannerType=$bannerType)"
    }

}