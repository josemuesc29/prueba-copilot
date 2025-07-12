package com.imaginamos.farmatodo.model.marketPlace

class MarketPlaceResGrowthBook {
    var WEB: Boolean = false
    var IOS: Boolean = false
    var ANDROID: Boolean = false
    var RESPONSIVE: Boolean = false

    operator fun get(source: String): Boolean? {
        return when(source.uppercase()) {
            "WEB" -> this.WEB
            "IOS" -> this.IOS
            "ANDROID" -> this.ANDROID
            "RESPONSIVE" -> this.RESPONSIVE
            else -> null
        }
    }
}