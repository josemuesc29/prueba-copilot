package com.imaginamos.farmatodo.model.customer;
/*

 */

public class CustomerCreditCardPrimeData {
    private boolean active;
    private double savings;
    private double missingPurchase;
    private double minimumPurchase;
    private double savingUserPrime;


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

    public double getMissingPurchase() {
        return missingPurchase;
    }

    public void setMissingPurchase(double missingPurchase) {
        this.missingPurchase = missingPurchase;
    }

    public double getMinimumPurchase() {
        return minimumPurchase;
    }

    public void setMinimumPurchase(double minimumPurchase) {
        this.minimumPurchase = minimumPurchase;
    }

    public double getSavingUserPrime() {
        return savingUserPrime;
    }

    public void setSavingUserPrime(double savingUserPrime) {
        this.savingUserPrime = savingUserPrime;
    }
}
