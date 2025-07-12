package com.imaginamos.farmatodo.networking.models.algolia;

import java.util.List;

public class ItemMultiqueryList {

    private List<Long> ids;
    private Long idStore;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public Long getIdStore() {
        return idStore;
    }

    public void setIdStore(Long idStore) {
        this.idStore = idStore;
    }
}
