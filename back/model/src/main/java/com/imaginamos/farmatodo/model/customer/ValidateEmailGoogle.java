package com.imaginamos.farmatodo.model.customer;

public class ValidateEmailGoogle {

    private int id;
    private Boolean result;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ValidateEmailGoogle{" +
                "id=" + id +
                ", result=" + result +
                '}';
    }
}
