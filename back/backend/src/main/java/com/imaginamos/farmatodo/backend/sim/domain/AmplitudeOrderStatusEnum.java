package com.imaginamos.farmatodo.backend.sim.domain;

public enum AmplitudeOrderStatusEnum {
    RECIBIDA("RECIBIDA"),
    EMITIDA("EMITIDA"),
    ENVIADA("ENVIADA"),
    ASIGNADA("ASIGNADA"),
    PICKING("PICKING"),
    FACTURADA("FACTURADA"),
    ENTREGADA("ENTREGADA"),
    FINALIZADA("FINALIZADA"),
    OCULTA("OCULTA"),
    PREPROCESADO("PREPROCESADO"),
    MODIFICADA("MODIFICADA"),
    ENVIADA_CON_ERROR("ENVIADA_CON_ERROR"),
    PAGADA("PAGADA"),
    EN_COLA_POR_PAGAR("EN_COLA_POR_PAGAR"),
    CANCELADA("CANCELADA");

    private final String emitida;

    AmplitudeOrderStatusEnum(String emitida) {
        this.emitida = emitida;
    }
}
