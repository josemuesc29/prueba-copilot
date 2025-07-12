package com.imaginamos.farmatodo.model.util;


/**
 */
public class BlobAttributes {
  /**
   * Entity that enables file uploads, saving all of its attributes.
   */
  private String BlobURL;
  private String BlobKey;
  private String ServingUrl;

  public void setBlobURL(String blobURL) {
    BlobURL = blobURL;
  }

  public String getBlobURL() {
    return BlobURL;
  }

  public String getBlobKey() {
    return BlobKey;
  }

  public void setBlobKey(String blobKey) {
    BlobKey = blobKey;
  }

  public String getServingUrl() {
    return ServingUrl;
  }

  public void setServingUrl(String urlUpload) {
    ServingUrl = urlUpload;
  }
}
