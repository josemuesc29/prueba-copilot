package com.imaginamos.farmatodo.model.util

import com.imaginamos.farmatodo.model.order.RequestSourceEnum
import java.util.*
import java.util.logging.Logger
import javax.servlet.http.HttpServletRequest

class FTDUtilities {

    private val logger = Logger.getLogger(FTDUtilities::class.java.name)

    fun addSubtractDaysDate(date: Date?, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }

    fun addHoursDate(date: Date?, hours: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR_OF_DAY, hours)
        return calendar.time
    }

    fun getSourceFromRequestHeader(request: HttpServletRequest): RequestSourceEnum {

        if (request.getHeader("source") == null){
            return RequestSourceEnum.WEB;
        }

        return RequestSourceEnum.valueOf(request.getHeader("source"))

    }

    fun getSourceFromRequestHeaderForPays(request: HttpServletRequest): RequestSourceEnum {

        if (request.getHeader("source") == null){
            logger.info("Source request null")
            return RequestSourceEnum.DEFAULT;
        }

        return RequestSourceEnum.valueOf(request.getHeader("source"))

    }

    fun getSourceFromString(source: String): RequestSourceEnum {
        return RequestSourceEnum.valueOf(source)
    }

}
