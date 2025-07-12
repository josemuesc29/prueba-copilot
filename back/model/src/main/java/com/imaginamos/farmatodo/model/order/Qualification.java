package com.imaginamos.farmatodo.model.order;

import java.util.List;

/**
 * Created by diego.poveda on 11/12/2017.
 */

public class Qualification {

  private Integer orderId;
  private Integer ratingId;
  private String comments;
  private List<Assessment> assessments;

  public Qualification() {
  }

  public Integer getOrderId() {
    return orderId;
  }

  public void setOrderId(Integer orderId) {
    this.orderId = orderId;
  }

  public Integer getRatingId() {
    return ratingId;
  }

  public void setRatingId(Integer ratingId) {
    this.ratingId = ratingId;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public List<Assessment> getAssessments() {
    return assessments;
  }

  public void setAssessments(List<Assessment> assessments) {
    this.assessments = assessments;
  }
}
