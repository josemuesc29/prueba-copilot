package com.imaginamos.farmatodo.model.product;

import java.util.List;

public class RequestUpdateHighlightSuggested {

    private List<ItemUpdateHighlightSuggested> items;

    public List<ItemUpdateHighlightSuggested> getItems() {
        return items;
    }

    public void setItems(List<ItemUpdateHighlightSuggested> items) {
        this.items = items;
    }
}
