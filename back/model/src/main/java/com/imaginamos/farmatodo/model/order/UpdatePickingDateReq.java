package com.imaginamos.farmatodo.model.order;

import java.util.Date;

public class UpdatePickingDateReq {
    private Long orderId;
    private Date pickingDate;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Date getPickingDate() {
        return pickingDate;
    }

    public void setPickingDate(Date pickingDate) {
        this.pickingDate = pickingDate;
    }
}
