package com.imaginamos.farmatodo.model.firebase;

public class FirebaseLoginCodeResponse {

    private String codeLogin;
    private String message;

    public String getCodeLogin() {
        return codeLogin;
    }

    public void setCodeLogin(String codeLogin) {
        this.codeLogin = codeLogin;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
