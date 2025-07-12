package com.imaginamos.farmatodo.model.OptimalRoute;

public class ItemPercentageCompleteness {
    private int itemId;
    private float percentage;

    public ItemPercentageCompleteness(int itemId, float percentage) {
        this.itemId = itemId;
        this.percentage = percentage;
    }

    public ItemPercentageCompleteness() {
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        if (itemId <= 0) {
            throw new IllegalArgumentException("itemId must be positive");
        }
        this.itemId = itemId;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("percentage must be between 0 and 100");
        }
        this.percentage = percentage;
    }
}
