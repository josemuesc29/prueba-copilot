package com.imaginamos.farmatodo.model.algolia;


public class CategorySeo {
    private String textoSEO;
    private String objectID;

    public CategorySeo(){}

    public CategorySeo(String objectID, String textoSEO){
        this.objectID = objectID;
        this.textoSEO = textoSEO;
    }

    public String getTextoSEO() {
        return textoSEO;
    }

    public void setTextoSEO(String textoSEO) {
        this.textoSEO = textoSEO;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
}
