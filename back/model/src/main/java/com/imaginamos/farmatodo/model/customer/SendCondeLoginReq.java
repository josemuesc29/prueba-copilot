package com.imaginamos.farmatodo.model.customer;

public class SendCondeLoginReq {
    private Boolean byEmail;
    private Boolean byPhone;
    private Boolean byWhatsapp;
    private Boolean byCall;
    private Long idCustomer;
    private String email;
    private String name;
    private Long phone;
    private String deviceId;
    private Boolean register;

    public Boolean getByEmail() {
        return byEmail;
    }

    public void setByEmail(Boolean byEmail) {
        this.byEmail = byEmail;
    }

    public Boolean getByPhone() {
        return byPhone;
    }

    public void setByPhone(Boolean byPhone) {
        this.byPhone = byPhone;
    }

    public Boolean getByWhatsapp() {
        return byWhatsapp;
    }

    public void setByWhatsapp(Boolean byWhatsapp) {
        this.byWhatsapp = byWhatsapp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getIdCustomer() {
        return idCustomer;
    }

    public void setIdCustomer(Long idCustomer) {
        this.idCustomer = idCustomer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getByCall() {
        return byCall;
    }

    public void setByCall(Boolean byCall) {
        this.byCall = byCall;
    }

    public Long getPhone() {
        return phone;
    }

    public void setPhone(Long phone) {
        this.phone = phone;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Boolean getRegister() {return register;}

    public void setRegister(Boolean register) {this.register = register;}
}
