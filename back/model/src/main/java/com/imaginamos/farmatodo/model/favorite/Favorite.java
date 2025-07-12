/*
 * Farmatodo Colombia
 * Copyrigth 2018
 */
package com.imaginamos.farmatodo.model.favorite;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.imaginamos.farmatodo.model.customer.Customer;

/**
 * [description]
 *
 * @author: Diego Poveda <diego.poveda@farmatodo.com>
 * @version: 1.0
 * @since: 1.0
 */
@Entity
public class Favorite {

  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String favoriteId;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Index
  private Key<Customer> customerKey;
  @Index
  private Integer itemId;

  public String getFavoriteId() {
    return favoriteId;
  }

  public void setFavoriteId(String favoriteId) {
    this.favoriteId = favoriteId;
  }

  public Key<Customer> getCustomerKey() {
    return customerKey;
  }

  public void setCustomerKey(Key<Customer> customerKey) {
    this.customerKey = customerKey;
  }

  public Integer getItemId() {
    return itemId;
  }

  public void setItemId(Integer itemId) {
    this.itemId = itemId;
  }
}
