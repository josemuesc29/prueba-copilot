package com.imaginamos.farmatodo.model.algolia;

/**
 * Created by JPuentes on 18/10/2018.
 */
public enum AlgoliaIndexEnum {

    PRODUCTS_PROD("VCOJEYD2PO","95b4b2df9c92207e52d472b1b66db8c8","products"),
    PRODUCTS_COLOMBIA_PROD("VCOJEYD2PO","95b4b2df9c92207e52d472b1b66db8c8","products-colombia"),
    PRODUCTS_DEV("VCOJEYD2PO","95b4b2df9c92207e52d472b1b66db8c8","products_dev"),
    FARMATODO_CHOICE_PROD("VCOJEYD2PO","95b4b2df9c92207e52d472b1b66db8c8","farmatodo_choice"),
    ADVISED_ITEMS("VCOJEYD2PO","95b4b2df9c92207e52d472b1b66db8c8","advised_items");

    private final String appID;
    private final String apiKey;
    private final String indexName;

    private AlgoliaIndexEnum(String appID,String apiKey,String indexName) {
        this.appID     = appID;
        this.apiKey    = apiKey;
        this.indexName = indexName;
    }

    public String getAppID() {
        return appID;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getIndexName() {
        return indexName;
    }
}
