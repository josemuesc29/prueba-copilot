package com.imaginamos.farmatodo.model.algolia;

import java.util.ArrayList;
import java.util.List;

public class NotAllowedTips {
    private List<Long> customerId;
    private String description;

    public List<Long> getCustomerId() {
        return customerId;
    }

    public void setCustomerId(List<Long> customerId) {
        this.customerId = customerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
