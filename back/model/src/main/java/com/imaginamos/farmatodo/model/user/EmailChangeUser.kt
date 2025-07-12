package com.imaginamos.farmatodo.model.user

import com.google.api.server.spi.config.AnnotationBoolean
import com.google.api.server.spi.config.ApiResourceProperty
import com.googlecode.objectify.annotation.Entity
import com.googlecode.objectify.annotation.Id
import com.googlecode.objectify.annotation.Index

@Entity
class EmailChangeUser  {
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Id
    var idEmailChangeUser: String? = null

    @Index
    var userId: Long? = null

//    var emailChanged: Boolean? = null
    override fun toString(): String {
        return "EmailChangeUser(idEmailChangeUser=$idEmailChangeUser, userId=$userId)"
    }


}