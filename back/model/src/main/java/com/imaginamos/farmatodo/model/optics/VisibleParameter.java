package com.imaginamos.farmatodo.model.optics;


import java.io.Serializable;
import java.util.List;

public class VisibleParameter implements Serializable {

    private String name;

    private String labelWeb;

    private String labelApp;

    private List<Object> list;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getList() {
        return list;
    }

    public void setList(List<Object> list) {
        this.list = list;
    }

    public String getLabelWeb() {
        return labelWeb;
    }

    public void setLabelWeb(String labelWeb) {
        this.labelWeb = labelWeb;
    }

    public String getLabelApp() {
        return labelApp;
    }

    public void setLabelApp(String labelApp) {
        this.labelApp = labelApp;
    }
}
