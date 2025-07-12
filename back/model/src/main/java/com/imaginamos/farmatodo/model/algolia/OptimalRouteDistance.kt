package com.imaginamos.farmatodo.model.algolia

import com.google.appengine.repackaged.com.google.gson.Gson

class OptimalRouteDistance {

    var firstDistance: Float = 0.toFloat()

    var secondDistance: Float = 0.toFloat()

    var percentagePrice: Float = 0.toFloat()

    var distancePopUp: Float = 0.toFloat()

    var hourToNextSchedule: Int? = null

    var active: Boolean = false

    var ruteoApi: Boolean = true

    var itemsToIgnore: List<String>? = null

    var newFlowOptimalRouteDistance: Boolean = false

    var cacheable: Boolean = false

    override fun toString(): String {
        return  Gson().toJson(this)
    }


}
