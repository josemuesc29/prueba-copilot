package com.imaginamos.farmatodo.networking.models.braze;

public class AddNonStockItemBrazeRequest {

    private String email;
    private String itemId;

    public String getEmail() {
        return email;
    }

    public AddNonStockItemBrazeRequest(String email, String idItem) {
        this.email = email;
        this.itemId = idItem;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
