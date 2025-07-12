package com.imaginamos.farmatodo.model.algolia;

public class ProductsForPrime {

    private Long year;
    private Long month;

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }

    public Long getMonth() {
        return month;
    }

    public void setMonth(Long month) {
        this.month = month;
    }

    @Override
    public String toString() {
        return "ProductsForPrime{" +
                "year=" + year +
                ", month=" + month +
                '}';
    }
}
