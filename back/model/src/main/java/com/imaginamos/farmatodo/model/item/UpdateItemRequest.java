package com.imaginamos.farmatodo.model.item;

import java.io.Serializable;
import java.util.List;

/**
 * Created by JPuentes on 10/07/2018.
 */
public class UpdateItemRequest{

    private Integer id;
    private String barcode;
    private String brand;
    private String gray_description;
    private String media_description;
    private Boolean highlight;
    private String image_url;
    private String require_prescription;
    private String status;
    private String large_description;
    private Long[] categories;
    private Integer[] filters;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getGray_description() {
        return gray_description;
    }

    public void setGray_description(String gray_description) {
        this.gray_description = gray_description;
    }

    public String getMedia_description() {
        return media_description;
    }

    public void setMedia_description(String media_description) {
        this.media_description = media_description;
    }

    public Boolean getHighlight() {
        return highlight;
    }

    public void setHighlight(Boolean highlight) {
        this.highlight = highlight;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getRequire_prescription() {
        return require_prescription;
    }

    public void setRequire_prescription(String require_prescription) {
        this.require_prescription = require_prescription;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLarge_description() {
        return large_description;
    }

    public void setLarge_description(String large_description) {
        this.large_description = large_description;
    }

    public Long[] getCategories() {
        return categories;
    }

    public void setCategories(Long[] categories) {
        this.categories = categories;
    }

    public Integer[] getFilters() {
        return filters;
    }

    public void setFilters(Integer[] filters) {
        this.filters = filters;
    }
}
