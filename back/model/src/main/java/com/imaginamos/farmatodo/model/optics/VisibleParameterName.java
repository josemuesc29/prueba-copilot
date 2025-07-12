package com.imaginamos.farmatodo.model.optics;

public enum VisibleParameterName {

    POWER("Poder"),
    CYLINDER("Cilindro"),
    AXLE("Eje"),
    ADDITION("Adición"),
    LENSCOLOR("Color");

    VisibleParameterName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

   private String name;

}
