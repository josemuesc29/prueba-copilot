package com.imaginamos.farmatodo.model.order

import com.google.appengine.repackaged.com.google.gson.annotations.Expose
import com.google.appengine.repackaged.com.google.gson.annotations.SerializedName

class ValidateStockRouteRes {

    var code : String = ""
    var message: String = ""
    var data: Data? = null

    inner class Data {
        @SerializedName("result")
        @Expose
        var result: List<Result>? = null

        inner class Result {

            @SerializedName("store")
            @Expose
            var store: Int = 0
            @SerializedName("isValid")
            @Expose
            var isValid: Boolean = false
            @SerializedName("items")
            @Expose
            var items: List<Item>? = null


            inner class Item {

                @SerializedName("id")
                @Expose
                var id: Int = 0
                @SerializedName("requestQuantity")
                @Expose
                var requestQuantity: Int = 0
                @SerializedName("isValid")
                @Expose
                var isValid: Boolean = false

            }

        }
        fun responseIsValid (): Boolean {
            return !result.isNullOrEmpty()
        }
    }

}
