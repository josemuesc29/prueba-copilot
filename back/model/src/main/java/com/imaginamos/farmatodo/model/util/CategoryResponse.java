package com.imaginamos.farmatodo.model.util;

import java.util.ArrayList;
import java.util.List;

public class CategoryResponse {

    private String status; //Success, Fail

    private List<Long> updatedCategories;

    private List<Long> failedCategories;

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public List<Long> getFailedCategories() { return failedCategories; }

    public void setFailedCategories(List<Long> failedCategories) { this.failedCategories = failedCategories; }

    public List<Long> getUpdatedCategories() { return updatedCategories; }

    public void setUpdatedCategories(List<Long> updatedCategories) { this.updatedCategories = updatedCategories; }

    public void addCategoryUpdated(Long category){
        if(updatedCategories == null){
            updatedCategories = new ArrayList<>();
        }
        updatedCategories.add(category);
    }

    public void addCategoryFail(Long category){
        if(failedCategories == null){
            failedCategories = new ArrayList<>();
        }
        failedCategories.add(category);
    }
}
