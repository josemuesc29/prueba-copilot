package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class SelfCheckoutAlgolia {
    private List<SelfCheckoutListAlgolia> customerByStoresAndBox;
    private Long customerDefault;
    private String infoConfig;
    private String objectID;

    public List<SelfCheckoutListAlgolia> getCustomerByStoresAndBox() {
        return customerByStoresAndBox;
    }

    public void setCustomerByStoresAndBox(List<SelfCheckoutListAlgolia> customerByStoresAndBox) {
        this.customerByStoresAndBox = customerByStoresAndBox;
    }

    public Long getCustomerDefault() {
        return customerDefault;
    }

    public void setCustomerDefault(Long customerDefault) {
        this.customerDefault = customerDefault;
    }

    public String getInfoConfig() {
        return infoConfig;
    }

    public void setInfoConfig(String infoConfig) {
        this.infoConfig = infoConfig;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
}
