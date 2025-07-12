package com.imaginamos.farmatodo.model.home
import java.util.*

class BannersDTFReq {
    var categories: List<Long> = ArrayList()
}


class BannersDTFRes {
    var code: String? = null
    var message: String? = null
//    var data: List<BannerDTFData>? = ArrayList() // old banners
    var data: BannerDataCMSType? = null

    fun isValid(): Boolean {
        return code != null && message != null && data != null
    }
}

class BannerDataCMSType {
    var desktop: Desktop? = null;
    var mobile: Mobile? = null;
}

class Mobile {
    var mainBanner: List<BannerCMSData>? = null;
    var newServices: List<BannerCMSData>? = null;
    var advertising: List<BannerCMSData>? = null;
}

class Desktop {
    var mainBanner: List<BannerCMSData>? = null;
    var leftAdvertising: List<BannerCMSData>? = null;
    var rightAdvertising: List<BannerCMSData>? = null;
    var staticBanner: List<BannerCMSData>? = null;
    var BannerCMSData: List<BannerCMSData>? = null;
}

class BannerCMSData {
    var idBanner: Long? = null
    var urlBanner: String? = null
    var redirectUrl: String? = null
    var order: Int? = null
    var directionBanner: Boolean? = null
    var bannerWeb: Boolean? = null
    var campaignName: String? = null
    var creative: String? = null
    var position: Position? = null
    var listClusteres: List<Long>? = null
    var category: Long? = null
    var home: Boolean? = null
}
enum class Position {
    Advertising,
    AdvertisingCategory,
    AdvertisingLeft,
    AdvertisingRight,
    MainBanner
}
class BannerDTFData {
    var id: Long? = null
    var idCategory: Long? = null
    var imageDesktop: String? = null
    var imageMobile: String? = null
    var redirectURL: String? = null
    var campaignName: String? = null
    var dateTimePublished: String? = null
    var dateTimeFinished: String? = null
    var position: Int? = null
    var typeImage: BannerTypeEnum? = null
}