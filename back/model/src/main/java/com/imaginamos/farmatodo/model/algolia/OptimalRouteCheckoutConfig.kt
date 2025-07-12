package com.imaginamos.farmatodo.model.algolia


class OptimalRouteCheckoutConfig {
    var minPercentStore: Long? = 0L
    var minPercentItem: Long? = 0L

    override fun toString(): String {
        return "OptimalRouteCheckoutConfig(minPercentStore=$minPercentStore, minPercentItem=$minPercentItem)"
    }


}
