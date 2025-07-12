package com.imaginamos.farmatodo.networking.models.amplitude;

import java.util.List;

public class BusinessOrder {

    private List<String> cclass;
    private List<String> departments;
    private List<String> divisions;
    private List<String> groups;
    private List<String> providers;
    private List<String> subclass;

    public List<String> getCclass() { return cclass; }

    public void setCclass(List<String> cclass) { this.cclass = cclass; }

    public List<String> getDepartments() { return departments; }

    public void setDepartments(List<String> departments) { this.departments = departments; }

    public List<String> getDivisions() { return divisions; }

    public void setDivisions(List<String> divisions) { this.divisions = divisions; }

    public List<String> getGroups() { return groups; }

    public void setGroups(List<String> groups) { this.groups = groups; }

    public List<String> getProviders() { return providers; }

    public void setProviders(List<String> providers) { this.providers = providers; }

    public List<String> getSubclass() { return subclass; }

    public void setSubclass(List<String> subclass) { this.subclass = subclass; }
}
