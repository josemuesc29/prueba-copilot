package com.imaginamos.farmatodo.networking.models.addresses;

import java.util.List;

public class KeyWordsCityConfig {
    private Boolean active;
    private List<KeyWord> keywords;

    public KeyWordsCityConfig(Boolean active, List<KeyWord> keywords) {
        this.active = active;
        this.keywords = keywords;
    }

    public KeyWordsCityConfig() {
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<KeyWord> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<KeyWord> keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "keyWordsCityConfig{" +
                "active=" + active +
                ", keywords=" + keywords +
                '}';
    }
}
