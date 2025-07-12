package com.imaginamos.farmatodo.networking.models.shorturl

data class ShortUrlReq(var urlToShort : String? ) {

    fun isValid(): Boolean {
        return !urlToShort.isNullOrEmpty()
    }

    override fun toString(): String {
        return "ShortUrlReq(urlToShort='$urlToShort')"
    }
}

data class ShortUrlRes(var oldURL: String?, var newURL: String? ) {

    fun isValid(): Boolean {
        return !oldURL.isNullOrEmpty() && !newURL.isNullOrEmpty()
    }

    override fun toString(): String {
        return "ShortUrlRes(oldURL='$oldURL', newURL='$newURL')"
    }
}

