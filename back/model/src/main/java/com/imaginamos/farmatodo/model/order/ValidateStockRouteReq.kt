package com.imaginamos.farmatodo.model.order

import com.google.appengine.repackaged.com.google.gson.annotations.Expose
import com.google.appengine.repackaged.com.google.gson.annotations.SerializedName

class ValidateStockRouteReq {

    @SerializedName("stores")
    @Expose
    var stores: List<Int>? = null
    @SerializedName("items")
    @Expose
    var items: List<Item>? = null

    inner class Item {

        @SerializedName("item_id")
        @Expose
        var itemId: Long? = null
        @SerializedName("request_quantity")
        @Expose
        var requestQuantity: Int = 0

    }

}
