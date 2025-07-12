/*
 * Farmatodo Colombia
 * Copyrigth 2017
 */
package com.imaginamos.farmatodo.model.order;

/**
 * [description]
 *
 * @author: Diego Poveda <diego.poveda@farmatodo.com>
 * @version: 1.0
 * @since: 1.0
 */
public enum CoreEventResponseCode {

  OK,
  NO_CONTENT,
  BAD_REQUEST,
  BUSSINESS_ERROR,
  APPLICATION_ERROR,
  UNAUTHORIZED,
  CONFLICT,
  INVALID_TOKEN,
  TOKEN_REQUIRED,
  TOKEN_EXPIRED,
  INVALID_CLIENT_ID,
  INVALID_CLIENT_SECRET,
  PAYMENT_APROVED,
  PAYMENT_DECLINED,
  PAYMENT_REPEAT_TRANSACTION,
  PAYMENT_DAILY_ATTEMPTS_EXCEEDED,
  PAYMENT_DUPLICATED,
  PAYMENT_ERROR,
  REJECTED,
  DUPLICATED,
  ERROR,
  CREATED;
}
