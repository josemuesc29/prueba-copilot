package com.imaginamos.farmatodo.model.OptimalRoute;

public enum OptimalRouteVersionEnum {
    V1("v1"),
    V2("v2");

    private String version;

    OptimalRouteVersionEnum(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public static OptimalRouteVersionEnum fromString(String version) {
        for (OptimalRouteVersionEnum b : OptimalRouteVersionEnum.values()) {
            if (b.version.equalsIgnoreCase(version)) {
                return b;
            }
        }
        return null;
    }
}
