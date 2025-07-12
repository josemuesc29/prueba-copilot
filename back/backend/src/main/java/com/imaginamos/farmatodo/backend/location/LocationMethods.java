package com.imaginamos.farmatodo.backend.location;

import com.algolia.search.exceptions.AlgoliaException;
import com.google.api.server.spi.response.ConflictException;
import com.imaginamos.farmatodo.backend.cache.CachedDataManager;
import com.imaginamos.farmatodo.model.algolia.*;
import com.imaginamos.farmatodo.model.order.OptimalRouteCheckoutRequest;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.HttpStatusCode;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.backend.util.FTDUtil;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;
import com.imaginamos.farmatodo.networking.models.addresses.StoreCalculationDistanceRes;
import com.imaginamos.farmatodo.networking.models.addresses.ValidateAddressRes;
import com.imaginamos.farmatodo.networking.models.addresses.osrm.TableOSRMResponse;
import com.imaginamos.farmatodo.networking.services.OSRMService;

import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by ccrodriguez on 27/12/2018.
 */

public class LocationMethods {

    private static final Logger LOG = Logger.getLogger(CachedDataManager.class.getName());

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



    public static List<Integer> getNearestStoresByAddress(float addressLat,
                                                          float addressLon,
                                                          float radioDistance,
                                                          OptimalRouteCheckoutRequest request) throws AlgoliaException {
        if (request == null) {
            LOG.severe("getNearestStoresByAddress() Request is null");
            return new ArrayList<>();
        }

        String customerId = Constants.CUSTOMER_ANONYMOUS;

        if (!request.getCustomerId().isBlank()) {
            customerId = request.getCustomerId();
        }

        StoresAlgolia stores = CachedDataManager.getStoresAlgoliaCached();

        OptimalRouteStoresConfig routeStoresConfig = GrowthBookConfigLoader.getStoresEnabledConfig(customerId);

        if (routeStoresConfig.getStores() == null) {
            LOG.warning("OptimalRouteStoresConfig getStores is null");
            return new ArrayList<>();
        }

        OptimalRouteDistance optimalRouteDistanceConfig = request.getOptimalRouteDistance();

        if (stores.getStores() == null || stores.getStores().isEmpty()) {
            LOG.warning("StoresAlgolia getStores is null or empty");
            return new ArrayList<>();
        }

        if (optimalRouteDistanceConfig == null) {
            LOG.warning("OptimalRouteDistance is null");
            return new ArrayList<>();
        }

        List<StoreAlgolia> storesFiltered = stores.getStores().stream()
                .filter(store -> routeStoresConfig.getStores().getEnable() != null
                        && !routeStoresConfig.getStores().getEnable().isEmpty()
                        && routeStoresConfig.getStores().getEnable().contains((long) store.getId()))
                .filter(store -> {
                    float latStore = store.getLatitude();
                    float lonStore = store.getLongitude();
                    float distanceToStoreHaversine = (float) distanceInKm(latStore, lonStore, addressLat, addressLon);
                    return distanceToStoreHaversine <= radioDistance;
                })
                .toList();

        if (storesFiltered.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, Float> nearbyStores = new HashMap<>();

        if (optimalRouteDistanceConfig.getRuteoApi()) {
            try {

                LOG.info("OSRM is enabled, calling OSRM for distance matrix");
                StringBuilder coordinates = new StringBuilder();
                for (StoreAlgolia store : storesFiltered) {
                    if (!coordinates.isEmpty()) {
                        coordinates.append(";");
                    }
                    coordinates.append(store.getLongitude())
                            .append(",")
                            .append(store.getLatitude());
                }

                coordinates.append(";")
                        .append(addressLon)
                        .append(",")
                        .append(addressLat);

                Optional<TableOSRMResponse> response = OSRMService.get()
                        .getDistanceMatrix(coordinates.toString());

                if (response.isPresent()) {
                    List<List<Double>> distances = response.get().getDistances();

                    for (int i = 0; i < storesFiltered.size(); i++) {
                        StoreAlgolia store = storesFiltered.get(i);
                        Double distance = distances.get(i).get(distances.get(i).size() - 1);

                        if (distance != null && distance > 0 && (distance / 1000.0) <= radioDistance) {
                            nearbyStores.put(store.getId(), (float) (distance / 1000.0));
                        }
                    }
                }
            } catch (Exception e) {
                LOG.warning("Error in OSRM matrix call: " + e.getMessage());
                // Fallback to Haversine
                for (StoreAlgolia store : storesFiltered) {
                    float distanceToStore = (float) distanceInKm(
                            store.getLatitude(),
                            store.getLongitude(),
                            addressLat,
                            addressLon
                    );
                    if (distanceToStore <= radioDistance) {
                        nearbyStores.put(store.getId(), distanceToStore);
                    }
                }
            }
        } else {
            // If OSRM is disabled, use Haversine
            for (StoreAlgolia store : storesFiltered) {
                float distanceToStore = (float) distanceInKm(
                        store.getLatitude(),
                        store.getLongitude(),
                        addressLat,
                        addressLon
                );
                if (distanceToStore <= radioDistance) {
                    nearbyStores.put(store.getId(), distanceToStore);
                }
            }
        }

        Map<Integer, Float> nearbyStoresSorted = FTDUtil.sortByValue(nearbyStores);
        return new ArrayList<>(nearbyStoresSorted.keySet());
    }

    public static String getDistanceAddressToStore(float addressLat, float addressLon,int storeId) throws AlgoliaException {

        StoresAlgolia stores = APIAlgolia.getStoresAlgolia();


        Optional<StoreAlgolia> optionalStoreAux = Objects.requireNonNull(stores.getStores()).stream().filter(storeAlgolia -> storeAlgolia.getId() == storeId).findFirst();


        if (optionalStoreAux.isPresent()){
            StoreAlgolia storeAux = optionalStoreAux.get();
            float latStore = storeAux.getLatitude();
            float lonStore = storeAux.getLongitude();

            DecimalFormat df = new DecimalFormat("#.##");
            return df.format(distanceInKm(addressLat,addressLon,latStore,lonStore));
        }

        return "0";


    }

    public static StoreCalculationDistanceRes getDistanceToStoreFromLatLng(float addressLat, float addressLon) throws AlgoliaException, ConflictException {


        StoresAlgolia stores = APIAlgolia.getStoresAlgolia();

        if (stores.getStores() == null || stores.getStores().isEmpty()){
            throw new ConflictException(Constants.COORDINATES_ARE_NOT_VALID);
        }

//        LOG.info("coordinates to validate -> " + addressLat + ", " + addressLon   );
        PropertiesBaseSAGAlgolia kmsWhitValidateCoordinates = APIAlgolia.getSagBasePropertiesAlgolia();
        final double distanceValidate = kmsWhitValidateCoordinates.getKmsWhitValidateCordinates();
//        LOG.info("Distancia Maxima Tiendas " + Math.round(distanceValidate * 1000) + "Metros");

        List<StoreCalculationDistanceRes> listStoresCalc = new ArrayList<>();

        stores.getStores().forEach(store -> {
            try {
                StoreCalculationDistanceRes storeValidWhitCoordinatesRes = new StoreCalculationDistanceRes();
                float latStore = store.getLatitude();
                float lonStore = store.getLongitude();

                storeValidWhitCoordinatesRes.setLatStore(latStore);
                storeValidWhitCoordinatesRes.setLngStore(lonStore);
                storeValidWhitCoordinatesRes.setStore(store.getId());
                storeValidWhitCoordinatesRes.setDistanceInKms(distanceInKm(addressLat, addressLon, latStore, lonStore));
                listStoresCalc.add(storeValidWhitCoordinatesRes);
            }catch (Exception e) {
                LOG.warning("Ocurrio un error con la tienda: " + store.getName());
            }
        });

        if (listStoresCalc.isEmpty()){
            throw new ConflictException(Constants.COORDINATES_ARE_NOT_VALID);
        }

        listStoresCalc.sort(Comparator.comparing(StoreCalculationDistanceRes::getDistanceInKms));

        Optional<StoreCalculationDistanceRes> optionalResp = listStoresCalc.stream().findFirst();

        if (!optionalResp.isPresent()){
            throw new ConflictException(Constants.COORDINATES_ARE_NOT_VALID);
        }

//        LOG.info("Tienda mas cercana -> " + optionalResp.get() );

        if (optionalResp.get().getDistanceInKms() > distanceValidate){
            LOG.warning("No se encuentra ninguna tienda a menos de " + Math.round(distanceValidate * 1000) + "Metros");
            throw new ConflictException(Constants.COORDINATES_ARE_NOT_VALID);
        }

        return optionalResp.get();

    }
    public static ValidateAddressRes BuildResponseNotContent(){
        return new ValidateAddressRes(HttpStatusCode.NO_CONTENT.getCode(),
                HttpStatusCode.NO_CONTENT.getStatusName(),
                Constants.LOCATION_PLACE_ID_NOT_FOUND, null);
    }
}
