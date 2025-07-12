package com.imaginamos.farmatodo.model.order;

import com.google.api.server.spi.config.ApiResourceProperty;

public class DeliveryOrderStatus {

    private String uuid;
    @ApiResourceProperty(name = "order_no")
    private String order_no;
    private String status;
    private String messenger;
    private String phone;
    private String url;
    @ApiResourceProperty(name = "payment_terms")
    private String paymentTerms;
    @ApiResourceProperty(name = "payment_means")
    private String paymentMeans;
    @ApiResourceProperty(name = "delivery_terms")
    private String deliveryTerms;

    private Long payment;

    public DeliveryOrderStatus(){}

    public DeliveryOrderStatus(String order_no, String uuid, Long payment){
        this.order_no = order_no;
        this.uuid = uuid;
        this.payment = payment;
    }

    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getMessenger() { return messenger; }

    public void setMessenger(String messenger) { this.messenger = messenger; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = url; }

    public String getPaymentTerms() { return paymentTerms; }

    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }

    public String getPaymentMeans() { return paymentMeans; }

    public void setPaymentMeans(String paymentMeans) { this.paymentMeans = paymentMeans; }

    public String getDeliveryTerms() { return deliveryTerms; }

    public void setDeliveryTerms(String deliveryTerms) { this.deliveryTerms = deliveryTerms; }

    public String getOrder_no() { return order_no; }

    public void setOrder_no(String order_no) { this.order_no = order_no; }

    public Long getPayment() { return payment; }

    public void setPayment(Long payment) { this.payment = payment; }

    @Override
    public String toString() {
        return "DeliveryOrderStatus{" +
                "uuid='" + uuid + '\'' +
                ", orderNo='" + order_no + '\'' +
                ", status='" + status + '\'' +
                ", messenger='" + messenger + '\'' +
                ", phone='" + phone + '\'' +
                ", url='" + url + '\'' +
                ", paymentTerms=" + paymentTerms +
                ", paymentMeans=" + paymentMeans +
                ", deliveryTerms=" + deliveryTerms +
                ", payment=" + payment +
                '}';
    }
}
