package com.imaginamos.farmatodo.model.photoSlurp;

public class PhotoSlurpConfigAlgolia {

    private Integer albumId;
    private String titlePhotoSlurp;
    private String titlePhotoSlurpGrid;
    private String description;
    private String filterProductIdHome;
    private String filterCategoryHome;
    private String objectId;

    public PhotoSlurpConfigAlgolia() {
    }

    public Integer getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Integer albumId) {
        this.albumId = albumId;
    }

    public String getTitlePhotoSlurp() {
        return titlePhotoSlurp;
    }

    public void setTitlePhotoSlurp(String titlePhotoSlurp) {
        this.titlePhotoSlurp = titlePhotoSlurp;
    }

    public String getTitlePhotoSlurpGrid() {
        return titlePhotoSlurpGrid;
    }

    public void setTitlePhotoSlurpGrid(String titlePhotoSlurpGrid) {
        this.titlePhotoSlurpGrid = titlePhotoSlurpGrid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilterProductIdHome() {
        return filterProductIdHome;
    }

    public void setFilterProductIdHome(String filterProductIdHome) {
        this.filterProductIdHome = filterProductIdHome;
    }

    public String getFilterCategoryHome() {
        return filterCategoryHome;
    }

    public void setFilterCategoryHome(String filterCategoryHome) {
        this.filterCategoryHome = filterCategoryHome;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }
}
