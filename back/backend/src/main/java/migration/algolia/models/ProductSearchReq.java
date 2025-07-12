package migration.algolia.models;

public class ProductSearchReq {

    private String indexName;
    private String objectID;

    public ProductSearchReq(String objectID) {
        this.indexName = "products";
        this.objectID = objectID;
    }

    public ProductSearchReq() {

    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
}
