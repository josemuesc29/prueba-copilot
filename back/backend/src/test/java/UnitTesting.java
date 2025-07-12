import com.imaginamos.farmatodo.backend.location.LocationMethods;
import com.imaginamos.farmatodo.networking.util.Util;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class UnitTesting {
    @Test
    void justAnExample() {
        System.out.println("This test method should be run");
    }

    @Test
    void testHaversineMethod(){

        double distance = LocationMethods.distanceInKm(4.681914, -74.044215,4.740245, -74.041208);
        System.out.println("test distance -> " + distance);


    }

    /*@Test
    void testGeoCoder(){
        AddressPredictionReq request = new AddressPredictionReq("bogota","calle 161 #16d - 10");

        GeoCoderResponse response  = ApiGatewayService.get().geoCoder(request);
        System.out.println("Response -> " + response.toString());

    }*/

    /*@Test
    void testCreateBrazeAndGetUUID(){
        final String email = "cristhian.rodriguez@farmatodo.com";
        final String document= "102078717999";
        final Optional<String> uuidBraze = ApiGatewayService.get().getUUIDFromBrazeCreateUser(email,document);

        Assert.isTrue(uuidBraze.isPresent());
    }*/

    /*@Test
    void testAlgoliaGeoGrids(){
        GeoGridsConfigAlgolia geoGridsConfigAlgolia = APIAlgolia.getGeoGridsAlgolia();

        Assert.notNull(geoGridsConfigAlgolia);
        Assert.isTrue(geoGridsConfigAlgolia.getCities().size() > 0);
    }/*

/*
    @Test
    void testGenericDataAlgolia(){

        List<String> seoIds = Arrays.asList("94","93");

        List<JSONObject> listData = APIAlgolia
                .getJsonListObjByObjectIDs(seoIds, "categories_seo");

        Assert.isTrue(!listData.isEmpty());
    }
*/

    /*@Test
    void testGeoCoderPlaceID() throws IOException {
        String placeId = "18b0c137-a041-4ef0-ba52-8602a63bd798";
        GeoCoderResponse response = ApiGatewayService.get().geoCoderLupapPlaceId(placeId);
        System.out.println("response -> " + response.toString());
    }*/

    /*@Test
    void testGeoInverse(){
        ReverseGeoReq request = new ReverseGeoReq(4.740301,-74.040644);
        ReverseGeoRes response = ApiGatewayService.get().geoInverse(request);
        System.out.println("Response -> " + response.toString());
    }*/

    @Test
    void testBuildUrl(){
        String baseBanners = "https://cms-dot-stunning-base-164402.appspot.com/backend/flexible/v2/cms/getBannersHome?emailUser={mail}&type=MAIN_BANNER&category=0";
        String base = "CustomerWS/v1/customerOnly/{idCustomer}";
        Map<String,String> pathVariables = new HashMap();
        pathVariables.put("idCustomer", "123456");

/*        Map<String,String> requestParams = new HashMap();
        requestParams.put("order", "98644");
        requestParams.put("fecha", "1234567890");*/

        String url = Util.buildUrl(base, pathVariables, null);
        System.out.println(url);
    }

    @Test
    void testNulls(){
        String a = null;
        String b = "";
        String c = "AAA";
        Integer d = 0;
        Integer e = null;

        System.out.println("1 .. todos, tienen valor 0 vacio y nulo");

        if (Stream.of(a,b,c,d,e).allMatch(Objects::nonNull)){
            System.out.println("Ninguno es nulo o vacio !");
        }else {
            System.out.println("Alguno es nulo o vacio");
        }

        System.out.println("2. String con valor y Integer con valor 0");

        if (Stream.of(c,d).allMatch(Objects::nonNull)){
            System.out.println("Ninguno es nulo o vacio!");
        }else {
            System.out.println("Alguno es nulo o vacio");
        }


        System.out.println("3. String vacio, integerr 0 y integer null");

        if (Stream.of(b,d,e).allMatch(Objects::nonNull)){
            System.out.println("Ninguno es nulo o vacio!");
        }else {
            System.out.println("Alguno es nulo o vacio");
        }

        System.out.println("4. String null y Integer null");

        if (Stream.of(a,e).allMatch(Objects::nonNull)){
            System.out.println("Ninguno es nulo o vacio!");
        }else {
            System.out.println("Alguno es nulo o vacio");
        }

        System.out.println("4. String vacio y String null");

        if (Stream.of(b,c).allMatch(Objects::nonNull)){
            System.out.println("Ninguno es nulo o vacio!");
        }else {
            System.out.println("Alguno es nulo o vacio");
        }


    }



    /*@Test
    void testAlgolia(){
        System.out.println("prueba test");

//        RestrictionQuantity restrictionQuantity = APIAlgolia.getRestrictionQuantityItems();
          Optional<DeliveryFast> deliveryFast = APIAlgolia.getDeliveryFastProperties();


        System.out.println(deliveryFast.get().toString());
//        System.out.println(restrictionQuantity.toString());

    }*/




}
