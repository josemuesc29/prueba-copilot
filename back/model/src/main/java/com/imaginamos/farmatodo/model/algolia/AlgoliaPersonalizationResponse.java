package com.imaginamos.farmatodo.model.algolia;


public class AlgoliaPersonalizationResponse {

    private String userToken;

    private Scores scores;

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public Scores getScores() {
        return scores;
    }

    public void setScores(Scores scores) {
        this.scores = scores;
    }


}
