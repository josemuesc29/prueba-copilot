package com.imaginamos.farmatodo.backend.images;

public class AnswerUploadImage {
    private Integer item;
    private String urlImage;
    private Integer position;
    private boolean confirmation = false;
    private String status;
    private Integer statusCode;
    private String message;
    private Boolean principal;

    public AnswerUploadImage(Integer item, String urlImage, boolean confirmation, String status, Integer statusCode, String message) {
        this.item = item;
        this.urlImage = urlImage;
        this.confirmation = confirmation;
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
    }

    public AnswerUploadImage(Integer item, String urlImage, Integer position, boolean confirmation, String status, Integer statusCode, String message, Boolean principal) {
        this.item = item;
        this.urlImage = urlImage;
        this.position = position;
        this.confirmation = confirmation;
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
        this.principal = principal;
    }

    public AnswerUploadImage() {

    }

    public Integer getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public boolean isConfirmation() {
        return confirmation;
    }

    public void setConfirmation(boolean confirmation) {
        this.confirmation = confirmation;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Boolean getPrincipal() { return principal; }

    public void setPrincipal(Boolean principal) { this.principal = principal; }
}
