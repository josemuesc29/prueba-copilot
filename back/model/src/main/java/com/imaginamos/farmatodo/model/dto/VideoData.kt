package com.imaginamos.farmatodo.model.dto

import com.google.appengine.repackaged.com.google.gson.Gson
import java.util.*

class VideoData {
    var id: Long? = null
    var title: String? = null
    var description: String? = null
    var thumbnail: String? = null
    var author: String? = null
    var url: String? = null
    var products: ArrayList<Long>? = null
    var position: Int? = null

    fun isValid(): Boolean {
        return id != null || title != null || description != null || thumbnail != null ||
                url != null || products != null || position != null || author != null
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

}