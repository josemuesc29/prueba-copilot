package com.imaginamos.farmatodo.networking.models.amplitude;

public class Event <T>{

    private UserPropertiesOrder user_Properties;
    private String user_id;
    private String device_id;
    private String event_type;
    private String platform;
    private Long time;
    private T event_properties;

    public UserPropertiesOrder getUser_Properties() {
        return user_Properties;
    }

    public void setUser_Properties(UserPropertiesOrder user_Properties) {
        this.user_Properties = user_Properties;
    }

    public String getUser_id() { return user_id; }

    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getDevice_id() { return device_id; }

    public void setDevice_id(String device_id) { this.device_id = device_id; }

    public String getEvent_type() { return event_type; }

    public void setEvent_type(String event_type) { this.event_type = event_type; }

    public Long getTime() { return time; }

    public void setTime(Long time) { this.time = time; }

    public T getEvent_properties() { return event_properties; }

    public void setEvent_properties(T event_properties) { this.event_properties = event_properties; }

    public String getPlatform() { return platform; }

    public void setPlatform(String platform) { this.platform = platform; }
}
