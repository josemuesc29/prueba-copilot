package com.imaginamos.farmatodo.networking.talonone.model;

public class TriggeredCampaign {
    private Long id;
    private String name;

    public TriggeredCampaign(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public TriggeredCampaign() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
