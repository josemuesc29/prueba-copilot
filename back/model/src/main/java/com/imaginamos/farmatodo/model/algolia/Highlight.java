package com.imaginamos.farmatodo.model.algolia;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Highlight {
    public final static String OBJECT_ID ="HIGHLIGHT.";

    private String objectID;
    private Long id;
    private String firstDescription;
    private String secondDescription;
    private String offerDescription;
    private String type;
    private String urlImage;
    private Date startDate;
    private Date endDate;
    private Integer orderingNumber;
    private Boolean highlightHead;
    private Long item;
    private List<Long> items;
    private List<Long> product;
    private List<Integer> categories;
    private String offerText;

    // TODO creado para que sea incluido en las busquedas
    private String mediaDescription;

    public Highlight() {
    }

    public Highlight(Long id, String firstDescription, String secondDescription, String offerDescription,String offerText,
                     String type, String urlImage, Date startDate, Date endDate, Integer orderingNumber,
                     List<Long> items, Long item, List<Long> product, List<Integer> categories) {
        this.id = id;
        this.firstDescription = firstDescription;
        this.secondDescription = secondDescription;
        this.offerDescription = offerDescription;
        this.type = type;
        this.urlImage = urlImage;
        this.startDate = startDate;
        this.endDate = endDate;
        this.orderingNumber = orderingNumber;
        this.item = item;
        this.items = items;
        this.product = product;
        this.categories = categories;
        this.highlightHead = Boolean.TRUE;
        this.offerText = offerText;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getObjectID() {
        return OBJECT_ID+this.id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstDescription() {
        return firstDescription;
    }

    public void setFirstDescription(String firstDescription) {
        this.firstDescription = firstDescription;
    }

    public String getSecondDescription() {
        return secondDescription;
    }

    public void setSecondDescription(String secondDescription) {
        this.secondDescription = secondDescription;
    }

    public String getOfferDescription() {
        return offerDescription;
    }

    public void setOfferDescription(String offerDescription) {
        this.offerDescription = offerDescription;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getOrderingNumber() {
        return orderingNumber;
    }

    public void setOrderingNumber(Integer orderingNumber) {
        this.orderingNumber = orderingNumber;
    }

    public Long getItem() {
        return item;
    }

    public void setItem(Long item) {
        this.item = item;
    }

    public List<Long> getItems() {
        return items;
    }

    public void setItems(List<Long> items) {
        this.items = items;
    }

    public List<Long> getProduct() {
        return product;
    }

    public void setProduct(List<Long> product) {
        this.product = product;
    }

    public List<Integer> getCategories() {
        return categories;
    }

    public void setCategories(List<Integer> categories) {
        this.categories = categories;
    }

    public String getMediaDescription() {
        return Objects.nonNull(this.firstDescription) ? this.firstDescription : this.offerDescription;
    }

    public Boolean getHighlightHead() {
        return highlightHead;
    }

    public void setHighlightHead(Boolean highlightHead) {
        this.highlightHead = highlightHead;
    }

    public String getOfferText() {
        return offerText;
    }

    public void setOfferText(String offerText) {
        this.offerText = offerText;
    }

    @Override
    public String toString() {
        return "Highlight{" +
                "objectID='" + objectID + '\'' +
                ", id=" + id +
                ", firstDescription='" + firstDescription + '\'' +
                ", secondDescription='" + secondDescription + '\'' +
                ", offerDescription='" + offerDescription +
                ", offerText=" + offerText +
                ", type='" + type + '\'' +
                ", urlImage='" + urlImage + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", orderingNumber=" + orderingNumber +
                ", highlightHead=" + highlightHead +
                ", item=" + item +
                ", items=" + items +
                ", product=" + product +
                ", categories=" + categories +
                ", mediaDescription='" + mediaDescription + '\'' +
                '}';
    }
}
