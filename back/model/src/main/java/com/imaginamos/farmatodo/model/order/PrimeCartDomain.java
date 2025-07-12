package com.imaginamos.farmatodo.model.order;

public class PrimeCartDomain {

    private Double savings;
    private Double missings;

    public PrimeCartDomain() {
    }

    public PrimeCartDomain(Double savings, Double missings) {
        this.savings = savings;
        this.missings = missings;
    }

    public Double getSavings() {
        return savings;
    }

    public void setSavings(Double savings) {
        this.savings = savings;
    }

    public Double getMissings() {
        return missings;
    }

    public void setMissings(Double missings) {
        this.missings = missings;
    }

    @Override
    public String toString() {
        return "PrimeCartDomain{" +
                "savings=" + savings +
                ", missings=" + missings +
                '}';
    }
}
