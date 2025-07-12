package com.imaginamos.farmatodo.model.customer;

public class CustomerResponseCart {
    public boolean active;
    public double savings;
    public double missing_purchase;
    public double minimum_purchase;
    public double savingUserPrime;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getSavings() {
        return savings;
    }

    public void setSavings(double savings) {
        this.savings = savings;
    }

    public double getMissing_purchase() {
        return missing_purchase;
    }

    public void setMissing_purchase(double missing_purchase) {
        this.missing_purchase = missing_purchase;
    }

    public double getMinimum_purchase() {
        return minimum_purchase;
    }

    public void setMinimum_purchase(double minimum_purchase) {
        this.minimum_purchase = minimum_purchase;
    }

    public double getSavingUserPrime() {
        return savingUserPrime;
    }

    public void setSavingUserPrime(double savingUserPrime) {
        this.savingUserPrime = savingUserPrime;
    }

    //generate toString
    @Override
    public String toString() {
        return "CustomerResponseCart{" +
                "active=" + active +
                ", savings=" + savings +
                ", missing_purchase=" + missing_purchase +
                ", minimum_purchase=" + minimum_purchase +
                '}';
    }
}
