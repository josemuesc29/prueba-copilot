package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class QueryParametersAlgoliaRecommend {
    private List<List<String>> facetFilters;

    public List<List<String>> getFacetFilters() {
        return facetFilters;
    }

    public void setFacetFilters(List<List<String>> facetFilters) {
        this.facetFilters = facetFilters;
    }
}
