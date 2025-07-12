package com.imaginamos.farmatodo.model.city

import com.google.appengine.repackaged.com.google.gson.Gson

class GeoGridsConfigAlgolia {
    val cities: List<CityGrid>? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

class CityGrid {
    var id: String? = null
    var municipalityList: List<MunicipalityList>? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

class MunicipalityList {

    var name: String? = null
    var coordinates: List<List<Double>>? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var active: Boolean? = null
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
