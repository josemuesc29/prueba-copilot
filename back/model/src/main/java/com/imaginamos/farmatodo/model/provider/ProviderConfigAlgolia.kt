package com.imaginamos.farmatodo.model.provider

import com.google.appengine.repackaged.com.google.gson.Gson

class ProviderConfigAlgolia {
    var providerConfig: ProviderConfig? = null

    fun isValid():Boolean {
        return this.providerConfig != null
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}