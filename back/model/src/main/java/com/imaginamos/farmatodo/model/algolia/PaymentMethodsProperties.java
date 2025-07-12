package com.imaginamos.farmatodo.model.algolia;

import java.util.List;

public class PaymentMethodsProperties {

    private Boolean efectivo;
    private Boolean datafono;
    private Boolean enLinea;
    private Boolean pse;

    public Boolean getEfectivo() {
        return efectivo;
    }

    public void setEfectivo(Boolean efectivo) {
        this.efectivo = efectivo;
    }

    public Boolean getDatafono() {
        return datafono;
    }

    public void setDatafono(Boolean datafono) {
        this.datafono = datafono;
    }

    public Boolean getEnLinea() {
        return enLinea;
    }

    public void setEnLinea(Boolean enLinea) {
        this.enLinea = enLinea;
    }

    public Boolean getPse() {
        return pse;
    }

    public void setPse(Boolean pse) {
        this.pse = pse;
    }
}
