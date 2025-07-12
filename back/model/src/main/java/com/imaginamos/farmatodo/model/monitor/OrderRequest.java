package com.imaginamos.farmatodo.model.monitor;

import java.util.List;

public class OrderRequest {
    private Long orderId;
    private String perfil;
    private String billId;
    private String billDate;
    private String observation;
    private String rol;
    private String correoUsuario;
    private String token;
    private String employeeNumber;
    private String startDate;
    private String endDate;
    private String documentNumberClient;
    private int courierId;
    private String uuid;
    private List<OrderItem> items;
    private String domicilio;
    private List<Order> orders;
    private String deliveryType;
    private String store;
    private FilterOrder filters;
    private String statusId;
    private String orderGuide;
    private int numberOrders;
    private String phoneCustomer;
    private String city;
    private Long paymentMethodId;
    private Long cancellationReasonId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDocumentNumberClient() {
        return documentNumberClient;
    }

    public void setDocumentNumberClient(String documentNumberClient) {
        this.documentNumberClient = documentNumberClient;
    }

    public int getCourierId() {
        return courierId;
    }

    public void setCourierId(int courierId) {
        this.courierId = courierId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public FilterOrder getFilters() {
        return filters;
    }

    public void setFilters(FilterOrder filters) {
        this.filters = filters;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getOrderGuide() {
        return orderGuide;
    }

    public void setOrderGuide(String orderGuide) {
        this.orderGuide = orderGuide;
    }

    public int getNumberOrders() {
        return numberOrders;
    }

    public void setNumberOrders(int numberOrders) {
        this.numberOrders = numberOrders;
    }

    public String getPhoneCustomer() {
        return phoneCustomer;
    }

    public void setPhoneCustomer(String phoneCustomer) {
        this.phoneCustomer = phoneCustomer;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Long getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(Long paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public Long getCancellationReasonId() {
        return cancellationReasonId;
    }

    public void setCancellationReasonId(Long cancellationReasonId) {
        this.cancellationReasonId = cancellationReasonId;
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "orderId=" + orderId +
                ", perfil='" + perfil + '\'' +
                ", billId='" + billId + '\'' +
                ", billDate='" + billDate + '\'' +
                ", observation='" + observation + '\'' +
                ", rol='" + rol + '\'' +
                ", correoUsuario='" + correoUsuario + '\'' +
                ", token='" + token + '\'' +
                ", employeeNumber='" + employeeNumber + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", documentNumberClient='" + documentNumberClient + '\'' +
                ", courierId=" + courierId +
                ", uuid='" + uuid + '\'' +
                ", items=" + items +
                ", domicilio='" + domicilio + '\'' +
                ", orders=" + orders +
                ", deliveryType='" + deliveryType + '\'' +
                ", store='" + store + '\'' +
                ", filters=" + filters +
                ", statusId='" + statusId + '\'' +
                ", orderGuide='" + orderGuide + '\'' +
                ", numberOrders=" + numberOrders +
                ", phoneCustomer='" + phoneCustomer + '\'' +
                ", city='" + city + '\'' +
                ", paymentMethodId=" + paymentMethodId +
                ", cancellationReasonId=" + cancellationReasonId +
                '}';
    }
}
