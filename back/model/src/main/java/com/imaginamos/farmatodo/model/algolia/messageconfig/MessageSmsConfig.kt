package com.imaginamos.farmatodo.model.algolia.messageconfig

/*
{
  "campaignMessages": [
    {
      "name": "====[Cupones Solo para EYL y Android]===",
      "active": true,
      "Messages": [
        {
          "key": "MESSAGE_VALIDATION_TOKEN_PHONE",
          "values": "Bienvenido a Farmatodo. Por favor coloca el codigo {CODE} para completar tu registro."
        },
        {
          "key": "MESSAGE_KEY_SMS",
          "values": "Tu clave para iniciar sesión en FARMATODO es: "
        },
        {
          "key": "MESSAGE_ORDER_CODE",
          "values": "Bienvenido a farmatodo. La transaccion asociada a tu orden {orderId} fue aprobada: "
        }
      ]
    }
  ],
  "objectID": "MESAGGES.MSG.CONFIG"
}
 */
class MessageSmsConfig {
    var campaignMessages: List<CampaignMessages>? = null
    var objectID: String? = null
}
class CampaignMessages {
    var name: String? = null
    var active: Boolean? = null
    var type: String? = null
    var messages: List<Message>? = null
}
class Message {
    var key: String? = null
    var values: String? = null
}