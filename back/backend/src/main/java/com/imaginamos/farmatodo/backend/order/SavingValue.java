package com.imaginamos.farmatodo.backend.order;

public class SavingValue {
    public double saving;
    public double total;

    public SavingValue(double totalPrime, double total) {
        this.saving = totalPrime - total;
        this.total = total;
    }

    public double getSaving() {
        return saving;
    }

    public void setSaving(double saving) {
        this.saving = saving;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
