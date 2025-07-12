package com.imaginamos.farmatodo.model.algolia.autocomplete;

public class City {

    private String id;
    private Boolean active;
    private String latitude;
    private String longitude;
    private Boolean useStrictBounds;
    private String radiusInMeters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public boolean isUseStrictBounds() {
        return useStrictBounds;
    }

    public void setUseStrictBounds(boolean useStrictBounds) {
        this.useStrictBounds = useStrictBounds;
    }

    public String getRadiusInMeters() {
        return radiusInMeters;
    }

    public void setRadiusInMeters(String radiusInMeters) {
        this.radiusInMeters = radiusInMeters;
    }

    public String getLocation() {
        return this.latitude + "," + this.longitude;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "City : {" +
                "id='" + id + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", useStrictBounds=" + useStrictBounds +
                ", radiusInMeters='" + radiusInMeters + '\'' +
                '}';
    }
}
