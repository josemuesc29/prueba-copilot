package com.imaginamos.farmatodo.model.algolia;

import com.imaginamos.farmatodo.model.product.Item;

import java.util.List;

public class RecommendResponse {

    private List<Results> results;

    public List<Results> getResults() {
        return results;
    }

    public void setResults(List<Results> results) {
        this.results = results;
    }

    public class Results {
        private List<ItemAlgolia> hits;

        public List<ItemAlgolia> getHits() {
            return hits;
        }

        public void setHits(List<ItemAlgolia> hits) {
            this.hits = hits;
        }
    }
}
