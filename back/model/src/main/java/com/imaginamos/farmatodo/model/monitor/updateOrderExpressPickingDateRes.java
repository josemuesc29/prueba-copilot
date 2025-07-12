package com.imaginamos.farmatodo.model.monitor;

import java.util.List;

public class updateOrderExpressPickingDateRes {

  private List<TracingResponse> tracing;
  private String address;
  private boolean updateShopping;
  private Long id;
  private String createDate;

  public List<TracingResponse> getTracing() {
    return tracing;
  }

  public void setTracing(List<TracingResponse> tracing) {
    this.tracing = tracing;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public boolean isUpdateShopping() {
    return updateShopping;
  }

  public void setUpdateShopping(boolean updateShopping) {
    this.updateShopping = updateShopping;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCreateDate() {
    return createDate;
  }

  public void setCreateDate(String createDate) {
    this.createDate = createDate;
  }
}
