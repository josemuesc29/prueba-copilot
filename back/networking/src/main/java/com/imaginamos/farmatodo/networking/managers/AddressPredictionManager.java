package com.imaginamos.farmatodo.networking.managers;

import com.imaginamos.farmatodo.networking.models.addresses.*;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.services.CloudFunctionsService;
import com.imaginamos.farmatodo.networking.services.GeoGoogleService;
import com.imaginamos.farmatodo.networking.services.ServinformacionService;

public class AddressPredictionManager {

    private static final int MAX_AUTOCOMPLETE_RES = 3;

    private static AddressPredictionManager instance;

    private ServinformacionService servinformacionService;
    private GeoGoogleService geoGoogleService;

    private AddressPredictionManager() {
        servinformacionService = ServinformacionService.get();
        geoGoogleService = GeoGoogleService.get();
    }

    public static AddressPredictionManager get(){
        if (instance == null) instance = getSync();
        return instance;
    }

    private static synchronized AddressPredictionManager getSync(){
        if (instance == null) instance = new AddressPredictionManager();
        return instance;
    }

    public AddressPredictionRes getAddressPredictions(AddressPredictionReq siAddressReq, String typeAlgolia) {
        AddressPredictionRes addressPredictionRes = new AddressPredictionRes();
        addressPredictionRes.setInput(siAddressReq.getAddress());
        addressPredictionRes.setCity(siAddressReq.getCity());
        // servi
//        GeoCoderResponse geoCoderResponse = servinformacionService.postAddressPrediction(siAddressReq);
        // lupap - servi
        GeoCoderResponse geoCoderResponse = ApiGatewayService.get().geoCoder(siAddressReq);
        if (!validateAddressPrediction(geoCoderResponse)) {
            GAutocompleteRes autocompleteRes = geoGoogleService.getAutocomplete(siAddressReq, typeAlgolia);
            int placesCount = 0;
            for (GPlacePredictionRes item : autocompleteRes.getPredictions()) {
                if (placesCount == MAX_AUTOCOMPLETE_RES) break;
                GPlaceIdRes placeIdRes = geoGoogleService.getPlaceById(item.getPlaceId());
                /*String latlng = String.format("%f,%f", placeIdRes.getLatitude(), placeIdRes.getLongitude());
                GGeoReverseRes geoReverseRes = geoGoogleService.getGeoReverse(latlng);
                String address = geoReverseRes.getAddress();
                if (!address.isEmpty()) {
                    GDataRes gDataRes = new GDataRes();
                    gDataRes.setAddress(geoReverseRes.getAddress());
                    gDataRes.setLatitude(placeIdRes.getLatitude());
                    gDataRes.setLongitude(placeIdRes.getLongitude());
                    gDataRes.setNeighborhood("");
                    gDataRes.setPlaceName(placeIdRes.getResult().getName());
                    addressPredictionRes.getAddressPredictions().add(gDataRes);
                    placesCount++;
                }*/
                ReverseGeoReq geoReq = new ReverseGeoReq();
                geoReq.setLatitude(placeIdRes.getLatitude());
                geoReq.setLongitude(placeIdRes.getLongitude());

                ReverseGeoRes geoRes = ApiGatewayService.get().geoInverse(geoReq);
                if (geoRes.getData() != null &&
                    geoRes.getData().getAddress() != null &&
                    !geoRes.getData().getAddress().isEmpty()) {

                    geoRes.getData().setLatitude(geoReq.getLatitude());
                    geoRes.getData().setLongitude(geoReq.getLongitude());
                    geoRes.getData().setPlaceName(placeIdRes.getResult().getName());
                    addressPredictionRes.getAddressPredictions().add(geoRes.getData());
                    placesCount++;
                }
            }
        } else {
            addressPredictionRes.getAddressPredictions().add(geoCoderResponse.getData());
        }
        return addressPredictionRes;
    }

    private static boolean validateAddressPrediction(GeoCoderResponse addressPrediction) {

        if (addressPrediction != null) {
            switch (addressPrediction.getData().getStatus()) {
                case "A":
                case "B":
                case "D":
                case "F":
                case "I":
                case "K":
                case "L":
                case "M":
                case "N":
                case "O":
                case "Y":
                case "Z":
                    return true;
            }
        }
        return false;
    }

}
