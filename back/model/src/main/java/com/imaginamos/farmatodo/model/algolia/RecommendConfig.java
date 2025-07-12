package com.imaginamos.farmatodo.model.algolia;

public class RecommendConfig {

    private String objectID;
    private boolean optimalRoute;
    private boolean departmentsCarrousel;
    private boolean advisedItems;

    private boolean suggestedItemsFlag;

    private boolean departmentsAfinity;
    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public boolean isOptimalRoute() {
        return optimalRoute;
    }

    public void setOptimalRoute(boolean optimalRoute) {
        this.optimalRoute = optimalRoute;
    }

    public boolean isDepartmentsCarrousel() {
        return departmentsCarrousel;
    }

    public void setDepartmentsCarrousel(boolean departmentsCarrousel) {
        this.departmentsCarrousel = departmentsCarrousel;
    }

    public boolean isAdvisedItems() {
        return advisedItems;
    }

    public void setAdvisedItems(boolean advisedItems) {
        this.advisedItems = advisedItems;
    }

    public boolean isSuggestedItemsFlag() {
        return suggestedItemsFlag;
    }

    public void setSuggestedItemsFlag(boolean suggestedItemsFlag) {
        this.suggestedItemsFlag = suggestedItemsFlag;
    }

    public boolean isDepartmentsAfinity() {
        return departmentsAfinity;
    }

    public void setDepartmentsAfinity(boolean departmentsAfinity) {
        this.departmentsAfinity = departmentsAfinity;
    }
}
