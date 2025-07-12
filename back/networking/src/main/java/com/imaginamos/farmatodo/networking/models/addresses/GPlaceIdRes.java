package com.imaginamos.farmatodo.networking.models.addresses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class GPlaceIdRes {

    @SerializedName("result")
    @Expose
    private Result result;

    private double latitude;
    private double longitude;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }


    public double getLatitude() {
        return (Objects.nonNull(result) && Objects.nonNull(result.getGeometry()) && Objects.nonNull(result.getGeometry().getLocation())) ?  result.getGeometry().getLocation().getLatitude() : 0d;
    }

    public double getLongitude() {
        return (Objects.nonNull(result) && Objects.nonNull(result.getGeometry()) && Objects.nonNull(result.getGeometry().getLocation())) ? result.getGeometry().getLocation().getLongitude() : 0d ;
    }

    public class Result {
        @SerializedName("geometry")
        @Expose
        private Geometry geometry;
        @SerializedName("name")
        @Expose
        private String name;

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private class Geometry {

            @SerializedName("location")
            @Expose
            private Location location;

            public Location getLocation() {
                return location;
            }

            public void setLocation(Location location) {
                this.location = location;
            }

            private class Location {

                @SerializedName("lat")
                @Expose
                private double latitude;
                @SerializedName("lng")
                @Expose
                private double longitude;

                public double getLatitude() {
                    return latitude;
                }

                public void setLatitude(double latitude) {
                    this.latitude = latitude;
                }

                public double getLongitude() {
                    return longitude;
                }

                public void setLongitude(double longitude) {
                    this.longitude = longitude;
                }
            }

        }
    }

    public String toStringJson() {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = null;
        try {
            json = ow.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
