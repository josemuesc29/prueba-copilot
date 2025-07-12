package com.imaginamos.farmatodo.model.order;

import java.util.List;

public class RatingResponseData {

    private List<Rating> orderReviewDomain;

    public List<Rating> getOrderReviewDomain() {
        return orderReviewDomain;
    }

    public void setOrderReviewDomain(List<Rating> orderReviewDomain) {
        this.orderReviewDomain = orderReviewDomain;
    }
}