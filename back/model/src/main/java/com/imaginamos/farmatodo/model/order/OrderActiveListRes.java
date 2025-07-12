package com.imaginamos.farmatodo.model.order;

import java.util.List;

public class OrderActiveListRes {
    private List<OrderActiveResponse> data;

    /*public List<OrderActiveResponse> getData(List<Long> ids) {
        return data;
    }

    public void setData(List<OrderActiveResponse> data) {
        this.data = data;
    }*/

    public List<OrderActiveResponse> getData() {
        return data;
    }

    public void setData(List<OrderActiveResponse> data) {
        this.data = data;
    }
}
