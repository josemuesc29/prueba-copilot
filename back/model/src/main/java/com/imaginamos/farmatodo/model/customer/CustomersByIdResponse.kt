package com.imaginamos.farmatodo.model.customer

import java.util.ArrayList

class CustomersByIdResponse {
    var code: String? = ""
    var message: String? = ""
    var data = ArrayList<CustomerResponse>()
}
class CustomerByIdRequest {
    var customerIds: List<Int> = ArrayList()
}
