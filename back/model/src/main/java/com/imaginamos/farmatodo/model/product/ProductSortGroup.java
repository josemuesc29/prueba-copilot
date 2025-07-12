package com.imaginamos.farmatodo.model.product;

import com.imaginamos.farmatodo.model.algolia.ItemAlgoliaSort;

import java.util.List;

public class ProductSortGroup {
    private Long idGroup;
    private List<ItemAlgoliaSort> items;

    public Long getIdGroup() { return idGroup; }

    public void setIdGroup(Long idGroup) { this.idGroup = idGroup; }

    public List<ItemAlgoliaSort> getItems() { return items; }

    public void setItems(List<ItemAlgoliaSort> items) { this.items = items; }
}
