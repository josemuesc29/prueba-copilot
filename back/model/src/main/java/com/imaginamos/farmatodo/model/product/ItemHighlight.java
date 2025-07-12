package com.imaginamos.farmatodo.model.product;

import java.util.List;

public class ItemHighlight {

    private String objectID;
    private List<String> id_highlights;

    public ItemHighlight() {}

    public ItemHighlight(String objectID, List<String> id_highlights) {
        this.objectID = objectID;
        this.id_highlights = id_highlights;
    }

    public String getObjectID() { return objectID; }

    public void setObjectID(String objectID) { this.objectID = objectID; }

    public List<String> getId_highlights() { return id_highlights; }

    public void setId_highlights(List<String> id_highlights) { this.id_highlights = id_highlights; }
}
