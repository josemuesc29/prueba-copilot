package com.imaginamos.farmatodo.networking.models.algolia;

import java.util.List;

public class OrderMessageConfiguration {

    private String turboMessage;
    private List<StatusMessageConfig> config;
    private boolean enable;
    private String objectID;
    private String title;
    private String qualifyTitle;
    private String qualifyMessage;

    private String rescheduleTitle;
    private String rescheduleMessage;
    private List<CancellationReason> cancellationReasons;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTurboMessage() {
        return turboMessage;
    }

    public void setTurboMessage(String turboMessage) {
        this.turboMessage = turboMessage;
    }

    public List<StatusMessageConfig> getConfig() {
        return config;
    }

    public void setConfig(List<StatusMessageConfig> config) {
        this.config = config;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getQualifyTitle() {
        return qualifyTitle;
    }

    public void setQualifyTitle(String qualifyTitle) {
        this.qualifyTitle = qualifyTitle;
    }

    public String getQualifyMessage() {
        return qualifyMessage;
    }

    public void setQualifyMessage(String qualifyMessage) {
        this.qualifyMessage = qualifyMessage;
    }


    public List<CancellationReason> getCancellationReasons() {
        return cancellationReasons;
    }

    public String getRescheduleTitle() {
        return rescheduleTitle;
    }

    public String getRescheduleMessage() {
        return rescheduleMessage;
    }

    public void setCancellationReasons(List<CancellationReason> cancellationReasons) {
        this.cancellationReasons = cancellationReasons;
    }
}
