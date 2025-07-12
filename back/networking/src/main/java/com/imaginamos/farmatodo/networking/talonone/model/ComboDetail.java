package com.imaginamos.farmatodo.networking.talonone.model;

import java.util.List;

public class ComboDetail {
    private String comboSku;
    private int Quantity;
    private List<ItemCombo> itemsCombo;

    // Constructor
    public ComboDetail(String comboSku, List<ItemCombo> itemsCombo) {
        this.comboSku = comboSku;
        this.itemsCombo = itemsCombo;
    }

    // Getters y Setters
    public String getComboSku() {
        return comboSku;
    }

    public void setComboSku(String comboSku) {
        this.comboSku = comboSku;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int Quantity) {
        this.Quantity = Quantity;
    }

    public List<ItemCombo> getItemsCombo() {
        return itemsCombo;
    }

    public void setItemsCombo(List<ItemCombo> itemsCombo) {
        this.itemsCombo = itemsCombo;
    }
}