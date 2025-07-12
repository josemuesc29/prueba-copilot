package com.imaginamos.farmatodo.model.algolia;

public class DeliveryLabelDetail {
    private Integer storeId;
    private String city;
    private String label;

    public Integer getStoreId() { return storeId; }

    public void setStoreId(Integer storeId) { this.storeId = storeId; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    @Override
    public String toString() {
        return "DeliveryLabelDetail{" +
                "storeId=" + storeId +
                ", city='" + city + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
