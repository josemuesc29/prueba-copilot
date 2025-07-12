package com.imaginamos.farmatodo.model.algolia;

public class RequestRecommend {

    private String indexName;
    private String model;
    private String objectID;
    private double threshold;
    private String facetName;
    private String facetValue;
    private int maxRecommendations;
    private QueryParametersAlgoliaRecommend queryParameters;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public String getFacetName() {
        return facetName;
    }

    public void setFacetName(String facetName) {
        this.facetName = facetName;
    }

    public String getFacetValue() {
        return facetValue;
    }

    public void setFacetValue(String facetValue) {
        this.facetValue = facetValue;
    }

    public QueryParametersAlgoliaRecommend getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(QueryParametersAlgoliaRecommend queryParameters) {
        this.queryParameters = queryParameters;
    }

    public int getMaxRecommendations() {
        return maxRecommendations;
    }

    public void setMaxRecommendations(int maxRecommendations) {
        this.maxRecommendations = maxRecommendations;
    }
}
