package com.imaginamos.farmatodo.model.user

import com.google.api.server.spi.config.AnnotationBoolean
import com.google.api.server.spi.config.ApiResourceProperty
import com.googlecode.objectify.annotation.Entity
import com.googlecode.objectify.annotation.Id
import com.googlecode.objectify.annotation.Index

/**
 * [description]
 *
 * @author: Cristhian Rodriguez
 * @version: 1.0
 * @since: 1.0
 */
@Entity
class BlockedUser {
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Id
    var idBlockedUser: String? = null

    //@ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Index
    var reasonBlock: String? = null

    @Index
    var idUser = 0

    override fun toString(): String {
        return "BlockedUser{" +
                "idBlockedUser='" + idBlockedUser + '\'' +
                ", reasonBlock='" + reasonBlock + '\'' +
                ", idUser=" + idUser +
                '}'
    }
}