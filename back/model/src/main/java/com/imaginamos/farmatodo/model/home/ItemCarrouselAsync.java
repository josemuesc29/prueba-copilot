package com.imaginamos.farmatodo.model.home;

import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;

import java.util.List;

public class ItemCarrouselAsync {

    private List<ItemAlgolia> favorites;
    private List<ItemAlgolia> purchases;
    private List<ItemAlgolia> viewed;

    public List<ItemAlgolia> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<ItemAlgolia> favorites) {
        this.favorites = favorites;
    }

    public List<ItemAlgolia> getPurchases() {
        return purchases;
    }

    public void setPurchases(List<ItemAlgolia> purchases) {
        this.purchases = purchases;
    }

    public List<ItemAlgolia> getViewed() {
        return viewed;
    }

    public void setViewed(List<ItemAlgolia> viewed) {
        this.viewed = viewed;
    }
}
