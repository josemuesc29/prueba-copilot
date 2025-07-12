package com.imaginamos.farmatodo.model.algolia.cuponFilters
/*
{
  "uniqueKeys": [
    "DELIVERY_TYPE",
    "SOURCE"
  ],
  "campaigns": [
    {
      "name": "====[Cupones Solo para EYL y Android]===",
      "active": true,
      "coupons": [
        "ESCANEA22"
      ],
      "variables": [
        {
          "key": "SOURCE",
          "values": [
            "ANDROID",
            "IOS",
            "DEFAULT",
            "WEB"
          ]
        },
        {
          "key": "DELIVERY_TYPE",
          "values": [
            "SCANANDGO"
          ]
        },
        {
          "key": "CARDS_BINS",
          "values": []
        }
      ],
      "messagesError": [
        {
          "type": "ERROR_CARD",
          "keyError": "ERROR_COUPON_FILTER_CARD_BIN",
          "active": true,
          "value": "La tarjeta de credito no es valida para este cupon. Para continuar eliminarlo"
        }
      ],
      "variablesToApply": [
        "SOURCE",
        "DELIVERY_TYPE"
      ]
    },
    {
      "name": "====[Cupones Solo para App y Web]===",
      "active": false,
      "coupons": [
        "TEQUEREMOS",
        "BIENVENIDO",
        "FARMATODO10",
        "FARMATODO22"
      ],
      "variables": [
        {
          "key": "SOURCE",
          "values": [
            "IOS",
            "ANDROID",
            "WEB",
            "RESPONSIVE",
            "DEFAULT"
          ]
        },
        {
          "key": "DELIVERY_TYPE",
          "values": [
            "EXPRESS",
            "NATIONAL",
            "ENVIALOYA",
            "SUBSCRIPTION",
            "PROVIDER"
          ]
        },
        {
          "key": "CARDS_BINS",
          "values": []
        }
      ],
      "variablesToApply": [
        "SOURCE",
        "DELIVERY_TYPE"
      ]
    },
    {
      "name": "====[Cupones Solo para SCANANDGO y Android]===",
      "active": true,
      "coupons": [
        "NIVEA630",
        "SERVIENTREGA"
      ],
      "variables": [
        {
          "key": "SOURCE",
          "values": [
            "ANDROID",
            "RESPONSIVE",
            "WEB"
          ]
        },
        {
          "key": "DELIVERY_TYPE",
          "values": [
            "EXPRESS",
            "NATIONAL"
          ]
        },
        {
          "key": "CARDS_BINS",
          "values": []
        }
      ],
      "variablesToApply": [
        "SOURCE",
        "PROVIDER"
      ]
    },
    {
      "name": "====[Cupones Solo para SCANANDGO y Android]===",
      "active": true,
      "coupons": [
        "COUPONTEST1"
      ],
      "variables": [
        {
          "key": "SOURCE",
          "values": [
            "RESPONSIVE",
            "ANDROID",
            "IOS",
            "DEFAULT",
            "WEB"
          ]
        },
        {
          "key": "DELIVERY_TYPE",
          "values": [
            "NATIONAL",
            "EXPRESS"
          ]
        },
        {
          "key": "CARDS_BINS",
          "values": [
            "146980",
            "166797",
            "530375"
          ]
        }
      ],
      "variablesToApply": [
        "SOURCE",
        "PROVIDER"
      ]
    }
  ],
  "messagesError": [
    {
      "typeError": "ERROR_COUPON_FILTER_CARD_BIN",
      "title": "El cupón no aplica para esta tarjeta",
      "couponTypeMessage": "GENERIC_MESSAGE",
      "value": "Debes seleccionar la tarjeta del banco indicado en el beneficio del cupón"
    },
    {
      "typeError": "ERROR_COUPON_SOURCE",
      "title": "El cupón no aplica para esta tarjeta",
      "couponTypeMessage": "GENERIC_MESSAGE",
      "value": "El cupón no es válido para esta orden. Para continuar debes eliminarlo"
    },
    {
      "typeError": "ERROR_COUPON_CARD_TERMINAL",
      "couponTypeMessage": "GENERIC_MESSAGE",
      "value": "El descuento del cupón solo se aplicará en el cobro si utilizas la tarjeta del banco indicado"
    },
    {
      "typeError": "ERROR_COUPON_CARD_TERMINAL",
      "couponTypeMessage": "GENERIC_WITH_COUPON",
      "value": "Recuerda que para aplicar los beneficos del cupón debes pagar con tarjeta{couponName}",
      "aplicateCoupons": [
        "COUPONTEST1"
      ]
    },
    {
      "typeError": "ERROR_COUPON_FILTER_CARD_BIN",
      "couponTypeMessage": "GENERIC_WITH_COUPON",
      "value": "Debes seleccionar tu tarjeta {couponName} para aplicar el beneficio del cupón.",
      "aplicateCoupons": []
    },
    {
      "typeError": "ERROR_COUPON_FILTER_CARD_BIN",
      "couponName": "",
      "value": "Debes seleccionar tu tarjeta ITAU para aplicar el beneficio del cupón."
    }
  ],
  "iconMessage": [
    {
      "typeError": "ERROR_COUPON_FILTER_CARD_BIN",
      "value": "https://lh3.googleusercontent.com/M4_kNO55MJK0KgoTtkAKDnrZfRV8_bZ3898BciMwSn2y-W8npjUh7HJI0Q5gQufgajjBosMb8kN5iXSZA70j3OSLugyyqJGqDvfnA-9duD4XHaH3"
    },
    {
      "typeError": "ERROR_COUPON_CARD_TERMINAL",
      "value": "https://lh3.googleusercontent.com/WgxdvR54eShA8JwzBVBE8OAb01g9e0cuSnVUVjuG0QdUpr0f7oF0QLgDtLtzQh2bvhXEsAYM26hXsX7PNHI7h1u0OXUvwlh-1oqu34lc35XQRhvc"
    },
    {
      "typeError": "ERROR_COUPON_SOURCE",
      "value": "https://lh3.googleusercontent.com/Gm6oRr0HcFOWKgBU9_rjxevk0E_oU8tmVpKlPlHUoJ45s3uUo5NsUm2OY9mnRB3reaT2HChuv7Cg5rq6-sIp6DPCkAvCjLkyIbq6yon1ebcQpKaE"
    }
  ],
  "optionButtons": [
    {
      "typeError": "ERROR_COUPON_FILTER_CARD_BIN",
      "firstOption": "https://lh3.googleusercontent.com/M4_kNO55MJK0KgoTtkAKDnrZfRV8_bZ3898BciMwSn2y-W8npjUh7HJI0Q5gQufgajjBosMb8kN5iXSZA70j3OSLugyyqJGqDvfnA-9duD4XHaH3",
      "secondOption": ""
    },
    {
      "typeError": "ERROR_COUPON_CARD_TERMINAL",
      "firstOption": "https://lh3.googleusercontent.com/M4_kNO55MJK0KgoTtkAKDnrZfRV8_bZ3898BciMwSn2y-W8npjUh7HJI0Q5gQufgajjBosMb8kN5iXSZA70j3OSLugyyqJGqDvfnA-9duD4XHaH3",
      "secondOption": ""
    },
    {
      "typeError": "ERROR_COUPON_SOURCE",
      "firstOption": "https://lh3.googleusercontent.com/M4_kNO55MJK0KgoTtkAKDnrZfRV8_bZ3898BciMwSn2y-W8npjUh7HJI0Q5gQufgajjBosMb8kN5iXSZA70j3OSLugyyqJGqDvfnA-9duD4XHaH3",
      "secondOption": ""
    }
  ],
  "info": "SOURCE:[WEB,ANDROID,IOS,RESPONSIVE,DEFAULT],DELIVERY_TYPE:[EXPRESS,NATIONAL,ENVIALOYA,SUBSCRIPTION,SCANANDGO,PROVIDER]",
  "infobines": "Si la lista de bines esta vacia significa que aceptara cualquier combinacion",
  "objectID": "COUPONS.FILTER.CONFIG.DEV"
}

 */

class CouponFiltersConfig {
    var uniqueKeys: List<String>? = null
    var campaigns: List<Campaign>? = null
    var objectID: String? = null
    var messagesError: List<MessagesError>? = null
    var iconMessage: List<ErrorCoupon>? = null
    var optionButtons: List<OptionButtons>? = null
}
class Campaign {
    var name: String? = null
    var active: Boolean? = null
    var coupons: List<String>? = null
    var variables: List<Variables>? = null
    var variablesToApply: List<String>? = null
    var messagesErrorCoupon: List<MessagesError>? = null
    var payMethods: List<Long>? = null
    var messagePaymethod: String? = null

}
class Variables {
    var key: String? = null
    var values: List<String>? = null

}
class VariablesToApply {
    var key: String? = null
    var values: List<String>? = null

}
class MessagesError {
    var title: String? = null
    var typeError: String? = null
    var couponTypeMessage: String? = null
    var couponName: String? = null
    var value: String? = null
    var aplicateCoupons: List<String>? = null

}
class  ErrorCoupon {
    var typeError: String? = null
    var value: String? = null
}
class OptionButtons{
    var typeError: String? = null
    var firstOption: String? = null
    var secondOption: String? = null
    var firstAction: String? = null
    var secondAction: String? = null
}










