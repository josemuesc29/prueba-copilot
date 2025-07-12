package com.imaginamos.farmatodo.model.payment;

public enum OrderStatusEnum {
    RECIBIDA(0L),
    EMITIDA(1L),
    ENVIADA(2L),
    ASIGNADA(3L),
    PICKING(4L),
    FACTURADA(5L),
    ENTREGADA(6L),
    FINALIZADA(7L),
    OCULTA(8L),
    PREPROCESADO(9L),
    MODIFICADA(10L),
    ENVIADA_CON_ERROR(11L),
    PAGADA(12L),
    EN_COLA_POR_PAGAR(13L),
    CANCELADA(14L),
    DEVOLUCION(53L),
    DEVOLUCION_EXITOSA(54L);

    private final Long id;

    OrderStatusEnum(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static OrderStatusEnum getOrderStatusEnum(String status) {
        if (status == null) {
            return null;
        }

        status = status.toUpperCase();

        switch (status) {
            case "RECIBIDA":
                return RECIBIDA;
            case "EMITIDA":
                return EMITIDA;
            case "ENVIADA":
                return ENVIADA;
            case "ASIGNADA":
                return ASIGNADA;
            case "ASSIGNED":
                return ASIGNADA;
            case "PICKING":
                return PICKING;
            case "FACTURADA":
                return FACTURADA;
            case "ENTREGADA":
                return ENTREGADA;
            case "DELIVERED":
                return ENTREGADA;
            case "FINISHED":
                return FINALIZADA;
            case "FINALIZADA":
                return FINALIZADA;
            case "OCULTA":
                return OCULTA;
            case "PREPROCESADO":
                return PREPROCESADO;
            case "MODIFICADA":
                return MODIFICADA;
            case "ENVIADA_CON_ERROR":
                return ENVIADA_CON_ERROR;
            case "PAGADA":
                return PAGADA;
            case "EN_COLA_POR_PAGAR":
                return EN_COLA_POR_PAGAR;
            case "CANCELADA":
                return CANCELADA;
            case "CANCEL":
                return CANCELADA;
            case "DEVOLUCION":
                return DEVOLUCION;
            case "DEVOLUCION_EXITOSA":
                return DEVOLUCION_EXITOSA;
            default:
                return null;
        }
    }

    public static Long getId(String status) {
        if (status == null) {
            return null;
        }

        status = status.toUpperCase();

        switch (status) {
            case "RECIBIDA":
                return RECIBIDA.getId();
            case "EMITIDA":
                return EMITIDA.getId();
            case "ENVIADA":
                return ENVIADA.getId();
            case "ASIGNADA":
                return ASIGNADA.getId();
            case "ASSIGNED":
                return ASIGNADA.getId();
            case "PICKING":
                return PICKING.getId();
            case "FACTURADA":
                return FACTURADA.getId();
            case "ENTREGADA":
                return ENTREGADA.getId();
            case "DELIVERED":
                return ENTREGADA.getId();
            case "FINALIZADA":
                return FINALIZADA.getId();
            case "FINISHED":
                return FINALIZADA.getId();
            case "OCULTA":
                return OCULTA.getId();
            case "PREPROCESADO":
                return PREPROCESADO.getId();
            case "MODIFICADA":
                return MODIFICADA.getId();
            case "ENVIADA_CON_ERROR":
                return ENVIADA_CON_ERROR.getId();
            case "PAGADA":
                return PAGADA.getId();
            case "EN_COLA_POR_PAGAR":
                return EN_COLA_POR_PAGAR.getId();
            case "CANCELADA":
                return CANCELADA.getId();
            case "CANCEL":
                return CANCELADA.getId();
            case "DEVOLUCION":
                return DEVOLUCION.getId();
            case "DEVOLUCION_EXITOSA":
                return DEVOLUCION_EXITOSA.getId();
            default:
                return null;
        }
    }
}
