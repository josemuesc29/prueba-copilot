package com.imaginamos.farmatodo.model.order;


import com.google.api.server.spi.config.ApiResourceProperty;

/**
 * Created by ccrodriguez
 */
public class OrderInfoTracing {
    @ApiResourceProperty(name = "create_date")
    private long createDate;
    @ApiResourceProperty(name = "status")
    private long status;


    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

}
