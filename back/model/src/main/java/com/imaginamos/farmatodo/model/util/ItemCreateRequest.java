package com.imaginamos.farmatodo.model.util;

import com.imaginamos.farmatodo.model.item.Item;
import com.imaginamos.farmatodo.model.item.StoreInformation;

import java.util.List;

/**
 * Created by JPuentes on 20/06/2018.
 */
public class ItemCreateRequest {

    private Item item;
    private List<StoreInformation> storeInformation = null;
    private List<Integer> filterList = null;
    private List<Long> subCategories = null;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<StoreInformation> getStoreInformation() {
        return storeInformation;
    }

    public void setStoreInformation(List<StoreInformation> storeInformation) {
        this.storeInformation = storeInformation;
    }

    public List<Integer> getFilterList() {
        return filterList;
    }

    public void setFilterList(List<Integer> filterList) {
        this.filterList = filterList;
    }

    public List<Long> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<Long> subCategories) {
        this.subCategories = subCategories;
    }


}
