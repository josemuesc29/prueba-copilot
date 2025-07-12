package com.imaginamos.farmatodo.model.monitor;



import java.util.List;

public class FilterOrder {
    private List<StoreFilter> stores;
    private List<CityFilter> cities;
    private List<OrderStatusFilter> orderStatusFilters;
    private List<CourierFilter> couriers;
    private List<PaymentMethodFilter> paymentMethods;
    private String optimalRoute;
    private String orderGuide;

    public List<StoreFilter> getStores() {
        return stores;
    }

    public void setStores(List<StoreFilter> stores) {
        this.stores = stores;
    }

    public List<CityFilter> getCities() {
        return cities;
    }

    public void setCities(List<CityFilter> cities) {
        this.cities = cities;
    }

    public List<OrderStatusFilter> getOrderStatusFilters() {
        return orderStatusFilters;
    }

    public void setOrderStatusFilters(List<OrderStatusFilter> orderStatusFilters) {
        this.orderStatusFilters = orderStatusFilters;
    }

    public List<CourierFilter> getCouriers() {
        return couriers;
    }

    public void setCouriers(List<CourierFilter> couriers) {
        this.couriers = couriers;
    }

    public List<PaymentMethodFilter> getPaymentMethods() {
        return paymentMethods;
    }

    public void setPaymentMethods(List<PaymentMethodFilter> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public String getOptimalRoute() {
        return optimalRoute;
    }

    public void setOptimalRoute(String optimalRoute) {
        this.optimalRoute = optimalRoute;
    }

    public String getOrderGuide() {
        return orderGuide;
    }

    public void setOrderGuide(String orderGuide) {
        this.orderGuide = orderGuide;
    }
}
