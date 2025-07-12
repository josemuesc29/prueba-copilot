package com.imaginamos.farmatodo.model.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

import java.io.IOException

enum class EnableForEnum {
    ANDROID, IOS, RESPONSIVE, WEB;

    @JsonValue
    fun toValue(): String? {
        when (this) {
            ANDROID -> return "ANDROID"
            IOS -> return "IOS"
            RESPONSIVE -> return "RESPONSIVE"
            WEB -> return "WEB"
        }
    }

    companion object {

        @JsonCreator
        @Throws(IOException::class)
        fun forValue(value: String): EnableForEnum {
            if (value == "ANDROID") return ANDROID
            if (value == "IOS") return IOS
            if (value == "RESPONSIVE") return RESPONSIVE
            if (value == "WEB") return WEB
            throw IOException("Cannot deserialize EnableFor")
        }
    }
}
