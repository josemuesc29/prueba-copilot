package com.imaginamos.farmatodo.model.order;

import org.apache.http.HttpStatus;

public class SendOrderStatus {
    private HttpStatus status;

    public HttpStatus getStatus() { return status; }

    public void setStatus(HttpStatus status) { this.status = status; }
}
