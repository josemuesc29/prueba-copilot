package com.imaginamos.farmatodo.model.productDetail

import com.google.appengine.repackaged.com.google.gson.Gson

class ItemConfigAlgolia {
    var itemConfig: ItemConfig? = null
    var objectID: String? = null

    fun isValid():Boolean {
        return this.itemConfig != null
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }
}