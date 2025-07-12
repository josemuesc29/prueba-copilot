package com.imaginamos.farmatodo.model.order;

public class CancelOrderReq {

    private String orderId;
    private String observation;
    private String rol;
    private String correoUsuario;
    private Integer statusId;
    private Long employeeNumber;
    private String orderGuide;
    private Long cancellationReasonId;
    private String operationType;
    private String message;
    private String messengerId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setCorreoUsuario(String correoUsuario) {
        this.correoUsuario = correoUsuario;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Long getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(Long employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getOrderGuide() {
        return orderGuide;
    }

    public void setOrderGuide(String orderGuide) {
        this.orderGuide = orderGuide;
    }

    public Long getCancellationReasonId() {
        return cancellationReasonId;
    }

    public void setCancellationReasonId(Long cancellationReasonId) {
        this.cancellationReasonId = cancellationReasonId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessengerId() {
        return messengerId;
    }

    public void setMessengerId(String messengerId) {
        this.messengerId = messengerId;
    }
}
