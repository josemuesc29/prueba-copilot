package com.imaginamos.farmatodo.model.OptimalRoute


class OptimalRouteCheckoutOmsReq {

    var city: String = "";
    var possibleStores: List<Int> = ArrayList()
    var shoppingCart: List<shoppingCartObj> =  ArrayList()


    class shoppingCartObj{
        var item: Long = 0
        var requestQuantity: Int = 0
        override fun toString(): String {
            return "shoppingCartObj(itemId=$item, requestQuantity=$requestQuantity)"
        }


    }

    override fun toString(): String {
        return "OptimalRouteCheckoutOmsReq(city='$city', possibleStores=$possibleStores, shoppingCart=$shoppingCart)"
    }

    fun requestIsValid(): Boolean {
        return (!city.isNullOrBlank() && !possibleStores.isEmpty() && !shoppingCart.isEmpty())
    }


}