package com.imaginamos.farmatodo.model.home

import com.google.appengine.repackaged.com.google.gson.Gson
import com.imaginamos.farmatodo.model.dto.EnableForEnum

class HomeRequest {
    var idCustomerWebSafe: String? = null
    var token: String? = null
    var tokenIdWebSafe: String? = null
    var source: EnableForEnum? = null
    var carouselLimit: Int? = null
    var city: String? = null
    var idStoreGroup: Int? = null
    var version: String? = null
    var talonOneData: Map<String, Object>? = null

    fun isValid(): Boolean {
        return idCustomerWebSafe != null && token != null && tokenIdWebSafe != null &&  source != null
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}