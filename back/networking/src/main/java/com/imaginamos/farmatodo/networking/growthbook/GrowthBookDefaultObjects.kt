package com.imaginamos.farmatodo.networking.growthbook

object OptimalRouteConstants {
    const val DEFAULT_FIRST_DISTANCE = 4.0f
    const val DEFAULT_SECOND_DISTANCE = 7.0f
    const val DEFAULT_PERCENTAGE_PRICE = 1000.0f
    const val DEFAULT_DISTANCE_POPUP = 3.0f
    const val DEFAULT_HOUR_TO_NEXT_SCHEDULE = 8
    const val DEFAULT_ACTIVE = true
    const val DEFAULT_RUTEO_API = true

    val DEFAULT_ITEMS_TO_IGNORE = listOf(
        "245700013", "245700021", "1031423", "1031422",
        "1053706", "1055794", "1054525", "1054526",
        "1053707", "1055795", "1050688", "1050417",
        "1054480", "1056112", "1052069", "1056113",
        "1051084", "1053766", "1053335", "1052801",
        "1051331", "1053441", "1053442", "1055712",
        "1056958", "1053443", "1053250", "1055817",
        "1053708", "1055086", "1054550", "1054551",
        "1052068", "1050688", "1053709", "201900005"
    )

    val DEFAULT_NEW_FLOW_OPTIMAL_ROUTE_DISTANCE = false
}