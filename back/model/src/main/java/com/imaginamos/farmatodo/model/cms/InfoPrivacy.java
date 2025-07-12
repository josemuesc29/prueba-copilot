package com.imaginamos.farmatodo.model.cms;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;

/**
 * Created by Admin on 23/06/2017.
 */

@Entity
public class InfoPrivacy {
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    @Id
    private String idInfoPrivacy;
    @IgnoreSave
    private String token;
    @IgnoreSave
    private String tokenIdWebSafe;
    private String privacyPolitics;
    private String habeasData;
    private String termsAndConditions;
    private String termsAndConditionsPrime;

    public String getIdInfoPrivacy() {
        return idInfoPrivacy;
    }

    public void setIdInfoPrivacy(String idInfoPrivacy) {
        this.idInfoPrivacy = idInfoPrivacy;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenIdWebSafe() {
        return tokenIdWebSafe;
    }

    public void setTokenIdWebSafe(String tokenIdWebSafe) {
        this.tokenIdWebSafe = tokenIdWebSafe;
    }

    public String getPrivacyPolitics() {
        return privacyPolitics;
    }

    public void setPrivacyPolitics(String privacyPolitics) {
        this.privacyPolitics = privacyPolitics;
    }

    public String getHabeasData() {
        return habeasData;
    }

    public void setHabeasData(String habeasData) {
        this.habeasData = habeasData;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public String getTermsAndConditionsPrime() {
        return termsAndConditionsPrime;
    }

    public void setTermsAndConditionsPrime(String termsAndConditionsPrime) {
        this.termsAndConditionsPrime = termsAndConditionsPrime;
    }
}
