package com.imaginamos.farmatodo.model.order;

import java.util.ArrayList;
import java.util.List;

public class CreateOrderSubscribeData {
    private int id;
    private Long createDate;
    private String address;
    private boolean updateShopping;
    private boolean changePaymentCreditCard;

    private List<Tracing> tracing = new ArrayList<>();


    public CreateOrderSubscribeData() {
    }

    public class Tracing {

        private int id;
        private Long createDate;
        private String comments;
        private Long cancellationReason;
        private Long courier;
        private int status;

        public Tracing() {
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Long getCreateDate() {
            return createDate;
        }

        public void setCreateDate(Long createDate) {
            this.createDate = createDate;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }

        public Long getCancellationReason() {
            return cancellationReason;
        }

        public void setCancellationReason(Long cancellationReason) {
            this.cancellationReason = cancellationReason;
        }

        public Long getCourier() {
            return courier;
        }

        public void setCourier(Long courier) {
            this.courier = courier;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isUpdateShopping() {
        return updateShopping;
    }

    public void setUpdateShopping(boolean updateShopping) {
        this.updateShopping = updateShopping;
    }

    public List<Tracing> getTracing() {
        return tracing;
    }

    public void setTracing(List<Tracing> tracing) {
        this.tracing = tracing;
    }

    public boolean isChangePaymentCreditCard() {
        return changePaymentCreditCard;
    }

    public void setChangePaymentCreditCard(boolean changePaymentCreditCard) {
        this.changePaymentCreditCard = changePaymentCreditCard;
    }

    @Override
    public String toString() {
        return "CreateOrderSubscribeData:{" +
                "id=" + id +
                ", createDate=" + createDate +
                ", address='" + address + '\'' +
                ", updateShopping=" + updateShopping +
                ", changePaymentCreditCard=" + changePaymentCreditCard +
                '}';
    }
}
