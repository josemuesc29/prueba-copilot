package com.imaginamos.farmatodo.model.home

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.io.IOException

enum class DataFromEnum {
    SUGGESTS,
    RECENTLY_VIEWED,
    PREVIOUS_ITEMS,
    RELATED_ITEMS,
    BAZAARVOICE,
    SAME_BRAND_ITEMS,
    MAIN_ITEM,
    HIGHLIGHTS,
    MOST_SALES,
    USER_NAME,
    FAVORITES,
    RECT_BANNER,
    TOW_RECT_BANNER,
    SHORTCUT_BANNER,
    GRID_BLOG_LIST,
    ITEM_LIST_TWO_ROWS,
    VIDEO_LIST,
    FLASH_OFFERS_ITEMS,
    TEXT_SEO,
    TEXT_VADEMECUM,
    GET_ITEM,
    GET_HOME,
    GET_LANDING;



    @JsonValue
    fun toValue(): String? {
        when (this) {
            SUGGESTS -> return "SUGGESTS"
            RECENTLY_VIEWED -> return "RECENTLY_VIEWED"
            PREVIOUS_ITEMS -> return "PREVIOUS_ITEMS"
            RELATED_ITEMS -> return "RELATED_ITEMS"
            BAZAARVOICE -> return "BAZAARVOICE"
            SAME_BRAND_ITEMS -> return "SAME_BRAND_ITEMS"
            MAIN_ITEM -> return "MAIN_ITEM"
            HIGHLIGHTS -> return "HIGHLIGHTS"
            MOST_SALES -> return "MOST_SALES"
            USER_NAME -> return "USER_NAME"
            FAVORITES -> return "FAVORITES"
            RECT_BANNER -> return "RECT_BANNER"
            TOW_RECT_BANNER -> return "TOW_RECT_BANNER"
            SHORTCUT_BANNER -> return "SHORTCUT_BANNER"
            GRID_BLOG_LIST -> return "GRID_BLOG_LIST"
            ITEM_LIST_TWO_ROWS -> return "ITEM_LIST_TWO_ROWS"
            VIDEO_LIST -> return "VIDEO_LIST"
            FLASH_OFFERS_ITEMS -> return "FLASH_OFFERS_ITEMS"
            TEXT_SEO -> return "TEXT_SEO"
            TEXT_VADEMECUM -> return "TEXT_VADEMECUM"
            GET_ITEM -> return "GET_ITEM"
            GET_HOME -> return "GET_HOME"
            GET_LANDING -> return "GET_LANDING"
        }
    }

    companion object {
        @JsonCreator
        @Throws(IOException::class)
        fun forValue(value: String): DataFromEnum {
            if (value == "SUGGESTS") return SUGGESTS
            if (value == "RECENTLY_VIEWED") return RECENTLY_VIEWED
            if (value == "PREVIOUS_ITEMS") return PREVIOUS_ITEMS
            if (value == "RELATED_ITEMS") return RELATED_ITEMS
            if (value == "BAZAARVOICE") return BAZAARVOICE
            if (value == "SAME_BRAND_ITEMS") return SAME_BRAND_ITEMS
            if (value == "MAIN_ITEM") return MAIN_ITEM
            if (value == "HIGHLIGHTS") return HIGHLIGHTS
            if (value == "MOST_SALES") return MOST_SALES
            if (value == "USER_NAME") return USER_NAME
            if (value == "FAVORITES") return FAVORITES
            if (value == "RECT_BANNER") return RECT_BANNER
            if (value == "TOW_RECT_BANNER") return TOW_RECT_BANNER
            if (value == "SHORTCUT_BANNER") return SHORTCUT_BANNER
            if (value == "GRID_BLOG_LIST") return GRID_BLOG_LIST
            if (value == "ITEM_LIST_TWO_ROWS") return ITEM_LIST_TWO_ROWS
            if (value == "VIDEO_LIST") return VIDEO_LIST
            if (value == "FLASH_OFFERS_ITEMS") return FLASH_OFFERS_ITEMS
            if (value == "TEXT_SEO") return TEXT_SEO
            if (value == "TEXT_VADEMECUM") return TEXT_VADEMECUM
            if (value == "GET_ITEM") return GET_ITEM
            if (value == "GET_HOME") return GET_HOME
            if (value == "GET_LANDING") return GET_LANDING
            throw  IOException("Cannot deserialize DataFrom Enum")
        }
    }
}