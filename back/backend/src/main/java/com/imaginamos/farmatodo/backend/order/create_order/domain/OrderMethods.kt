package com.imaginamos.farmatodo.backend.order.create_order.domain

import com.imaginamos.farmatodo.backend.location.LocationMethods
import com.imaginamos.farmatodo.model.algolia.OptimalRouteDistance
import com.imaginamos.farmatodo.model.algolia.StoreConfig
import com.imaginamos.farmatodo.model.order.OptimalRouteCheckoutRequest
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia
import java.util.*
import java.util.logging.Logger
import kotlin.collections.ArrayList

class OrderMethods {

    private val log = Logger.getLogger(OrderMethods::class.java.name)
    private final val BOGOTA_GMT = -5

    fun allStoreIsClosed(optimalRouteDistance: OptimalRouteDistance, request: OptimalRouteCheckoutRequest): Boolean {
        //log.info("Data   1 // " + request)
        if (optimalRouteDistance.firstDistance > 0 && optimalRouteDistance.secondDistance > 0){

            val lat: Float = request.addressLat;
            val lng: Float = request.addressLon;
            val city: String = request.city;
            val distance: Float = optimalRouteDistance.secondDistance;

            if (lat == 0F  || lng == 0F || city.isEmpty() || distance <= 0){
                return false;
            }

            val nearStores: List<Int> = LocationMethods.getNearestStoresByAddress(lat,lng,distance, request);

            val availableStores: ArrayList<Int> = ArrayList();


            nearStores.forEach { store ->
                //log.info("validando la tienda $store en la ciudad $city")
                if (isStoreAvailable(store.toString(),null)){
                    //log.info("La tienda $store esta abierta!!")
                    availableStores.add(store)
                }
            }

            if (availableStores.isNullOrEmpty()){
                return true
            }/*else {
                availableStores.forEach { store ->
                    run {
                        log.info("near store is available -> $store")
                    }
                }
            }*/

        }

        return false
    }

    fun getStoreAssigned(optimalRouteDistance: OptimalRouteDistance, request: OptimalRouteCheckoutRequest): Int {
        //log.info("Data   1 // $request")
        if (optimalRouteDistance.secondDistance > 0){

            val lat: Float = request.addressLat;
            val lng: Float = request.addressLon;
            val city: String = request.city;
            val distance: Float = optimalRouteDistance.secondDistance;

            if (lat == 0F  || lng == 0F || city.isEmpty() || distance <= 0){
                //log.info("La validacion de los parametros es nula")
                return 0;
            }

            val nearStores: List<Int> = LocationMethods.getNearestStoresByAddress(lat,lng,distance, request);

            if (nearStores.isEmpty()) {
                return 0;
            }

            return nearStores[0];
        }
        //log.info("Es nulo el request")
        return 0
    }


    fun getNextDateToSchedule(hourToOpen: Int): Date {

        log.info("hora de apertura de la tienda -> : $hourToOpen")

//        val calendar: Calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"))
        val calendar: Calendar = Calendar.getInstance()
        log.info("fecha_gcloud: ${calendar.time}")
        calendar.add(Calendar.HOUR_OF_DAY, BOGOTA_GMT)

        log.info("fecha_col: ${calendar.time}")

        val currentHour: Int = calendar[Calendar.HOUR_OF_DAY]
        log.info("hora_actual: $currentHour")

        if (currentHour in 1 until hourToOpen){
            log.info("Se manda a programar el mismo dia")
            calendar.add(Calendar.DAY_OF_MONTH, 0)
        }else {
            log.info("Se manda a programar al dia siguiente")
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.AM_PM, Calendar.AM)
        calendar.set(Calendar.HOUR, hourToOpen)
        log.info("fecha_programar: ${calendar.time}")
        return calendar.time;
    }

    fun getStoreConfig(storeID: String): Int{
        val calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"))
        val storeConfig : StoreConfig
        storeConfig = APIAlgolia.getStoreConfigByStoreID(storeID, calendar)
        log.info("Tienda asignada -> " + storeConfig.store)
        return storeConfig.store
    }

    fun getHourStoreConfig(storeID: String): Int{
        val calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"))
        val storeConfig : StoreConfig
        storeConfig = APIAlgolia.getStoreConfigByStoreID(storeID, calendar)
        log.info("Tienda asignada -> " + storeConfig.store)
        return storeConfig.open
    }

    fun isStoreAvailable(storeID: String, pickingDate: Date?): Boolean {
        val calendar: Calendar?
        var isStoreAvailable = false
        try {
            // ¿Es una orden programada?
            //LOG.warning("Validating ¿Es una orden programada? (pickingDate!=null) => " + (pickingDate != null));
            if (pickingDate != null) {
                calendar = GregorianCalendar.getInstance()
                calendar.time = pickingDate
            } else {
                calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Bogota"))
                calendar.time = Date()
            }
            val storeConfig = APIAlgolia.getStoreConfigByStoreID(storeID, calendar)
            //log.warning("(storeConfig!=null)=>" + (storeConfig != null))
            val currentHour = calendar[Calendar.HOUR_OF_DAY]
//            logger.warning("currentHour=>$currentHour")
            //log.info("hora hoy -->" + currentHour)
            if (storeConfig != null) {
                //log.info("configuracion de horario de tienda: $storeConfig")
//                logger.warning("(currentHour>=storeConfig.getOpen() && currentHour<=storeConfig.getClose())=>" + (currentHour >= storeConfig.open && currentHour <= storeConfig.close - 1))

                //Se resta 1 hora a la hora de cierre para que la compra pueda ser llevada por el mensajero
                if (currentHour >= storeConfig.open && currentHour <= storeConfig.close - 1) {
                    //log.info("LA TIENDA $storeID se toma como abierta en la hora: $currentHour , y la tienda tiene" +
                            //"como horario: $storeConfig ")
                    isStoreAvailable = true
                }
//                logger.warning("(storeConfig.getOpen()==0 && storeConfig.getClose()==0)=>" + (storeConfig.open == 0 && storeConfig.close == 0))
                if (storeConfig.open == 0 && storeConfig.close == 0) {
                    //log.info("LA TIENDA SE TOMA COMO ABIERTA POR Q TIENE HORARIO 0,0")
                    isStoreAvailable = true
                }

            } else {
                //log.warning(Constants.ERROR_STORE_CONFIG_NOT_FOUND)
                isStoreAvailable = true
            }
        } catch (e: Exception) {
            //log.warning("Error en isStoreAvailable(). Mensaje:" + e.message)
            //log.warning(Constants.ERROR_STORE_CONFIG_NOT_FOUND)
            isStoreAvailable = true
        }
        return isStoreAvailable

    }

    fun showTransferOption(distancePopUp: Float, request: OptimalRouteCheckoutRequest): Boolean {

        val lat: Float = request.addressLat;
        val lng: Float = request.addressLon;
        val city: String = request.city;
        val distance: Float = distancePopUp;

        log.info("showTransferOption() La latitud es: $lat, la longitud es: $lng, la ciudad es: $city, y la distancia es: $distance")

        if (lat == 0F  || lng == 0F || city.isEmpty() || distance <= 0){
            //log.info("showTransferOption() La validacion de los parametros es nula")
            return false;
        }

        val nearStores: List<Int> = LocationMethods.getNearestStoresByAddress(lat,lng,distance, request);

        if (nearStores.isEmpty()) {
            log.info("showTransferOption() No hay tiendas cercanas")
            return false;
        }

        log.info("showTransferOption() La tienda mas cercana es: ${nearStores[0]}, y la distancia es: $distance")

        return true;

    }


}
