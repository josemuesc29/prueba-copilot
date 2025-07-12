package com.imaginamos.farmatodo.model.customer;

import java.io.Serializable;

public class CustomerResetPasswordReq implements Serializable {
    private String email;

    private Long customerId;
    private String oldPassword;
    private String newPassword;

    public CustomerResetPasswordReq(){}

    public CustomerResetPasswordReq(String email){
        this.email = email;
    }

    public CustomerResetPasswordReq(Long customerId, String oldPassword, String newPassword) {
        this.customerId = customerId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public CustomerResetPasswordReq(Long customerId, String newPassword) {
        this.customerId = customerId;
        this.newPassword = newPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
