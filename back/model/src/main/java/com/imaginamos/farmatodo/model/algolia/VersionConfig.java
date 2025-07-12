package com.imaginamos.farmatodo.model.algolia;

public class VersionConfig {

    private String platform;
    private String version;
    private String componentType;

    public String getPlatform() { return platform; }

    public void setPlatform(String platform) { this.platform = platform; }

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }

    public String getComponentType() { return componentType; }

    public void setComponentType(String componentType) { this.componentType = componentType; }
}
