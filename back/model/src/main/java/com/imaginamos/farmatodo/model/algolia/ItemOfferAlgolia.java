package com.imaginamos.farmatodo.model.algolia;

public class ItemOfferAlgolia {
    private String offerTextTwoForOne = "2x1";
    private String offerTextThreeForTwo = "3x2";
    private String offerTextFiveForFour = "5x4";

    public String getOfferTextTwoForOne() {
        return offerTextTwoForOne;
    }

    public void setOfferTextTwoForOne(String offerTextTwoForOne) {
        this.offerTextTwoForOne = offerTextTwoForOne;
    }

    public String getOfferTextThreeForTwo() {
        return offerTextThreeForTwo;
    }

    public void setOfferTextThreeForTwo(String offerTextThreeForTwo) {
        this.offerTextThreeForTwo = offerTextThreeForTwo;
    }

    public String getOfferTextFiveForFour() {
        return offerTextFiveForFour;
    }

    public void setOfferTextFiveForFour(String offerTextFiveForFour) {
        this.offerTextFiveForFour = offerTextFiveForFour;
    }

    public ItemOfferAlgolia(String offerTextTwoForOne, String offerTextThreeForTwo, String offerTextFiveForFour) {
        this.offerTextTwoForOne = offerTextTwoForOne;
        this.offerTextThreeForTwo = offerTextThreeForTwo;
        this.offerTextFiveForFour = offerTextFiveForFour;
    }
}
