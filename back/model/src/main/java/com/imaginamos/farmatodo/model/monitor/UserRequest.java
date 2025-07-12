package com.imaginamos.farmatodo.model.monitor;

public class UserRequest {
    private String employeeNumber;
    private String password;
    private String token;
    private String oldPassword;
    private String newPassword;
    private String employeeName;
    private long storeId;
    private String rolUser;
    private String email;
    private String tokenFirebaseAuth;
    private String tokenFirebasePush;

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public String getRolUser() {
        return rolUser;
    }

    public void setRolUser(String rolUser) {
        this.rolUser = rolUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTokenFirebaseAuth() {
        return tokenFirebaseAuth;
    }

    public void setTokenFirebaseAuth(String tokenFirebaseAuth) {
        this.tokenFirebaseAuth = tokenFirebaseAuth;
    }

    public String getTokenFirebasePush() {
        return tokenFirebasePush;
    }

    public void setTokenFirebasePush(String tokenFirebasePush) {
        this.tokenFirebasePush = tokenFirebasePush;
    }
}
