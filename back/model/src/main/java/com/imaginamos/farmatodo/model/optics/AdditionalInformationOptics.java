package com.imaginamos.farmatodo.model.optics;

import java.io.Serializable;
import java.util.List;

public class AdditionalInformationOptics implements Serializable {

    private List<AdditionalInformation> additionalInformationList;


    public List<AdditionalInformation> getAdditionalInformationList() {
        return additionalInformationList;
    }

    public void setAdditionalInformationList(List<AdditionalInformation> additionalInformationList) {
        this.additionalInformationList = additionalInformationList;
    }
}
