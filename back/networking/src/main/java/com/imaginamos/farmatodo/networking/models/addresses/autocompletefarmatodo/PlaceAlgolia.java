package com.imaginamos.farmatodo.networking.models.addresses.autocompletefarmatodo;

/**
 * Representación logica de https://www.algolia.com/apps/VCOJEYD2PO/explorer/browse/data_autocomplete
 * */
public class PlaceAlgolia {

    private String description;
    private String id;
    private String placeId;
    private String city;
    private String objectID;

    public PlaceAlgolia() {}

    public PlaceAlgolia(String description, String id, String placeId, String city, String objectID) {
        this.description = description;
        this.id = id;
        this.placeId = placeId;
        this.city = city;
        this.objectID = objectID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    @Override
    public String toString() {
        StringBuilder object = new StringBuilder();
        object.append("{\n");
        object.append("'description':'"+description+"'\n");
        object.append("'id':'"+id+"'\n");
        object.append("'placeId':'"+placeId+"'\n");
        object.append("'city':'"+city+"'\n");
        object.append("'objectID':'"+objectID+"'\n");
        object.append("}\n");
        return object.toString();
    }
}