package com.imaginamos.farmatodo.model.algolia;

public class ItemSeoAlgolia {

    private String textoSEO;
    private String textoSEOApps;
    private String objectID;
    private String textoVademecum;

    public String getTextoSEO() {
        return textoSEO;
    }

    public void setTextoSEO(String textoSEO) {
        this.textoSEO = textoSEO;
    }

    public String getTextoSEOApps() {
        return textoSEOApps;
    }

    public void setTextoSEOApps(String textoSEOApps) {
        this.textoSEOApps = textoSEOApps;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getTextoVademecum() { return textoVademecum; }

    public void setTextoVademecum(String textoVademecum) { this.textoVademecum = textoVademecum; }

    @Override
    public String toString() {
        return "ItemSeoAlgolia{" +
                "textoSEO='" + textoSEO + '\'' +
                ", textoSEOApps='" + textoSEOApps + '\'' +
                ", objectID='" + objectID + '\'' +
                '}';
    }
}
