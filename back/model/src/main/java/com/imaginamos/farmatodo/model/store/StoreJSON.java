package com.imaginamos.farmatodo.model.store;

import com.imaginamos.farmatodo.model.location.Net;
import com.imaginamos.farmatodo.model.util.DeliveryType;

import java.math.BigDecimal;
import java.util.List;

public class StoreJSON {

    private Long id;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String city;
    private short status;
    private String operationStartTime;
    private String operationEndTime;
    private String photo;
    private String phone;
    private String scheduleMsn;
    private DeliveryType deliveryType;
    private List<Net> nets;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    public String getOperationStartTime() {
        return operationStartTime;
    }

    public void setOperationStartTime(String operationStartTime) {
        this.operationStartTime = operationStartTime;
    }

    public String getOperationEndTime() {
        return operationEndTime;
    }

    public void setOperationEndTime(String operationEndTime) {
        this.operationEndTime = operationEndTime;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getScheduleMsn() {
        return scheduleMsn;
    }

    public void setScheduleMsn(String scheduleMsn) {
        this.scheduleMsn = scheduleMsn;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public List<Net> getNets() {
        return nets;
    }

    public void setNets(List<Net> nets) {
        this.nets = nets;
    }
}