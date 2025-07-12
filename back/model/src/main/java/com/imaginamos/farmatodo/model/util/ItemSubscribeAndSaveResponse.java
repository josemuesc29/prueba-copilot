package com.imaginamos.farmatodo.model.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lorenzo.pinango on 28/11/2018.
 */
public class ItemSubscribeAndSaveResponse {

    private String status;       //  OK, CREATED, ACCEPTED, NO CONTENT
    private Integer status_code; //  200, 201,     202,       204
    private String message;      // FIELD X IS MISSING.
    private List<String> messages;

    public ItemSubscribeAndSaveResponse(){}

    public ItemSubscribeAndSaveResponse(String status, Integer status_code, String message) {
        this.status = status;
        this.status_code = status_code;
        this.message = message;
    }

    public ItemSubscribeAndSaveResponse(String status, Integer status_code, ArrayList<String> messages) {
        this.status = status;
        this.status_code = status_code;
        this.messages = messages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatus_code() {
        return status_code;
    }

    public void setStatus_code(Integer status_code) {
        this.status_code = status_code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
