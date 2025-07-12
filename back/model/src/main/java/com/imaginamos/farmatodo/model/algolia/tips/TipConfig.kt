package com.imaginamos.farmatodo.model.algolia.tips

class TipConfig {
    var tip: Tip? = null
    var itemTips: List<ItemTip>? = null
    var defaultTipsByCity: List<DefaultTipsByCity>? = null
}

class DefaultTipsByCity {
    var cityId: String? = null
    var defaultTip: Int? = null
}

class ItemTip {
    var itemId: Int? = null
    var value: Float? = null
}

class Tip {
    var values: List<Int>? = null
    var valueMin: Int? = null
    var valueMax: Int? = null
    var increment: Int? = null
    var defaultTip: Int? = null
    var title: String? = null
    var description: String? = null
}





/*{
    "tip": {
    "values": [0, 2000, 4000, 6000],
    "valueMin": 0,
    "valueMax": 7000,
    "increment": 500,
    "default": 2000,
    "title": "Propina para el domiciliario",
    "description": "El domiciliario recibe el monto total de la propina"
},
    "itemTips": [
    {
        "itemId": 1004403,
        "value": 500
    },
    {
        "itemId": 1004270,
        "value": 2000
    }
    ],
    "defaultTipsByCity": [
    {
        "cityId": "BOG",
        "defaultTip": 2000
    },
    {
        "cityId": "BAR",
        "defaultTip": 500
    }
    ],
    "objectID": "TIPS.CONFIG"
}*/

