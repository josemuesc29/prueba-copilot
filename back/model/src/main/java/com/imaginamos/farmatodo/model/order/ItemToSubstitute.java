package com.imaginamos.farmatodo.model.order;

import com.imaginamos.farmatodo.model.product.Substitutes;

import java.util.List;

public class ItemToSubstitute {

    private Integer itemId;
    private Integer minQuantity;
    private Integer missingQuantity;
    private List<Substitutes> substitutesByList;

    public ItemToSubstitute(Integer itemId, Integer minQuantity, List<Substitutes> substitutesByList) {
        this.itemId = itemId;
        this.minQuantity = minQuantity;
        this.substitutesByList = substitutesByList;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public List<Substitutes> getSubstitutesByList() {
        return substitutesByList;
    }

    public void setSubstitutesByList(List<Substitutes> substitutesByList) {
        this.substitutesByList = substitutesByList;
    }

    public Integer getMissingQuantity() {
        return missingQuantity;
    }

    public void setMissingQuantity(Integer missingQuantity) {
        this.missingQuantity = missingQuantity;
    }
}
