package com.imaginamos.farmatodo.model.algolia;

import java.util.HashMap;

public class Scores {
    private HashMap<String, Integer> genero;
    private HashMap<String, Integer> marca;
    private HashMap<String, Integer> rms_class;

    private HashMap<String, Integer> Categoría;

    private HashMap<String, Integer> departments;

    public HashMap<String, Integer> getGenero() {
        return genero;
    }

    public void setGenero(HashMap<String, Integer> genero) {
        this.genero = genero;
    }

    public HashMap<String, Integer> getMarca() {
        return marca;
    }

    public void setMarca(HashMap<String, Integer> marca) {
        this.marca = marca;
    }

    public HashMap<String, Integer> getRms_class() {
        return rms_class;
    }

    public void setRms_class(HashMap<String, Integer> rms_class) {
        this.rms_class = rms_class;
    }

    public HashMap<String, Integer> getCategoría() {
        return Categoría;
    }

    public void setCategoría(HashMap<String, Integer> categoría) {
        Categoría = categoría;
    }

    public HashMap<String, Integer> getDepartments() {
        return departments;
    }

    public void setDepartments(HashMap<String, Integer> departments) {
        this.departments = departments;
    }
}
