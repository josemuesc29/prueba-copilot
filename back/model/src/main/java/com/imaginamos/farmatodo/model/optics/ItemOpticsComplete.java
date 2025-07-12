package com.imaginamos.farmatodo.model.optics;

import java.io.Serializable;
import java.util.List;

public class ItemOpticsComplete implements Serializable {

    private ItemOptics itemOptics;
    private List<VisibleParameter> visibleParameters;

    private List<Long> idItemsList;

    private StepsParameters stepParameters;

    private AdditionalInformationOptics additionalInformation;


    public ItemOptics getItemOptics() {
        return itemOptics;
    }

    public void setItemOptics(ItemOptics itemOptics) {
        this.itemOptics = itemOptics;
    }

    public List<VisibleParameter> getVisibleParameters() {
        return visibleParameters;
    }

    public void setVisibleParameters(List<VisibleParameter> visibleParameters) {
        this.visibleParameters = visibleParameters;
    }

    public StepsParameters getStepParameters() {
        return stepParameters;
    }

    public void setStepParameters(StepsParameters stepParameters) {
        this.stepParameters = stepParameters;
    }

    public List<Long> getIdItemsList() {
        return idItemsList;
    }

    public void setIdItemsList(List<Long> idItemsList) {
        this.idItemsList = idItemsList;
    }

    public AdditionalInformationOptics getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(AdditionalInformationOptics additionalInformation) {
        this.additionalInformation = additionalInformation;
    }


    @Override
    public String toString() {
        return "ItemOpticsComplete{" +
                "itemOptics=" + itemOptics +
                ", visibleParameters=" + visibleParameters +
                ", idItemsList=" + idItemsList +
                ", stepsParameters=" + stepParameters +
                ", additionalInformation=" + additionalInformation +
                '}';
    }
}
