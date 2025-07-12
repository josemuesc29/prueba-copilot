package com.imaginamos.farmatodo.networking.models.amplitude;


import com.google.appengine.repackaged.com.google.gson.Gson;

import java.util.List;

public class EventRequest {

    List<Event> events;

    public List<Event> getEvents() { return events; }

    public void setEvents(List<Event> events) { this.events = events; }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
