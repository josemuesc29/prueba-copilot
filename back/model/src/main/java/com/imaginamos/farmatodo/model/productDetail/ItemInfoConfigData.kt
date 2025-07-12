package com.imaginamos.farmatodo.model.productDetail

import com.google.appengine.repackaged.com.google.gson.Gson
import com.imaginamos.farmatodo.model.customer.CustomerOnlyData
import com.imaginamos.farmatodo.model.dto.Component
import com.imaginamos.farmatodo.model.dto.EnableForEnum
import com.imaginamos.farmatodo.model.product.Item

class ItemInfoConfigData {
    var source: EnableForEnum? = null
    var itemData: Item? = null
    var nearbyStores: List<Long>? = null
    var customerOnlyData: CustomerOnlyData? = null
    var itemConfigAlgolia: ItemConfigAlgolia? = null
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