package com.imaginamos.farmatodo.model.order

import java.util.*


class CreateOrderResponseCore {
    var id: Int = 0
    var createDate: Long? = null
    var address: String? = null
    var isUpdateShopping: Boolean = false
    var tracing: List<Tracing> = ArrayList()

    inner class Tracing {

        var id: Int = 0
        var createDate: Date? = null
        var comments: String? = null
        var cancellationReason: Long? = null
        var courier: Long? = null
        var status: Int = 0
    }

    override fun toString(): String {
        return "CreateOrderResponseCore(id=$id, createDate=$createDate, address=$address, isUpdateShopping=$isUpdateShopping, tracing=$tracing)"
    }


}
