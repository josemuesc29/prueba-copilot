package com.imaginamos.farmatodo.networking.models.addresses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GGeoCodeRes {
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "GGeoCodeRes{" +
                "results=" + results +
                '}';
    }

    public static class Result {
        private String formatted_address;
        private Geometry geometry;

        public String getFormattedAddress() {
            return formatted_address;
        }

        public void setFormattedAddress(String formatted_address) {
            this.formatted_address = formatted_address;
        }

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "formatted_address='" + formatted_address + '\'' +
                    ", geometry=" + geometry +
                    '}';
        }

        public static class Geometry {
            private Location location;

            public Location getLocation() {
                return location;
            }

            public void setLocation(Location location) {
                this.location = location;
            }

            @Override
            public String toString() {
                return "Geometry{" +
                        "location=" + location +
                        '}';
            }

            public static class Location {
                private double lat;
                private double lng;

                public double getLat() {
                    return lat;
                }

                public void setLat(double lat) {
                    this.lat = lat;
                }

                public double getLng() {
                    return lng;
                }

                public void setLng(double lng) {
                    this.lng = lng;
                }

                @Override
                public String toString() {
                    return "Location{" +
                            "lat=" + lat +
                            ", lng=" + lng +
                            '}';
                }
            }
        }
    }
}
