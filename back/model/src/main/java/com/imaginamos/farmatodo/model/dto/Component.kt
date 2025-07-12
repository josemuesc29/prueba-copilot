package com.imaginamos.farmatodo.model.dto

import com.google.appengine.repackaged.com.google.gson.Gson
import com.imaginamos.farmatodo.model.home.DataFromEnum
import com.imaginamos.farmatodo.model.provider.ElementProvider

class Component {

    var componentType: ComponentTypeEnum? = null
    var enableFor: List<EnableForEnum>? = null
    var dataFrom: DataFrom? = null
    var label: String? = null
    var labelWeb: String? = null
    var position: Long = 0
    var maxLines: Long? = null
    var urlBanner: String? = null
    var urlTracking: String? = null
    var redirectUrl: String? = null
    var redirectUrlSub: String? = null
    var active: Boolean? = false
    var userType: String? = null
    var list: List<ElementProvider>? = ArrayList()
    var listMobile: List<ElementProvider>? = ArrayList()
    var html: String? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }


}

class DataFrom {
    var id: String? = null
    var from: DataFromEnum? = null
    var listData: List<Component>? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }
}