package com.imaginamos.farmatodo.model.photoSlurp;

public class PhotoSlurp {

     private Integer albumId;
     private String filterProductId;
     private String filterCategory;
     private String title;
     private String description;

     public PhotoSlurp(Integer albumId, String filterProductId, String filterCategory, String title, String description) {
          this.albumId = albumId;
          this.filterProductId = filterProductId;
          this.filterCategory = filterCategory;
          this.title = title;
          this.description = description;
     }

     public PhotoSlurp() {
     }

     public Integer getAlbumId() {
          return albumId;
     }

     public void setAlbumId(Integer albumId) {
          this.albumId = albumId;
     }

     public String getFilterProductId() {
          return filterProductId;
     }

     public void setFilterProductId(String filterProductId) {
          this.filterProductId = filterProductId;
     }

     public String getFilterCategory() {
          return filterCategory;
     }

     public void setFilterCategory(String filterCategory) {
          this.filterCategory = filterCategory;
     }

     public String getTitle() {
          return title;
     }

     public void setTitle(String title) {
          this.title = title;
     }

     public String getDescription() {
          return description;
     }

     public void setDescription(String description) {
          this.description = description;
     }
}
