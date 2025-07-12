package com.imaginamos.farmatodo.model.customer;

import java.util.List;

public class GetCustomerCallCenterResponse {
    private String status;
    private String message;
    private List<CustomerCallCenterJSON> customers;

    public GetCustomerCallCenterResponse(String status, String message, List<CustomerCallCenterJSON> customers) {
        this.status = status;
        this.message = message;
        this.customers = customers;
    }

    public List<CustomerCallCenterJSON> getCustomers() {
        return customers;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
