package com.imaginamos.farmatodo.model.util

class MostSalesReq {
    var departments: List<Department> = ArrayList()

    fun requestIsValid(): Boolean {
        return !departments.isEmpty()
    }

    override fun toString(): String {
        return "MostSalesReq(departments=$departments)"
    }

}

class Department {
    var departmentId: Long? = null
    var itemsMostSales: List<ItemsMostSale> = ArrayList()

    fun isValid(): Boolean {
        return departmentId != null && !itemsMostSales.isEmpty()
    }

    override fun toString(): String {
        return "Department(departmentId=$departmentId, itemsMostSales=$itemsMostSales)"
    }

}

class ItemsMostSale {
    var itemId: Long? = null
    var sales: Long? = null

    fun isValid(): Boolean {
        return itemId != null && sales != null
    }

    override fun toString(): String {
        return "ItemsMostSale(itemId=$itemId, sales=$sales)"
    }


}
