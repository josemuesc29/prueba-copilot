package com.imaginamos.farmatodo.backend.sim.domain;


import com.google.api.server.spi.response.BadRequestException;
import com.imaginamos.farmatodo.model.order.FulfilOrdColDescDomain;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Jhon Chaparro
 * @since 2022
 */
public class SimEndpointGuard {
    private static final Logger LOG = Logger.getLogger(SimEndpointGuard.class.getName());

    public static void validationDomain(FulfilOrdColDescDomain fulfilOrdColDescDomain) throws BadRequestException {
        if (Objects.isNull(fulfilOrdColDescDomain)) {
//            LOG.info("method: stockAlgolia() --> BadRequest [fulfilOrdColDescDomain is null]");
            throw new BadRequestException("BadRequest [fulfilOrdColDescDomain is null]");
        } else if (Objects.isNull(fulfilOrdColDescDomain.getFulfilOrdDesc()) || fulfilOrdColDescDomain.getFulfilOrdDesc().length == 0) {
//            LOG.info("method: stockAlgolia() --> BadRequest [fulfilOrdColDescDomain.fulfilOrdDesc is null or empty]");
            throw new BadRequestException("BadRequest [fulfilOrdColDescDomain.fulfilOrdDesc is null or empty]");
        }
    }
}
