package com.imaginamos.farmatodo.model.customer;

import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;

public class LoginRequest {

    private String keyClient;
    private String emailAddress;
    private String password;
    private double latitude;
    private double longitude;
    private String deviceId;
    private String firebaseTokenDevice;

    public String getKeyClient() {
        return keyClient;
    }

    public void setKeyClient(String keyClient) {
        this.keyClient = keyClient;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFirebaseTokenDevice() {
        return firebaseTokenDevice;
    }

    public void setFirebaseTokenDevice(String firebaseTokenDevice) {
        this.firebaseTokenDevice = firebaseTokenDevice;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "keyClient='" + keyClient + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", password='" + password + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", deviceId='" + deviceId + '\'' +
                ", firebaseTokenDevice='" + firebaseTokenDevice + '\'' +
                '}';
    }
}
