package com.imaginamos.farmatodo.model.order

import com.google.appengine.repackaged.com.google.gson.Gson
import java.util.ArrayList

class ValidateOrderReq {

    var source: String? = null
    var customerId: Int = 0
    var storeId: Int = 0
    var deliveryType: String? = null
    var items = ArrayList<Item>()
    var coupons = ArrayList<Coupon>()
    var idCustomerWebSafe: String? = null
    var farmaCredits: Long = 0
    var paymentCardId: Int = 0
    var daneCodeCustomer: String? = ""
    var addressCustomer: String? = ""
    var talonOneData: Map<String, Object>? = null
    var nearbyStores: List<Int> = ArrayList()
    var buildCodeNumberApp: Int = 0


    inner class Item {

        var itemId: Int = 0
        var quantityRequested: Int = 0

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

    inner class Coupon {

        var couponType: String? = null
        var offerId: Int = 0

        override fun toString(): String {
            return Gson().toJson(this)
        }
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }


}
