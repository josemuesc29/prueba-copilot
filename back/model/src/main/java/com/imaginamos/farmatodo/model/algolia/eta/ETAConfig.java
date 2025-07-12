package com.imaginamos.farmatodo.model.algolia.eta;

import java.util.List;

public class ETAConfig {

    private Integer standardDeliveryTimeInMinutes;
    private Integer standardAdditionalTimeInMinForEachStop;

    private Integer thresholdToAddMoreTimeWhenIsPickingAndTimeIsUp;
    private Integer standardAdditionalTimeWhenIsPickingAndTimeIsUp;

    private Integer thresholdToAddMoreTimeWhenNotBilledAndTimeIsUp;
    private Integer standardAdditionalTimeWhenNotBilledAndTimeIsUp;

    private List<Variable> variables;
    private List<VariableByCity> variablesCity;
    private List<About> about;
    private String objectID;

    public Integer getStandardDeliveryTimeInMinutes() {
        return standardDeliveryTimeInMinutes;
    }

    public void setStandardDeliveryTimeInMinutes(Integer standardDeliveryTimeInMinutes) {
        this.standardDeliveryTimeInMinutes = standardDeliveryTimeInMinutes;
    }

    public Integer getStandardAdditionalTimeInMinForEachStop() {
        return standardAdditionalTimeInMinForEachStop;
    }

    public void setStandardAdditionalTimeInMinForEachStop(Integer standardAdditionalTimeInMinForEachStop) {
        this.standardAdditionalTimeInMinForEachStop = standardAdditionalTimeInMinForEachStop;
    }

    public Integer getThresholdToAddMoreTimeWhenIsPickingAndTimeIsUp() {
        return thresholdToAddMoreTimeWhenIsPickingAndTimeIsUp;
    }

    public void setThresholdToAddMoreTimeWhenIsPickingAndTimeIsUp(Integer thresholdToAddMoreTimeWhenIsPickingAndTimeIsUp) {
        this.thresholdToAddMoreTimeWhenIsPickingAndTimeIsUp = thresholdToAddMoreTimeWhenIsPickingAndTimeIsUp;
    }

    public Integer getStandardAdditionalTimeWhenIsPickingAndTimeIsUp() {
        return standardAdditionalTimeWhenIsPickingAndTimeIsUp;
    }

    public void setStandardAdditionalTimeWhenIsPickingAndTimeIsUp(Integer standardAdditionalTimeWhenIsPickingAndTimeIsUp) {
        this.standardAdditionalTimeWhenIsPickingAndTimeIsUp = standardAdditionalTimeWhenIsPickingAndTimeIsUp;
    }

    public Integer getThresholdToAddMoreTimeWhenNotBilledAndTimeIsUp() {
        return thresholdToAddMoreTimeWhenNotBilledAndTimeIsUp;
    }

    public void setThresholdToAddMoreTimeWhenNotBilledAndTimeIsUp(Integer thresholdToAddMoreTimeWhenNotBilledAndTimeIsUp) {
        this.thresholdToAddMoreTimeWhenNotBilledAndTimeIsUp = thresholdToAddMoreTimeWhenNotBilledAndTimeIsUp;
    }

    public Integer getStandardAdditionalTimeWhenNotBilledAndTimeIsUp() {
        return standardAdditionalTimeWhenNotBilledAndTimeIsUp;
    }

    public void setStandardAdditionalTimeWhenNotBilledAndTimeIsUp(Integer standardAdditionalTimeWhenNotBilledAndTimeIsUp) {
        this.standardAdditionalTimeWhenNotBilledAndTimeIsUp = standardAdditionalTimeWhenNotBilledAndTimeIsUp;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public List<About> getAbout() {
        return about;
    }

    public void setAbout(List<About> about) {
        this.about = about;
    }

    public List<VariableByCity> getVariablesCity() {
        return variablesCity;
    }

    public void setVariablesCity(List<VariableByCity> variablesCity) {
        this.variablesCity = variablesCity;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }
}
