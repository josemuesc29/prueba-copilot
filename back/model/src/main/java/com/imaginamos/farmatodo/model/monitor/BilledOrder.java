package com.imaginamos.farmatodo.model.monitor;

import java.util.List;

public class BilledOrder {
    private double totalValue;
    private String billId;
    private String billDate;
    private String codigo;
    private String mensaje;
    private List<BilledOrderDetail> listDetail;

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getBillDate() {
        return billDate;
    }

    public void setBillDate(String billDate) {
        this.billDate = billDate;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<BilledOrderDetail> getListDetail() {
        return listDetail;
    }

    public void setListDetail(List<BilledOrderDetail> listDetail) {
        this.listDetail = listDetail;
    }
}
