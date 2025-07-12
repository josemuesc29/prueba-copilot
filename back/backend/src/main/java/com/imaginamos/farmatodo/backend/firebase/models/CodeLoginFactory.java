package com.imaginamos.farmatodo.backend.firebase.models;

import com.google.gson.JsonObject;

import java.util.Objects;

public class CodeLoginFactory {

    public static Boolean isValid(final NotifyCodeLogin request){
        if (Objects.isNull(request))
            return false;
        if (Objects.isNull(request.getKey()))
            return false;
        if (Objects.isNull(request.getCode()))
            return false;

        return true;
    }

    public static JsonObject buildBodyForNewCodeLogin(final NotifyCodeLogin request){
        final String key = request.getKey();
        final String code = request.getCode();

        JsonObject body = new JsonObject();
        body.addProperty(key, code);
        return body;
    }
}
