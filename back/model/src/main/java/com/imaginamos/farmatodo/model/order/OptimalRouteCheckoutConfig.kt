package com.imaginamos.farmatodo.model.order

import com.google.appengine.repackaged.com.google.gson.Gson
import com.imaginamos.farmatodo.model.algolia.OptimalRouteDistance
import java.util.ArrayList

class OptimalRouteCheckoutRequest {

    var addressLat: Float = 0.0f
    var addressLon: Float  = 0.0f
    var idAddress: Long = 0
    var address: String = ""
    var city: String = ""
    var items: List<Item> = ArrayList()
    var idCustomerWebSafe: String = ""
    var deliveryType: String = ""
    var customerId: String = ""
    var optimalRouteDistance: OptimalRouteDistance? = null

    class Item {

        var itemId: Int = 0
        var requestQuantity: Int = 0
        var unitPrice: Double = 0.0
        override fun toString(): String {
            return "Item(itemId=$itemId, requestQuantity=$requestQuantity, itemPrice=$unitPrice)"
        }


    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

    fun requestIsValid(): Boolean {
        return (addressLat != 0f && addressLon != 0f && city.isNotBlank() )
    }


}
