package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class ItemImages {

    private String objectID;
    private List<String> listUrlImages;

    public ItemImages(){}

    public ItemImages(String objectID, List<String> listUrlImages) {
        this.objectID = objectID;
        this.listUrlImages = listUrlImages;
    }

    public String getObjectID() { return objectID; }

    public void setObjectID(String objectID) { this.objectID = objectID; }

    public List<String> getListUrlImages() { return listUrlImages; }

    public void setListUrlImages(List<String> listUrlImages) { this.listUrlImages = listUrlImages; }
}
