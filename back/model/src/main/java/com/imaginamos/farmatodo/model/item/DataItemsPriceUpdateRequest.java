package com.imaginamos.farmatodo.model.item;

import java.util.List;

public class DataItemsPriceUpdateRequest {
    private List<ItemPriceUpload> ListItemsPriceUpload;

    public List<ItemPriceUpload> getListItemsPriceUpload() {
        return ListItemsPriceUpload;
    }

    public void setListItemsPriceUpload(List<ItemPriceUpload> listItemsPriceUpload) {
        ListItemsPriceUpload = listItemsPriceUpload;
    }
}
