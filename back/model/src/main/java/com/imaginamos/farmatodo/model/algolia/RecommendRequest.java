package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class RecommendRequest {

    private List<RequestRecommend> requests;

    public List<RequestRecommend> getRequests() {
        return requests;
    }

    public void setRequests(List<RequestRecommend> requests) {
        this.requests = requests;
    }
}
