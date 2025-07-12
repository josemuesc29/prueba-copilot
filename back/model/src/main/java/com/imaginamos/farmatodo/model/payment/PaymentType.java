package com.imaginamos.farmatodo.model.payment;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.imaginamos.farmatodo.model.customer.CreditCard;

import java.util.List;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class PaymentType {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idPaymentType;
  @Index
  private long id;
  private String description;
  @Index
  private int positionIndex;
  private String showPayments; //Pendiente
  @Index
  private boolean status;
  @IgnoreSave
  private String idPaymentTypeWebSafe;
  @IgnoreSave
  private CreditCard creditCard;
  @IgnoreSave
  private List<CreditCard> creditCards;
  @IgnoreSave
  private String name;

  @IgnoreSave
  private PSEResponse pse;

  public String getIdPaymentType() {
    return idPaymentType;
  }

  public void setIdPaymentType(String idPaymentType) {
    this.idPaymentType = idPaymentType;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getPositionIndex() {
    return positionIndex;
  }

  public void setPositionIndex(int positionIndex) {
    this.positionIndex = positionIndex;
  }

  public String getShowPayments() {
    return showPayments;
  }

  public void setShowPayments(String showPayments) {
    this.showPayments = showPayments;
  }

  public boolean getStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public String getIdPaymentTypeWebSafe() {
    return idPaymentTypeWebSafe;
  }

  public void setIdPaymentTypeWebSafe(String idPaymentTypeWebSafe) {
    this.idPaymentTypeWebSafe = idPaymentTypeWebSafe;
  }

  public CreditCard getCreditCard() {
    return creditCard;
  }

  public void setCreditCard(CreditCard creditCard) {
    this.creditCard = creditCard;
  }

  public List<CreditCard> getCreditCards() {
    return creditCards;
  }

  public void setCreditCards(List<CreditCard> creditCards) {
    this.creditCards = creditCards;
  }

  public PSEResponse getPse() {
    return pse;
  }

  public void setPse(PSEResponse pse) {
    this.pse = pse;
  }

  public String getName() { return name; }

  public void setName(String name) { this.name = name; }
}
