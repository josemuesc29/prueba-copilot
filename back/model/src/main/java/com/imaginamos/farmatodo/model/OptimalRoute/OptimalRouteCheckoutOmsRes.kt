package com.imaginamos.farmatodo.model.OptimalRoute

class OptimalRouteCheckoutOmsRes{
    var code: String? = null
    var message: String? = null
    var data: Data? = null


    inner class Data {
        var possibleStoreToAssing: Int? = null
        var totalPercentage: Float? = null
        var routeHasTransfer: Boolean? = null
        var uniqueStore: Boolean? = null
        var itemsPercentage: List<ItemPercentageObj> = ArrayList()

        inner class ItemPercentageObj {
            var item: Int? = null
            var percentage: Float? = null

            override fun toString(): String {
                return "ItemPercentageObj(item=$item, percentage=$percentage)"
            }

        }

        override fun toString(): String {
            return "Data(possibleStoreToAssing=$possibleStoreToAssing, totalPercentage=$totalPercentage, routeHasTransfer=$routeHasTransfer, uniqueStore=$uniqueStore, itemsPercentage=$itemsPercentage)"
        }


    }

    override fun toString(): String {
        return "OptimalRouteCheckoutOmsRes(code=$code, message=$message, data=$data)"
    }


}