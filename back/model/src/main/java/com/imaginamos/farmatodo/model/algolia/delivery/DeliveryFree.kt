package com.imaginamos.farmatodo.model.algolia.delivery



/*
{
  "uniqueKeys": [
    "CITY",
    "ITEMS",
    "DELIVERY_TYPE",
    "MIN_AMOUNT",
    "CUSTOMER",
    "CUSTOMER_PRIME",
    "MIN_QUANTITY",
    "PURCHASES"
  ],
  "combinations": [
    "FREE_DELIVERY_FOR_CUSTOMER_PRIME",
    "MIN_AMOUNT_ITEMS",
    "FREE_DELIVERY_FOR_FIRST_PURCHASES"
  ],
  "campaigns": [
    {
      "name": "====[ENVIO GRATIS PARA CUSTOMERS PRIME]===",
      "active": true,
      "banner": {
        "active": false,
        "message": "",
        "imageUrl": ""
      },
      "combinationToApply": "FREE_DELIVERY_FOR_CUSTOMER_PRIME",
      "variables": [
        {
          "key": "CUSTOMER_PRIME",
          "values": [
            "85382"
          ]
        },
        {
          "key": "MIN_AMOUNT",
          "values": [
            "20000"
          ]
        }
      ],
      "variablesToApply": [
        "CUSTOMER_PRIME",
        "MIN_AMOUNT"
      ]
    }
  ],
  "objectID": "FREE.DELIVERY.CONFIG.DEV"
}
 */
class DeliveryFree {
    var uniqueKeys: List<String>? = null
    var combinations: List<String>? = null
    var campaigns: List<CampaignFree>? = null
    var objectID: String? = null
}

class CampaignFree {
    var name: String? = null
    var active: Boolean? = null
    var banner: Banner? = null
    var combinationToApply: String? = null
    var variables: List<VariablesFree>? = null
    var variablesToApply: List<String>? = null
}

class Banner {
    var active: Boolean? = null
    var message: String? = null
    var imageUrl: String? = null
}

class VariablesFree {
    var key: String? = null
    var values: List<String>? = null
}

class VariablesToApplyFree {
    var key: String? = null
    var values: List<String>? = null
}



