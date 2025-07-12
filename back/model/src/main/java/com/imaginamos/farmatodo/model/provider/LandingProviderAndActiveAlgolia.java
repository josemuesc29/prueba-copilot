package com.imaginamos.farmatodo.model.provider;

public class LandingProviderAndActiveAlgolia {
    private boolean active;
    private String objectID;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    @Override
    public String toString() {
        return "LandingProviderAndActiveAlgolia{" +
                "active=" + active +
                ", objectID='" + objectID + '\'' +
                '}';
    }
}
