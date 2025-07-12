package com.imaginamos.farmatodo.model.algolia;

import java.util.Date;

public class ReminderAlgolia {

    private long itemId;
    private Date expiration;

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

}
