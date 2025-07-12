/*
 * Farmatodo Colombia
 * Copyrigth 2018
 */
package com.imaginamos.farmatodo.model.favorite;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.user.User;

import java.util.List;

/**
 * [description]
 *
 * @author: Diego Poveda <diego.poveda@farmatodo.com>
 * @version: 1.0
 * @since: 1.0
 */
public class FavoriteRequest {

  private String idCustomerWebSafe;
  private List<Integer> items;

  public FavoriteRequest() {
  }

  public String getIdCustomerWebSafe() {
    return idCustomerWebSafe;
  }

  public void setIdCustomerWebSafe(String idCustomerWebSafe) {
    this.idCustomerWebSafe = idCustomerWebSafe;
  }

  public List<Integer> getItems() {
    return items;
  }

  public void setItems(List<Integer> items) {
    this.items = items;
  }
}
