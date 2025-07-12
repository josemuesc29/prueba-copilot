package com.imaginamos.farmatodo.model.algolia;

public class ItemSeoConfigAlgolia {

    private String htmlSeo;
    private String cssSeo;
    private String objectID;
    private String htmlVademecum;
    private String cssVademecum;

    public String getHtmlSeo() {
        return htmlSeo;
    }

    public void setHtmlSeo(String htmlSeo) {
        this.htmlSeo = htmlSeo;
    }

    public String getCssSeo() {
        return cssSeo;
    }

    public void setCssSeo(String cssSeo) {
        this.cssSeo = cssSeo;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getHtmlVademecum() { return htmlVademecum; }

    public void setHtmlVademecum(String htmlVademecum) { this.htmlVademecum = htmlVademecum; }

    public String getCssVademecum() { return cssVademecum; }

    public void setCssVademecum(String cssVademecum) { this.cssVademecum = cssVademecum; }

    @Override
    public String toString() {
        return "ItemSeoAlgolia{" +
                "htmlSeo='" + htmlSeo + '\'' +
                ", cssSeo='" + cssSeo + '\'' +
                ", objectID='" + objectID + '\'' +
                '}';
    }
}
