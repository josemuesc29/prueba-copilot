package com.imaginamos.farmatodo.model.item;

public enum EyeDirectionEnum {
    SAME("derecho e izquierdo"),
    RIGHT("derecho"),
    LEFT("izquierdo");



    EyeDirectionEnum(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    private final String name;
}
