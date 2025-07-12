package com.imaginamos.farmatodo.model.home

import com.google.appengine.repackaged.com.google.gson.Gson
import com.imaginamos.farmatodo.model.dto.Component


class HomeConfig {
    var headerComponents: List<Component>? = null
    var bodyComponents: List<Component>? = null
    var footerComponents: List<Component>? = null


    fun isValid(): Boolean {
        return headerComponents != null || bodyComponents != null || footerComponents != null
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }


}
