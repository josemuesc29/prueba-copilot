package com.imaginamos.farmatodo.model.monitor;

import java.util.List;

public class OrderActived {
    private Long id;
    private String createDate;
    private String address;
    private String updateShopping;
    private List<OrderActivedTracing> tracing;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUpdateShopping() {
        return updateShopping;
    }

    public void setUpdateShopping(String updateShopping) {
        this.updateShopping = updateShopping;
    }

    public List<OrderActivedTracing> getTracing() {
        return tracing;
    }

    public void setTracing(List<OrderActivedTracing> tracing) {
        this.tracing = tracing;
    }
}
