/*
 * Farmatodo Colombia
 * Copyrigth 2017
 */
package com.imaginamos.farmatodo.model.order;

import com.google.appengine.repackaged.com.google.gson.Gson;

/**
 * [description]
 *
 * @author: Diego Poveda <diego.poveda@farmatodo.com>
 * @version: 1.0
 * @since: 1.0
 */
public class CoreEventResponse {

  private CoreEventResponseCode code;
  private String message;

  public CoreEventResponse() {
  }

  public CoreEventResponseCode getCode() {
    return code;
  }

  public void setCode(CoreEventResponseCode code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
