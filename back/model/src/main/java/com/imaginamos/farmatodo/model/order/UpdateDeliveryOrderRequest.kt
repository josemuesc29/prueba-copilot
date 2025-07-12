package com.imaginamos.farmatodo.model.order

import com.google.appengine.repackaged.com.google.gson.Gson
import java.util.*

class UpdateDeliveryOrderRequest {

    val token: String? = null
    val tokenIdWebSafe: String? = null
    val idCustomerWebSafe: String? = null
    var items: List<Item> = ArrayList()
    var itemsToDelete: List<Item> = ArrayList()
    var idStoreGroupFromRequest: Int = 0
    var deliveryType: String? = null
    var isInShoppingCart: Boolean = false

    class Item {
        var itemId: Int = 0
        var quantityRequested: Int = 0
        var origin: String? = null
        var observations: String? = null
        var isSubstitute: Boolean = false
        override fun toString(): String {
            return "Item(itemId=$itemId, quantityRequested=$quantityRequested, origin=$origin, observations=$observations, isSubstitute=$isSubstitute)"
        }

    }

    override fun toString(): String {
        return Gson().toJson(this)
    }


}
