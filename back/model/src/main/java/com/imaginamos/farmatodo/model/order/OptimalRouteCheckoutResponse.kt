package com.imaginamos.farmatodo.model.order

import java.util.*

class OptimalRouteCheckoutResponse {
    var isOptimalRouteIsValid = false
    var posibleStoreToAssign: Int? = null
    var distance: String? = null
    var isRouteHasTransfer = false
    var itemsToSubstitute: List<Int> = ArrayList()
    var isForceToSchedule = false
    var isShowTransferOption = true
    var toSubstitutes: List<ItemToSubstitute> = ArrayList()
    var dateFirstSchedule: Date? = null
    var idOptimalRoute: String? = null
    var nearestStore: Int? = null
    var addressIsRedZone: Boolean = false
}