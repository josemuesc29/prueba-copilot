package com.imaginamos.farmatodo.model.dto

import com.google.appengine.repackaged.com.google.gson.Gson
import com.imaginamos.farmatodo.model.home.BannerDataCMSType
import com.imaginamos.farmatodo.model.home.BannerTypeEnum
import com.imaginamos.farmatodo.model.photoSlurp.PhotoSlurp
import com.imaginamos.farmatodo.model.product.Item
import java.util.*

class DynamicResponse {
    var toolbarBackground: String? = null
    var toolbarIcon: String? = null
    var homeSections: List<DynamicSection>? = ArrayList()
    var itemSection: List<DynamicSection>? = ArrayList()
    var filters: Any? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }

}

  class DynamicSection : PhotoSlurp(){
    var componentType: ComponentTypeEnum? = null
    var list: List<Element>? = ArrayList()
    var label: String? = null
    var labelWeb: String? = null
    var html: String? = null
    var maxLines: Long? = null
    var redirectURL: String? = null
    var urlBanner: String? = null
    var idOrder: Long? = null
    var bannersDesktop: BannerDataCMSType? = null
    var isPrime: Boolean? = null
    var total_saved: Double? = null
    var userType: String? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }

}

enum class ActionEnum( val desc: String) {
    HIGHLIGHTED("HIGHLIGHTED"),
    SUGGESTED ("SUGGESTED"),
    FAVORITE("FAVORITE"),
    OFFER ("OFFER"),
    DETAIL("DETAIL"),
    RECENTLY_VIEWED("RECENTLY_VIEWED")
}

enum class ProductTypeEnum( val desc: String) {
    GROUP("GROUP"),
    UNIQUE ("GROUP")
}

class BannerData {
    var categoryId: Long? = null
    var bannerList: List<BannerInfo>? = ArrayList()
}

class BannerInfo {
    var bannerType: BannerTypeEnum? = null
    var data: List<Element>? = null
}

class Element {
    var urlBanner: String? = null
    var redirectURL: String? = null
    var redirectUrlSub: String? = null
    var label: String? = null
    var id: String? = null
    var firstDescription: String? = null
    var primeTextDiscount: String? = null
    var primeDescription: String? = null
    var description: String? = null
    var offerText: String? = null
//    var type: String? = null
    var type: ProductTypeEnum? = null
    var urlImage: String? = null
    var url: String? = null
    var startDate: Long? = null
    var endDate: Long? = null
    var orderingNumber: Int? = null
    var item: String? = null
    var product: List<Item>? = null
    var action: ActionEnum? = null
    var tutorial : String? = null
    var title: String? = null
    var thumbnail: String? = null
    var products: List<Long>? = null
    var position: Int? = null
    var author: String? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }

}
