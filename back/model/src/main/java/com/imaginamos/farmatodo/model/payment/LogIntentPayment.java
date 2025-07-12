package com.imaginamos.farmatodo.model.payment;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class LogIntentPayment {

    @Id
    private String id;
    @Index
    private Long orderId;
    @Index
    private Long createdDate;
    @Index
    private String status;

    private String message;

    private String uuid;

    public LogIntentPayment() { }

    public LogIntentPayment(Long orderId, String uuid, Long createdDate, String status) {
        this.id = orderId+"_"+uuid;
        this.orderId = orderId;
        this.createdDate = createdDate;
        this.uuid = uuid;
        this.status = status;
    }

    public LogIntentPayment(Long orderId, String uuid, Long createdDate, String status, String message) {
        this(orderId, uuid, createdDate, status);
        this.message = message;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Long getOrderId() { return orderId; }

    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getCreatedDate() { return createdDate; }

    public void setCreatedDate(Long createdDate) { this.createdDate = createdDate; }

    public String getStatus() { return status;  }

    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid; }

    @Override
    public String toString() {
        return "LogIntentPayment{" +
                "id='" + id + '\'' +
                ", orderId=" + orderId +
                ", createdDate=" + createdDate +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
