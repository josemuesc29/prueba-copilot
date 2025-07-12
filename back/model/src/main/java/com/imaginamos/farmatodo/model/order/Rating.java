package com.imaginamos.farmatodo.model.order;

import java.util.List;

/**
 * Created by diego.poveda on 11/12/2017.
 */

public class Rating {

  private Integer id;
  private String name;
  private String description;
  private String imageURL;
  private List<Assessment> reviewAssesmentList;

  public Rating() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getImageURL() {
    return imageURL;
  }

  public void setImageURL(String imageURL) {
    this.imageURL = imageURL;
  }

  public List<Assessment> getReviewAssesmentList() {
    return reviewAssesmentList;
  }

  public void setReviewAssesmentList(List<Assessment> reviewAssesmentList) {
    this.reviewAssesmentList = reviewAssesmentList;
  }
}
