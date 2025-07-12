package main.java.com.imaginamos.farmatodo.model.order;

import java.util.List;

import com.imaginamos.farmatodo.model.provider.SupplierShippingCostMarketplace;

public class ShoppingCartCourierCostResp {

    private List<SupplierShippingCostMarketplace> supplierShippingCostResp;
    private double shippingCostTotal;

    public List<SupplierShippingCostMarketplace> getSupplierShippingCostResp() {
        return supplierShippingCostResp;
    }

    public void setSupplierShippingCostResp(List<SupplierShippingCostMarketplace> supplierShippingCostResp) {
        this.supplierShippingCostResp = supplierShippingCostResp;
    }

    public double getShippingCostTotal() {
        return shippingCostTotal;
    }

    public void setShippingCostTotal(double shippingCostTotal) {
        this.shippingCostTotal = shippingCostTotal;
    }
}