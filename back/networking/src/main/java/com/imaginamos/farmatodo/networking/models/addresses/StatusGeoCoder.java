package com.imaginamos.farmatodo.networking.models.addresses;

public enum StatusGeoCoder {


    A("A","Aproximado con geo al 100%",true),

    B("B","Normalizado y Georreferenciado Exacto", true),

    C("C","Intraducible",false ),

    D("D","Normalizado y georreferenciado aproximado",true),

    E("E","Normalizado y no georreferenciado",false),

    F("F","Normalizado por cruce y georreferenciado exacto",true),

    G("G","Normalizado por cruce y no georreferenciado",false),

    H("H","Atípicas",false),

    I("I","Normalizado por barrio georreferenciado",true),

    J("J","Normalizado por barrio no georreferenciado",false),

    K("K","Georreferenciado a centroide de barrio",true),

    L("L","Normalizado por sitio y georreferenciado",true),

    M("M","Normalizado y georreferenciado por predio",true),

    N("N","Normalizado y georreferenciado por predio MZ",true),

    O("O","Georreferenciado a centroide de localidad / comuna",true),

    R("R","Direcciones Rurales",false),

    W("W","Apartados aéreos",false),

    X("X","Ciudad disponible no adquirida",false),

    Y("Y","Georreferenciado por predio aproximado",true),

    Z("Z","Ubicado por centroide del centro poblado",true),

    ;

    private String status;
    private String description;
    private boolean haveCoordinates;

    StatusGeoCoder(String status, String description, boolean haveCoordinates) {
        this.status = status;
        this.description = description;
        this.haveCoordinates = haveCoordinates;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHaveCoordinates() {
        return haveCoordinates;
    }

    public void setHaveCoordinates(boolean haveCoordinates) {
        this.haveCoordinates = haveCoordinates;
    }
}
