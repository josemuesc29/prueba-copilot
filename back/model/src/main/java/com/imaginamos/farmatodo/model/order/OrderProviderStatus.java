package com.imaginamos.farmatodo.model.order;

import com.google.api.server.spi.config.ApiResourceProperty;

public class OrderProviderStatus {
    @ApiResourceProperty(name = "order")
    private long order;
    @ApiResourceProperty(name = "status")
    private long status;
    @ApiResourceProperty(name = "url_tracking")
    private String urlTracking;

    public long getOrder() { return order; }

    public void setOrder(long order) { this.order = order; }

    public long getStatus() { return status; }

    public void setStatus(long status) { this.status = status; }

    public String getUrlTracking() { return urlTracking; }

    public void setUrlTracking(String urlTracking) { this.urlTracking = urlTracking; }

    @Override
    public String toString() {
        return "OrderProviderStatus{" +
                "order=" + order +
                ", status=" + status +
                ", urlTracking='" + urlTracking + '\'' +
                '}';
    }
}
