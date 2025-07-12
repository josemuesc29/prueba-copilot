package com.imaginamos.farmatodo.model.provider

import com.google.appengine.repackaged.com.google.gson.Gson
import com.imaginamos.farmatodo.model.dto.Component
import com.imaginamos.farmatodo.model.dto.ComponentTypeEnum

class ProviderResponse {
    var toolbarBackground: String? = null
    var toolbarIcon: String? = null
    var providerConfig: ProviderConfigData? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }

}

class ProviderConfigData {
    var headerComponents: List<Component>? = null
    var bodyComponents: List<Component>? = null
    var footerComponents: List<Component>? = null
}

class ProviderSections {
    var componentType: ComponentTypeEnum? = null
    //    var action: ActionEnum? = null
    var list: List<ElementProvider>? = ArrayList()
    var listMobile: List<ElementProvider>? = ArrayList()
    var label: String? = null
    var redirectURL: String? = null
    var urlBanner: String? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }

}

class ElementProvider {
    var urlBanner: String? = null
    var redirectUrl: String? = null
    var label: String? = null
    var id: String? = null
    var firstDescription: String? = null
    var urlImage: String? = null
    var url: String? = null
    var startDate: Long? = null
    var endDate: Long? = null
    var orderingNumber: Int? = null
    var item: String? = null
    var tutorial : String? = null
    var title: String? = null
    var description: String? = null
    var thumbnail: String? = null
    var position: Long? = null
    var products: List<Long>? = null
    var author: String? = null

    override fun toString(): String {
        return Gson().toJson(this)
    }
}