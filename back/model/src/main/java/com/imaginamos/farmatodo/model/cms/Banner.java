package com.imaginamos.farmatodo.model.cms;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;
import com.imaginamos.farmatodo.model.categories.Department;

/**
 * Created by mileniopc on 11/30/16.
 * Property of Imaginamos.
 */

@Entity
public class Banner {
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Id
  private String idBanner;
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  @Parent
  private Ref<Department> classificationLevel1Ref;
  private String urlBanner;
  private String redirectUrl;
  private String redirectId;
  private String redirectType;
  private String idWebSafeBanner;
  private int order;
  @Index
  private boolean directionBanner = false;
  @Index
  private boolean bannerWeb;
  private String campaignName;
  private String creative;
  private String position;

  public String getIdBanner() {
    return idBanner;
  }

  public void setIdBanner(String idBanner) {
    this.idBanner = idBanner;
  }

  public String getUrlBanner() {
    return urlBanner;
  }

  public Ref<Department> getClassificationLevel1Ref() {
    return classificationLevel1Ref;
  }

  public void setClassificationLevel1Ref(Ref<Department> classificationLevel1Ref) {
    this.classificationLevel1Ref = classificationLevel1Ref;
  }

  public void setDirectionBanner(boolean directionBanner) {
    this.directionBanner = directionBanner;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public boolean isDirectionBanner() {
    return directionBanner;
  }

  public void setUrlBanner(String urlBanner) {
    this.urlBanner = urlBanner;
  }

  public String getRedirectId() {
    return redirectId;
  }

  public void setRedirectId(String redirectId) {
    this.redirectId = redirectId;
  }

  public String getRedirectType() {
    return redirectType;
  }

  public void setRedirectType(String redirectType) {
    this.redirectType = redirectType;
  }

  public String getIdWebSafeBanner() {
    return idWebSafeBanner;
  }

  public void setIdWebSafeBanner(String idWebSafeBanner) {
    this.idWebSafeBanner = idWebSafeBanner;
  }

  public boolean getBannerWeb() {
    return bannerWeb;
  }

  public void setBannerWeb(boolean bannerWeb) {
    this.bannerWeb = bannerWeb;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public String getCampaignName() {
    return campaignName;
  }

  public void setCampaignName(String campaignName) {
    this.campaignName = campaignName;
  }

  public String getCreative() {
    return creative;
  }

  public void setCreative(String creative) {
    this.creative = creative;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  @Override
  public String toString() {
    return "Banner{" +
            "idBanner='" + idBanner + '\'' +
            ", classificationLevel1Ref=" + classificationLevel1Ref +
            ", urlBanner='" + urlBanner + '\'' +
            ", redirectUrl='" + redirectUrl + '\'' +
            ", redirectId='" + redirectId + '\'' +
            ", redirectType='" + redirectType + '\'' +
            ", idWebSafeBanner='" + idWebSafeBanner + '\'' +
            ", order=" + order +
            ", directionBanner=" + directionBanner +
            ", bannerWeb=" + bannerWeb +
            ", campaignName='" + campaignName + '\'' +
            ", creative='" + creative + '\'' +
            ", position='" + position + '\'' +
            '}';
  }
}
