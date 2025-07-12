package com.imaginamos.farmatodo.model.util

class ProcessOrderReq(var orderId: String = "",
                      var processedBy: String = "") {
    override fun toString(): String {
        return "ProcessOrderReq(orderId='$orderId', processedBy='$processedBy')"
    }
}


