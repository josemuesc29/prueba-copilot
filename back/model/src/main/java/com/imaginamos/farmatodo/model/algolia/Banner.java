package com.imaginamos.farmatodo.model.algolia;

public class Banner {
    private String idBanner;
    private String urlBanner;
    private String redirectUrl;
    private String redirectId;
    private String redirectType;
    private String idWebSafeBanner;
    private int order;
    private boolean directionBanner = false;
    private boolean bannerWeb;
    private String campaignName;
    private String creative;
    private String position;
    private Long idCategory;
    private Integer classificationLevel;

    public Banner() {}

    public Banner(String idBanner, String urlBanner, String redirectUrl, String redirectId, String redirectType, String idWebSafeBanner, int order, boolean directionBanner, boolean bannerWeb, String campaignName, String creative, String position, Long idCategory) {
        this.idBanner = idBanner;
        this.urlBanner = urlBanner;
        this.redirectUrl = redirectUrl;
        this.redirectId = redirectId;
        this.redirectType = redirectType;
        this.idWebSafeBanner = idWebSafeBanner;
        this.order = order;
        this.directionBanner = directionBanner;
        this.bannerWeb = bannerWeb;
        this.campaignName = campaignName;
        this.creative = creative;
        this.position = position;
        this.idCategory = idCategory;
    }

    public Banner(String idBanner, String urlBanner, String redirectUrl, String redirectId, String redirectType, String idWebSafeBanner, int order, boolean directionBanner, boolean bannerWeb, String campaignName, String creative, String position, Long idCategory, Integer classificationLevel) {
        this(idBanner, urlBanner, redirectUrl, redirectId, redirectType, idWebSafeBanner, order, directionBanner, bannerWeb, campaignName, creative, position, idCategory);
        this.classificationLevel = classificationLevel;
    }

    public String getIdBanner() {
        return idBanner;
    }

    public void setIdBanner(String idBanner) {
        this.idBanner = idBanner;
    }

    public String getUrlBanner() {
        return urlBanner;
    }

    public void setUrlBanner(String urlBanner) {
        this.urlBanner = urlBanner;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isDirectionBanner() {
        return directionBanner;
    }

    public void setDirectionBanner(boolean directionBanner) {
        this.directionBanner = directionBanner;
    }

    public boolean isBannerWeb() {
        return bannerWeb;
    }

    public void setBannerWeb(boolean bannerWeb) {
        this.bannerWeb = bannerWeb;
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

    public Long getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Long idCategory) {
        this.idCategory = idCategory;
    }

    public Integer getClassificationLevel() { return classificationLevel; }

    public void setClassificationLevel(Integer classificationLevel) { this.classificationLevel = classificationLevel; }

    @Override
    public String toString() {
        return "Banner{" +
                "idBanner='" + idBanner + '\'' +
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
                ", idCategory=" + idCategory +
                '}';
    }
}
