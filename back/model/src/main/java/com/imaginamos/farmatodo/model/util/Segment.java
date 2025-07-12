package com.imaginamos.farmatodo.model.util;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by mileniopc on 10/26/16.
 * Property of Imaginamos.
 */

@Entity
public class Segment {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idSegment;
  @Index
  private int id;
  @Index
  private String name;
  @IgnoreSave
  private int segment;
  @IgnoreSave
  private String idSegmentWebSafe;

  public String getIdSegment() {
    return idSegment;
  }

  public void setIdSegment(String idSegment) {
    this.idSegment = idSegment;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getSegment() {
    return segment;
  }

  public void setSegment(int segment) {
    this.segment = segment;
  }

  public String getIdSegmentWebSafe() {
    return idSegmentWebSafe;
  }

  public void setIdSegmentWebSafe(String idSegmentWebSafe) {
    this.idSegmentWebSafe = idSegmentWebSafe;
  }
}
