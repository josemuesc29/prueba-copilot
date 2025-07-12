package com.imaginamos.farmatodo.model.order;

import com.imaginamos.farmatodo.model.customer.CreditCard;
import com.imaginamos.farmatodo.model.payment.PSEResponse;

import java.util.List;

public class PSEResponseCode {
    private String code;
    private String message;
    private PSEResponse data;

    public PSEResponseCode(PSEResponse allPSE) {
        this.data = allPSE;
    }

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

    public PSEResponse getData() {
        return data;
    }

    public void setData(PSEResponse data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "PSEResponseCode{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
