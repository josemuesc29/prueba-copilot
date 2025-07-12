package com.imaginamos.farmatodo.networking.models.addresses.osrm

import com.google.gson.Gson

class TableOSRMResponse {
    var code: String? = ""
    var distances: List<List<Double>> = ArrayList()
    override fun toString(): String {
        return Gson().toJson(this)
    }
}