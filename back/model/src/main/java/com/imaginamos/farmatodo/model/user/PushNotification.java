package com.imaginamos.farmatodo.model.user;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@Entity
public class PushNotification {

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Id
    private String idPushNotification;

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Parent
    private Key<User> user;
    @Index
    private Long timeLastPush;

    public String getIdPushNotification() {
        return idPushNotification;
    }

    public void setIdPushNotification(String idPushNotification) {
        this.idPushNotification = idPushNotification;
    }

    public Key<User> getUser() {
        return user;
    }

    public void setUser(Key<User> user) {
        this.user = user;
    }

    public Long getTimeLastPush() {
        return timeLastPush;
    }

    public void setTimeLastPush(Long timeLastPush) {
        this.timeLastPush = timeLastPush;
    }
}
