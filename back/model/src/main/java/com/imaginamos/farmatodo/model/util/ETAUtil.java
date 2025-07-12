package com.imaginamos.farmatodo.model.util;

import com.imaginamos.farmatodo.model.algolia.eta.ETAConfig;
import com.imaginamos.farmatodo.model.algolia.eta.Variable;
import com.imaginamos.farmatodo.model.algolia.eta.VariableByCity;
import com.imaginamos.farmatodo.model.payment.OrderStatusEnum;


import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ETAUtil {

    private static final Logger LOG = Logger.getLogger(ETAUtil.class.getName());

    /**
     * @param cityId
     * @return int El tiempo estimado de llegada en minutos. Ej: 50 mins.
     * */
    public static int getTimeToArrive(final Long orderId, final String cityId, final boolean hasTransfer,
                                      final Date creationDate, final int orderCurrentStatus,
                                      final Optional<ETAConfig> optionalETAConfig, final Integer inNumberOfStops){
        //LOG.info("getTimeToArrive("+cityId+")");
        int estimatedTimeArrival = 0;

        if(optionalETAConfig.isPresent()){
            final ETAConfig etaConfig = optionalETAConfig.get();

            //Se inicializa el ETA con el valor estandar. (30 minutos)
            estimatedTimeArrival += etaConfig.getStandardDeliveryTimeInMinutes();
            //LOG.info("estimatedTimeArrival : ["+estimatedTimeArrival+"]");

            // Cada orden tiene AL MENOS 1 parada.
            //LOG.info("inNumberOfStops : ["+inNumberOfStops+"]");
            Integer numberOfStops = (inNumberOfStops != null && inNumberOfStops > 0) ? inNumberOfStops : 1;

            // Valida tiempo adicional por posibles paradas...
            estimatedTimeArrival += numberOfStops * etaConfig.getStandardAdditionalTimeInMinForEachStop();
            //LOG.info("estimatedTimeArrival + paradas : ["+estimatedTimeArrival+"]");

            // Valida restrasos en picking o facturacion...
            estimatedTimeArrival += validateCurrentOrderStatusAndApplyAdditionalTimeToETA(creationDate, etaConfig, orderCurrentStatus);
            //LOG.info("estimatedTimeArrival + restrasos : ["+estimatedTimeArrival+"]");

            if(etaConfig.getVariables() != null && !etaConfig.getVariables().isEmpty()){
                final List<Variable> variables = etaConfig.getVariables();
                VariableByCity variableByCity = getVariableByCity(cityId, etaConfig.getVariablesCity());

                if(variableByCity != null){
                    List<String> variablesToApply = variableByCity.getVariablesToApply();

                    for(String variableToApply : variablesToApply){
                        estimatedTimeArrival += getAdditionalTimeInMinutes(variables, variableToApply);
                        //LOG.info("variableToApply => {"+variableToApply+"}, estimatedTimeArrival => {"+estimatedTimeArrival+"}");
                    }
                }
            }
        }else{
            LOG.warning("No se encontro la configuracion de ETA en Algolia.");
        }

        return estimatedTimeArrival;
    }

    /**
     * Valida si es necesario adicionar mas tiempo por posibles retrasos en  picking o facturacion.
     * */
    private static int validateCurrentOrderStatusAndApplyAdditionalTimeToETA(final Date orderCreationDate, final ETAConfig etaConfig, final int orderCurrentStatus){
        int addtionalTime = 0;
        final int thresholdToAddMoreTimeWhenIsPickingAndTimeIsUp = etaConfig.getThresholdToAddMoreTimeWhenIsPickingAndTimeIsUp();
        final int thresholdToAddMoreTimeWhenNotBilledAndTimeIsUp = etaConfig.getThresholdToAddMoreTimeWhenNotBilledAndTimeIsUp();

        try{
            final Date futureDeliveryDate = DateUtil.addMinutesToDate(orderCreationDate, etaConfig.getStandardDeliveryTimeInMinutes());
            final Date now = DateUtil.getDate();
            final Duration differenceTime = Duration.between(now.toInstant(), futureDeliveryDate.toInstant());
            final long diffInMinutes      = TimeUnit.MILLISECONDS.toMinutes(differenceTime.toMillis());
            LOG.info("now => "+now.toString());
            LOG.info("differenceTime => " + differenceTime.toString());
            LOG.info("diffInMinutes => " + diffInMinutes);

            final boolean isInPicking      = orderCurrentStatus == OrderStatusEnum.PICKING.getId();
            final boolean hasNotBeenBilled = orderCurrentStatus < OrderStatusEnum.FACTURADA.getId();
            LOG.info("isInPicking => " + isInPicking);
            LOG.info("hasNotBeenBilled => " + hasNotBeenBilled);

            final boolean timeIsUpWhenPicking          = diffInMinutes <= thresholdToAddMoreTimeWhenIsPickingAndTimeIsUp;
            final boolean timeIsUpWhenNotHasBeenBilled = diffInMinutes <= thresholdToAddMoreTimeWhenNotBilledAndTimeIsUp;
            LOG.info("timeIsUpWhenPicking => " + timeIsUpWhenPicking);
            LOG.info("timeIsUpWhenNotHasBeenBilled => " + timeIsUpWhenNotHasBeenBilled);

            // Si No esta en PICKING y falta poco para cumplirse el limite entonces adicione tiempo.
            LOG.info("IF(!isInPicking AND timeIsUpWhenPicking) : ["+(!isInPicking && timeIsUpWhenPicking)+"]");
            if(!isInPicking && timeIsUpWhenPicking){
                addtionalTime += etaConfig.getStandardAdditionalTimeWhenIsPickingAndTimeIsUp();
            }
            LOG.info("addtionalTime => " + addtionalTime);

            // Si no ha sido FACTURADO y falta poco para cumplirse el limite entonces adicione tiempo.
            LOG.info("IF(hasNotBeenBilled AND timeIsUpWhenNotHasBeenBilled) : ["+(hasNotBeenBilled && timeIsUpWhenNotHasBeenBilled)+"]");
            if(hasNotBeenBilled && timeIsUpWhenNotHasBeenBilled){
                addtionalTime += etaConfig.getStandardAdditionalTimeWhenNotBilledAndTimeIsUp();
            }
            LOG.info("addtionalTime => " + addtionalTime);
        }catch (Exception e){}
        return addtionalTime;
    }

    private static VariableByCity getVariableByCity(final String cityId, List<VariableByCity> variablesByCity){
        if(cityId != null){
            for(VariableByCity variableByCity : variablesByCity){
                if(variableByCity.getCityId().equals(cityId))
                    return variableByCity;
            }
            return null;
        }else
            return null;
    }

    /**
     * Obtener el valor en tiempo que tiene configurado la variable.
     * */
    private static int getAdditionalTimeInMinutes(final List<Variable> listaVariables, final String variableToFind){
        LOG.info("getAdditionalTimeInMinutes()");
        int additionalTimeInMinutes = 0;
        LOG.info("IF (listaVariables != null && !listaVariables.isEmpty()) => "+(listaVariables != null && !listaVariables.isEmpty()));
        if(listaVariables != null && !listaVariables.isEmpty()){
            LOG.info("listaVariables => "+listaVariables.toString());
            LOG.info("variableToFind => "+variableToFind);
            for(Variable variable : listaVariables){
                if(Objects.deepEquals(variable.getKey(), variableToFind)) {
                    LOG.info("variable {"+variableToFind+"} found.");
                    additionalTimeInMinutes = variable.getAdditionalTimeInMinutes();
                    LOG.info("time : "+additionalTimeInMinutes);
                    break;
                }
            }
        }
        return additionalTimeInMinutes;
    }

}
