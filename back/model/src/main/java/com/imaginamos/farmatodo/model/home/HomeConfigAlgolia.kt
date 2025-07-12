package com.imaginamos.farmatodo.model.home

import com.google.appengine.repackaged.com.google.gson.Gson

class HomeConfigAlgolia {
    var homeConfig: HomeConfig? = null

    fun isValid():Boolean {
        return this.homeConfig != null
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}
