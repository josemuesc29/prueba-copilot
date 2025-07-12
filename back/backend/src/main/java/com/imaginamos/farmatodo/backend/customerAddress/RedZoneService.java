package com.imaginamos.farmatodo.backend.customerAddress;

import com.google.api.server.spi.response.BadRequestException;
import com.imaginamos.farmatodo.model.customer.RedZone;
import com.imaginamos.farmatodo.networking.services.ActiveRedZoneService;

import java.io.IOException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RedZoneService {

    private static final Logger LOG = Logger.getLogger(RedZoneService.class.getName());

    public static boolean isRedZoneActive(String redZoneId, String cityId){
        if(Objects.isNull(redZoneId) || Objects.isNull(cityId)){
            return false;
        }

        Optional<List<String>> optionalRedZoneIdList = activeRedZoneList(cityId);
        return optionalRedZoneIdList.isPresent() &&
                optionalRedZoneIdList.get().contains(redZoneId);
    }

    private static Optional<List<String>> activeRedZoneList(String cityId){

        try {
            Optional<List<RedZone>> OptionalRedZoneList = ActiveRedZoneService.get().fetchRedZoneData(cityId);
            if(OptionalRedZoneList.isEmpty()){
                return Optional.empty();
            }

            List<RedZone> redZoneList = OptionalRedZoneList.get();


            List<String> cityRedZoneIds = redZoneList.stream()
                    .map(RedZone::getCityRedZoneId)
                    .collect(Collectors.toList());

            return Optional.of(cityRedZoneIds);

        } catch (BadRequestException | IOException e) {
            LOG.warning("activeRedZoneList e: " + e);
            return Optional.empty();
        }
    }

    public static boolean isOutsideRedZoneHoursColombia() {

        final String COLOMBIAN_ZONE_ID = "America/Bogota";
        ZonedDateTime nowInColombia = ZonedDateTime.now(ZoneId.of(COLOMBIAN_ZONE_ID));
        LocalTime currentTime = nowInColombia.toLocalTime();

        // Definir los límites de 8:00 AM y 5:00 PM
        final int OPEN_HOUR = 8;
        final int CLOSE_HOUR = 17;
        final int ZERO_MINUTES = 0;


        LocalTime startOfDay = LocalTime.of(OPEN_HOUR, ZERO_MINUTES);
        LocalTime endOfDay = LocalTime.of(CLOSE_HOUR, ZERO_MINUTES);

        // Retornar true si está antes o después de las horas fijadas
        return currentTime.isBefore(startOfDay) || currentTime.isAfter(endOfDay);
    }
}
