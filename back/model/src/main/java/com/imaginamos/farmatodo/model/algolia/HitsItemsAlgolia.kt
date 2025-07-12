package com.imaginamos.farmatodo.model.algolia

import com.google.appengine.repackaged.com.google.gson.Gson
import java.util.Collections.emptyList

class HitsItemsAlgolia {
    var itemAlgoliaList: List<ItemAlgolia>? = emptyList()
    var nbHits: Long? = 0
    var nbHitsPerPage: Long? = 0
    var nbPages: Long? = 0
    var categoryCode: String ? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
