package com.imaginamos.farmatodo.model.algolia

import com.google.appengine.repackaged.com.google.gson.annotations.SerializedName

// import com.google.appengine.repackaged.com.google.gson.annotations.SerializedName

class CategoriaAlgolia {

    @SerializedName("id")
    var id: String = ""
    @SerializedName("name")
    var name: String = ""
    @SerializedName("code")
    var code: String = ""
    @SerializedName("type")
    var type: String = ""

    override fun toString(): String {
        return "OptimalRouteDistance(id=$id, name=$name)"
    }



}