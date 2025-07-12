package com.imaginamos.farmatodo.model.order;

import java.util.ArrayList;
import java.util.List;

public class OrderCourier {
    private static final String FORMAT_DATE = "dd-mm-yyyy";
    private static final String FORMAT_HOUR = "hh:mm:ss";

    /**
     * Identificador del usuario que solicita el docmicilio
     */
    private Long id_user;

    private Long type_service;
    /**
     * Codificación para el medio de pago 0. EFECTIVO 1. DATAFONO
     */
    private Long roundtrip;
    /**
     *
     */
    private Long declared_value;
    /**
     * Ciudad desde donde se realiza el domicilio.
     */
    private Long city;
    /**
     * Fecha del inicio del pedido
     */
    private String start_date;
    /**
     * Hora del pedido
     */
    private String start_time;
    /**
     * Información que ha introducido el cliente como una observación a ser tenida
     * en cuenta por parte del domiciliario
     */
    private String observation;
    /**
     * Método de pago con el que se ha creado la orden
     */
    private Long user_payment_type;
    /**
     * Segmento al que pertenece el cliente
     */
    private Long type_segmentation;
    /**
     *
     */
    private Long type_task_cargo_id;

    /**
     *
     */
    private String os;

    /**
     * Token del courier
     */
    private String uuid;
    /**
     * Arreglo que tiene contenida la información adicional del pedido
     */
    private List<Coordinates> coordinates = new ArrayList<>();

    public OrderCourier() {
    }


    /**
     * Punto de referencia geografica de la ubicación de las tiendas
     */
    public class Coordinates {

        private String type;
        private String id_point;
        private Long order_id;
        private String address;
        private String lat;
        private String lng;
        private String city;
        private String token;
        private String description;
        private ClientCourier client_data;
        private List<ProductCourier> products;

        /**
         *
         */
        private Coordinates() {
        }

        // <editor-fold defaultstate="collapsed" desc="Modificadores de acceso">
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId_point() {
            return id_point;
        }

        public void setId_point(String id_point) {
            this.id_point = id_point;
        }

        public Long getOrder_id() {
            return order_id;
        }

        public void setOrder_id(Long order_id) {
            this.order_id = order_id;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ClientCourier getClient_data() {
            return client_data;
        }

        public void setClient_data(ClientCourier client_data) {
            this.client_data = client_data;
        }

        public List<ProductCourier> getProducts() {
            return products;
        }

        public void setProducts(List<ProductCourier> products) {
            this.products = products;
        }

        // </editor-fold>
    }

    public class ClientCourier {

        private String client_name;
        private String client_phone;
        private String client_email;
        private String products_value;
        private String domicile_value;
        private String client_document;
        private Long payment_type;

        /**
         * Constructor vacio de acceso a la clase
         */
        private ClientCourier() {
        }

        // <editor-fold defaultstate="collapsed" desc="Modificadores de acceso">
        public String getClient_name() {
            return client_name;
        }

        public void setClient_name(String client_name) {
            this.client_name = client_name;
        }

        public String getClient_phone() {
            return client_phone;
        }

        public void setClient_phone(String client_phone) {
            this.client_phone = client_phone;
        }

        public String getClient_email() {
            return client_email;
        }

        public void setClient_email(String client_email) {
            this.client_email = client_email;
        }

        public String getProducts_value() {
            return products_value;
        }

        public void setProducts_value(String products_value) {
            this.products_value = products_value;
        }

        public String getDomicile_value() {
            return domicile_value;
        }

        public void setDomicile_value(String domicile_value) {
            this.domicile_value = domicile_value;
        }

        public String getClient_document() {
            return client_document;
        }

        public void setClient_document(String client_document) {
            this.client_document = client_document;
        }

        public Long getPayment_type() {
            return payment_type;
        }

        public void setPayment_type(Long payment_type) {
            this.payment_type = payment_type;
        }
// </editor-fold>
    }

    public class ProductCourier {

        /**
         * Identificador de la tienda
         */
        private Long store_id;

        /**
         * Identificador
         */
        private String sku;
        /**
         * Nombre del producto
         */
        private String product_name;
        /**
         * <code>URL</code> de la imagen del producto
         */
        private String url_img;
        /**
         * Valor calculado del precio unitario por las unidades solicitadas
         */
        private Long value;
        /**
         * Cantidad solicitada
         */
        private Long quantity;
        /**
         *
         */
        private String id_point;
        /**
         * Código de barras del producto
         */
        private String barcode;
        /**
         * Indica en que parte de la tienda se encuentra. Si el producto esta al
         * alacance del cliente o es responsabilidad del farmaceuta
         */
        private String planogram;

        /**
         * Constructor vacio para el control de acceso a la clase
         */
        private ProductCourier() {
        }

        // <editor-fold defaultstate="collapsed" desc="Modificadores de acceso">
        public Long getStore_id() {
            return store_id;
        }

        public void setStore_id(Long store_id) {
            this.store_id = store_id;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getProduct_name() {
            return product_name;
        }

        public void setProduct_name(String product_name) {
            this.product_name = product_name;
        }

        public String getUrl_img() {
            return url_img;
        }

        public void setUrl_img(String url_img) {
            this.url_img = url_img;
        }

        public Long getValue() {
            return value;
        }

        public void setValue(Long value) {
            this.value = value;
        }

        public Long getQuantity() {
            return quantity;
        }

        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }

        public String getId_point() {
            return id_point;
        }

        public void setId_point(String id_point) {
            this.id_point = id_point;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getPlanogram() {
            return planogram;
        }

        public void setPlanogram(String planogram) {
            this.planogram = planogram;
        }
        // </editor-fold>
    }

    // <editor-fold defaultstate="collapsed" desc="Modificadores de acceso">
    public Long getId_user() {
        return id_user;
    }

    public void setId_user(Long id_user) {
        this.id_user = id_user;
    }

    public Long getType_service() {
        return type_service;
    }

    public void setType_service(Long type_service) {
        this.type_service = type_service;
    }

    public Long getRoundtrip() {
        return roundtrip;
    }

    public void setRoundtrip(Long roundtrip) {
        this.roundtrip = roundtrip;
    }

    public Long getDeclared_value() {
        return declared_value;
    }

    public void setDeclared_value(Long declared_value) {
        this.declared_value = declared_value;
    }

    public Long getCity() {
        return city;
    }

    public void setCity(Long city) {
        this.city = city;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public Long getUser_payment_type() {
        return user_payment_type;
    }

    public void setUser_payment_type(Long user_payment_type) {
        this.user_payment_type = user_payment_type;
    }

    public Long getType_segmentation() {
        return type_segmentation;
    }

    public void setType_segmentation(Long type_segmentation) {
        this.type_segmentation = type_segmentation;
    }

    public Long getType_task_cargo_id() {
        return type_task_cargo_id;
    }

    public void setType_task_cargo_id(Long type_task_cargo_id) {
        this.type_task_cargo_id = type_task_cargo_id;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public List<Coordinates> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinates> coordinates) {
        this.coordinates = coordinates;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    // </editor-fold>
}
