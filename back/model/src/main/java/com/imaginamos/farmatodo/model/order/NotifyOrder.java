package com.imaginamos.farmatodo.model.order;

public class NotifyOrder {
    private Long idOrder;
    private String message;
    private Long idUser;
    private String title;

    public Long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(Long idOrder) {
        this.idOrder = idOrder;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "NotifyOrder{" +
                "idOrder=" + idOrder +
                ", message='" + message + '\'' +
                ", idUser=" + idUser +
                ", title='" + title + '\'' +
                '}';
    }
}
