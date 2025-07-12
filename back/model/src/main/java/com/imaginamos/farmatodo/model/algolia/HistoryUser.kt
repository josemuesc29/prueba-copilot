package com.imaginamos.farmatodo.model.algolia

import com.google.appengine.repackaged.com.google.gson.Gson

class HistoryUser {
    var objectID: String? = null
    var items: List<Long>? = null

    override fun toString(): String {
        return Gson().toJson(this)
    }
}