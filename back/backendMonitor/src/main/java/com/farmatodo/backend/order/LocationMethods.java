package com.farmatodo.backend.order;

/**
 * Created by lorenzopinango on 16/05/2019.
 */

public class LocationMethods {

    /**
     * Radio de la tierra.
     */
    private static final int EARTH_RADIUS = 6371;

    /**
     *
     * Method using the Haversine formula to calculate the great-circle distance
     * between tow points by the latitude and longitude coordinates.</p>
     *
     * @param startLati Initial latitude
     * @param startLong Initial longitude
     * @param endLati Final latitude
     * @param endLong Final longitude
     * @return The distance in Kilometers (Km)
     */
    public static double distanceInKm(double startLati, double startLong, double endLati, double endLong) {

        double diffLati = Math.toRadians(endLati - startLati);
        double diffLong = Math.toRadians(endLong - startLong);

        double radiusStartLati = Math.toRadians(startLati);
        double radiusEndLati = Math.toRadians(endLati);

        double a = Math.pow(Math.sin(diffLati / 2), 2) + Math.pow(Math.sin(diffLong / 2), 2) * Math.cos(radiusStartLati) * Math.cos(radiusEndLati);
        double c = 2 * Math.asin(Math.sqrt(a));

        return EARTH_RADIUS * c;
    }


}
