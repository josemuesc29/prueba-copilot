package com.imaginamos.farmatodo.model.order;

public enum OptionSelectPopUpEnum {
    ONLY_AVAILABLE_UNITS("ONLY_AVAILABLE_UNITS"),
    REPLACE_WITH_SIMILAR_PRODUCTS("REPLACE_WITH_SIMILAR_PRODUCTS"),
    WAIT_TRANSFER_FOR_ALL_PRODUCTS("WAIT_TRANSFER_FOR_ALL_PRODUCTS");

    private String optionSelectPopUp;

    OptionSelectPopUpEnum(String optionSelectPopUp) {
        this.optionSelectPopUp = optionSelectPopUp;
    }

    public String getOptionSelectPopUp() {
        return optionSelectPopUp;
    }

    public void setOptionSelectPopUp(String optionSelectPopUp) {
        this.optionSelectPopUp = optionSelectPopUp;
    }
}
