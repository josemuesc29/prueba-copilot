package com.imaginamos.farmatodo.model.algolia

class ExcludeStoresCreateOrder {
    var enableStores: List<Long>? =  emptyList()
    override fun toString(): String {
        return "ExcludeStoresCreateOrder(stores=$enableStores)"
    }

}