package com.imaginamos.farmatodo.model.item

import com.google.appengine.repackaged.com.google.gson.Gson


class TtlCacheAlgoliaRecommendRes {
    var algoliaRecommendTtlSeconds: Int? = null
    var getItemTtlSeconds: Int? = null
    var algoliaRecommendEnabled: Boolean? = null
    var advisedItems: Boolean = false
    var optimalRoute: Boolean = false
    var departmentsCarrousel: Boolean = false
    var suggestedItemsFlag: Boolean = false
    var departmentsAfinity: Boolean = false

    override fun toString(): String {
        return Gson().toJson(this)
    }
}