package com.imaginamos.farmatodo.model.order;

public class OrderFinalized {
    private Long idOrder;
    private int status;

    public Long getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(Long idOrder) {
        this.idOrder = idOrder;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "OrderFinalized{" +
                "idOrder=" + idOrder +
                ", status=" + status +
                '}';
    }
}
