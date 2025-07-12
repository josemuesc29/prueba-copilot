package com.imaginamos.farmatodo.model.item;

import com.imaginamos.farmatodo.model.util.Constants;

public class ItemReq {
    private String source;
    private Long token;
    private Integer page;
    private Integer revision;

    private String barcode;
    private Long storeId;

    public ItemReq() {
    }

    public ItemReq(String source, Long token, Integer page) {
        this.source = source;
        this.token = token;
        this.page = page;
    }

    public ItemReq(String source, Integer revision) {
        this.source = source;
        this.revision = revision;
    }

    public ItemReq(String source, Long token) {
        this.source = source;
        this.token = token;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getToken() {
        return token;
    }

    public void setToken(Long token) {
        this.token = token;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }
}
