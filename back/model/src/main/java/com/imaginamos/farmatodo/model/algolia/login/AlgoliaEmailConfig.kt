package com.imaginamos.farmatodo.model.algolia.login

import com.google.appengine.repackaged.com.google.gson.Gson

class AlgoliaEmailConfig {
    var subject: String? = null
    var message: String? = null

    override fun toString(): String {
        return Gson().toJson(this)
    }
}