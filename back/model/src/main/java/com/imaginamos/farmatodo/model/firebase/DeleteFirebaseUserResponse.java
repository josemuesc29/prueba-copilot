package com.imaginamos.farmatodo.model.firebase;

/**
 * Created by JPuentes on 25/09/2018.
 */
public class DeleteFirebaseUserResponse {

    private int status;
    private String message;

    public DeleteFirebaseUserResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
