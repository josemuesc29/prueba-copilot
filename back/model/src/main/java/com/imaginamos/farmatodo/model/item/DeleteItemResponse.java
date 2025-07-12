package com.imaginamos.farmatodo.model.item;

import java.util.List;

/**
 * Created by JPuentes on 13/07/2018.
 */
public class DeleteItemResponse {
    private String status;       //  OK, CREATED, ACCEPTED, NO CONTENT
    private Integer status_code; //  200, 201,     202,       204
    private String message;      // FIELD X IS MISSING.
    private List<String> messages;

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
