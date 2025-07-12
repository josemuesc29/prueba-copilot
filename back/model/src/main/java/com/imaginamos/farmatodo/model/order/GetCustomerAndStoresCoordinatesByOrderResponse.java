package com.imaginamos.farmatodo.model.order;

public class GetCustomerAndStoresCoordinatesByOrderResponse {

    private String code;
    private String message;
    private GetCoodinatesCustomerAndAddressByOrderResponseData data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GetCoodinatesCustomerAndAddressByOrderResponseData getData() {
        return data;
    }

    public void setData(GetCoodinatesCustomerAndAddressByOrderResponseData data) {
        this.data = data;
    }
}
