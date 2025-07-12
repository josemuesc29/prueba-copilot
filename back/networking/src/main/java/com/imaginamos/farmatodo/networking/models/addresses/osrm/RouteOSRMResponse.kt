package com.imaginamos.farmatodo.networking.models.addresses.osrm

import com.google.gson.Gson

class RouteOSRMResponse {
    var code: String? = ""
    var routes: List<Route> = ArrayList()
    var waypoints: List<Waypoint> = ArrayList()
    override fun toString(): String {
        return Gson().toJson(this)
    }

}

class Route {
    var geometry: String? = ""
    var legs: List<Leg>? = ArrayList()
    var weightName: String? = ""
    var weight: Double? = 0.0
    var duration: Double? = 0.0
    var distance: Double? = 0.0
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

class Leg {
    var steps: List<Any?>? = ArrayList()
    var summary: String? = ""
    var weight: Double? = 0.0
    var duration: Double? = 0.0
    var distance: Double? = 0.0
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
class Waypoint {
    var hint: String? = ""
    var distance: Double? = 0.0
    var name: String? = ""
    var location: List<Double>? = ArrayList()
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
