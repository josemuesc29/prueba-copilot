package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class GroupSorted {

    public GroupSorted(){}

    public GroupSorted(List<ItemAlgoliaSort> items){ this.items = items; }

    public List<ItemAlgoliaSort> items;

    public List<ItemAlgoliaSort> getItems() {
        return items;
    }

    public void setItems(List<ItemAlgoliaSort> items) {
        this.items = items;
    }
}
