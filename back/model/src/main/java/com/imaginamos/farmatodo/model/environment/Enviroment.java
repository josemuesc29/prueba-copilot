package com.imaginamos.farmatodo.model.environment;

public class Enviroment {


	/**********************************************************************************/
	/** Defina aqui las propieades VARIABLES y setee el valor en todos los ambientes. */
	/**********************************************************************************/
	public static String IP_BASE;
	public static String ENVIROMENT_NAME;
	public static String IP_BASE_MONITOR;
	public static String BACKEND_CORE_30_HOST_BASE;
	public static String API_GATEWAY;

	public static String SHOPPING_DIRECT_FAIL_OVER;
	public static String SHOPPING_DIRECT;
	public static String OMS_DIRECT;
	public static String DTF_DIRECT;
	public static String BACKEND_CORE_30_OMS_HOST_BASE;
	public static String CMS_DIRECT;
	public static String ANTIFRAUD;
	public static String NEST_UTILITIES_DIRECT;

	/** Algolia: */
	public static String ALGOLIA_PRODUCTS;
	public static String ALGOLIA_OPTICAL_PRODUCTS;
	public static String ALGOLIA_PRODUCTS_SCAN_AND_GO;
	public static String ALGOLIA_APP_ID;
	public static String ALGOLIA_COUPON_POPUP;
	public static String ALGOLIA_API_KEY;
	public static String ALGOLIA_STORE_CONFIG;
	public static String ALGOLIA_HOLIDAYS;
	public static String ALGOLIA_INDEX_PROPERTIES;

	public static String ALGOLIA_SUBSCRIBE_AND_SAVE_CONFIG_INDEX;
	public static String ALGOLIA_INDEX_PROD_VIDEO;
	public static String ALGOLIA_INDEX_ITEMS_SEO;
	public static String ALGOLIA_INDEX_CONFIG_SEO;
	public static String ALGOLIA_INDEX_LANDING_PAGES;
	public static String ALGOLIA_PROPERTIES;
	public static String ALGOLIA_PAYMENTMETHODS_DELIVERYTYPE;
	public static String ALGOILIA_DISTANCE_PROPERTIES;
	public static String ALGOLIA_AUTOCOMPLETE_GOOGLE_PROPERTIES;
	public static String ALGOLIA_COURIER_SOCKET_ACTIVE;
	public static String ALGOLIA_DEFAULT_STORES_BY_CITY;
	public static String ALGOLIA_KEYWORDS_CONFIG;
	public static String ALGOLIA_CONFIG_ADDRESS;
	public static String ALGOLIA_DEFAULT_STORES_BY_ID;
	public static String STORE_ID_DEFAULT_OPTICS;
	public static String ALGOLIA_PAYMENTMETHODS_PROPERTIES;
	public static String ALGOLIA_SUPPORT_NUMBERS;
	public static String ALGOLIA_HTTPS_WEBSOCKET_URL;
	public static String ALGOLIA_IMAGE_TRACKING;
	public static String ALGOLIA_MESSAGE_CONFIG_CREATE_ORDER;
	public static String ALGOLIA_HTTP_WEBSOCKET_URL;
	public static String ALGOLIA_INDEX_SORTED_HIGHLIGHT;
	public static String ALGOLIA_SAG_BASE_PROPERTIES;
	public static String ALGOLIA_FILTERS_PRODUCTS;
	public static String ALGOLIA_CART_LABEL_DELIVERY_VALUE;
	public static String ALGOLIA_OPTIMAL_ROUTE_DISTANCES;
    public static String ALGOLIA_DELIVERY_TIME_LABEL_CONFIG;
    public static String ALGOLIA_HOME_DEAFULT_BANNERS;
	public static String ALGOLIA_DELIVERY_TIME_LABEL_TEMPLATE;
	public static String ALGOLIA_DELIVERY_TIME_FOR_DELIVERY_TYPE;
	public static String ALGOLIA_CREATE_ORDER_CONFIG;
	public static String ALGOLIA_SCAN_AND_GO_PUSH_NOTIFICATION;
	public static String ALGOLIA_ETA_CONFIG;
	public static String TIME_OUT_CONFIG;
	public static String ALGOLIA_SCAN_AND_GO_PUSH_REGISTERED_ORDER;
	public static String ALGOLIA_DELIVERY_FAST;
	public static String ALGOLIA_EXCLUDE_STORES_CREATE_ORDER;
	public static String ALGOLIA_HOME_CONFIG;
	public static String ALGOLIA_HOME_V2_CONFIG;
	public static String ALGOLIA_GEO_GRIDS;
	public static String ALGOLIA_LANDING_PAGES;
	public static String ALGOLIA_PRODUCT_DETAIL;
	public static String ALGOLIA_AUTOCOMPLETE_CONFIG;
	public static String ALGOLIA_LOGIN_EMAIL_CONFIG;
	public static String ALGOLIA_DATA_AUTOCOMPLETE;
	public static String ALGOLIA_DATA_GEOCODING;
	public static String ALGOLIA_CONFIG_ITEM_OFFER_POPUP3X2;
	public static String ALGOLIA_CONFIG_ITEM_OFFER_POPUP2X1;
	public static String ALGOLIA_BROWSE_HISTORY_USERS;


	public static String ALGOLIA_LANDING_PAGES_PROVIDER;
	public static String ALGOLIA_LANDING_PROVIDERS_AND_ACTIVE;

	public static String ALGOLIA_CHARGE_ORDER_ACTIVE;

	public static String ALGOLIA_TALON_ONE;
	public static String ALGOLIA_PETALO_TALON_ONE;

	public static String ALGOLIA_MAIN_CITIES;

	/** Firebase */
	public static String URL_PASSWORD_FIREBASE;
	public static String URL_GET_USER_FIREBASE;

	public static String MAIN_ID_STORE;

	/** PayU */
	public static String URL_PAYU;
	public static String PAYU_API_LOGIN;
	public static String PAYU_API_KEY;

	//Firebase account Authentication
	public static String FIREBASE_AUTHENTICATION_EMAIL;

	public static String FIREBASE_AUTHENTICATION_PASSWORD;

	public static String FIREBASE_API_KEY;
	public static Integer REDIS_DB_PORT;
	public static String REDIS_DB_IP;




	/*****************************************************************/
	/** Defina aqui las propieades CONSTANTES a todos los ambientes. */
	/*****************************************************************/

	/** URLs */
	public final static String URL_PREVALIDATE_DEL_CREDITCARD_BC_30 = "customer/v1/customer/creditcard/prevalidate/{idCard}/delete";
	public final static String URL_GET_DISCOUNT_BY_CUSTOMER_BC_30   = "customer/v1/subscription-cart/thermometer/customer/{idCustomer}";

	/** OMS **/
	public final static String URL_OMS_CREATE_ORDER                 = "oms/v1/order";
	public static final String URL_OMS_EDIT_ORDERS                  = "oms/v1/order/courier";
	public static final String URL_GET_ORDER_INFO                   = "oms/v1/order/getTrackingOrder/{orderId}";
	public final static String URL_OMS_GET_ORDER_STOPS              = "oms/v1/order/{orderId}/stops";
	public final static String URL_OMS_GET_UNBILLED_ITEMS_BY_ORDER  = "oms/v1/order/{orderId}/items/unbilled";
	public final static String URL_OMS_OPTIMAL_ROUTE_CHECKOUT       = "oms/v1/order/optimalRouteCheckout";
	public final static String URL_ROUTER_OPTIMAL_ROUTE_POP_UP      = "router/v1/optimal-route/popup";
	public final static String URL_OMS_CHARGE_ORDER                 = "oms/v1/payment/chargeOrder";
	public final static String URL_OMS_GET_ORDER_COORDINATES        = "oms/v1/order/{orderId}/coordinates";
	public final static String URL_OMS_VALIDATE_FREE_DELIVERY       = "oms/v1/order/free-delivery/cart";

	public final static String URL_OMS_RELATION_ORDER_PRIME          = "oms/v1/order/prime/relations";

	public final static String URL_OMS_GET_ORDER_SUMMARY_BY_ORDER_ID= "oms/v1/order/{orderId}/summary";

	public final static String URL_OMS_GET_LAST_STATUS_ORDER_PROVIDER = "oms/v1/order/laststatusorderprovider/{orderId}/{providerId}";
	public final static String URL_OMS_GET_ORDER_INFO			    = "oms/v1/order/getTrackingOrder/{orderId}";
	public final static String URL_OMS_GET_RATINGS					= "oms/v1/order/ratings";
	public static final String URL_OMS_READ_ORDER 					= "oms/v1/order/getOrder/{orderId}";
	public static final String URL_OMS_EDIT_ORDER_FROM_COURIER      = "oms/v1/order/courier";
	public static final String URL_OMS_ORDER_QUALIFY		        = "oms/v1/order/qualifyService";
	public static final String URL_OMS_PUT_ORDER_STATUS_UPDATE      = "oms/v1/order/status/update";
	public static final String URL_OMS_PUT_ORDER_PICKING_DATE       = "oms/v1/order/orderUpdate/pickingDate";
	public static final String URL_OMS_POST_ORDER_PROVIDER_STATUS_UPDATE = "oms/v1/order/provider/status/update";
	public static final String URL_OMS_GET_ORDER_PROVIDER			= "oms/v1/order/provider/";
	public static final String URL_OMS_PUT_ORDER_PAYMENT_METHOD     = "oms/v1/order/update/paymentMethod";

	public static final String URL_OMS_GET_COURIER_ALL		        = "oms/v3/courier/all";
	public static final String URL_OMS_CANCEL_ORDER_TO_COURIER      = "oms/v3/courier/sendCancelOrderToCourier";
	public static final String URL_OMS_ORDER_SUBSCRIPTION_CENDIS_MAIL = "oms/v1/order/subscription/mail/send/cendis/{id}";
	public static final String URL_OMS_GET_ACTIVE_ORDER				= "oms/v1/order/getActiveOrder/{orderId}";
	public static final String URL_OMS_GET_ORDER_QUANTITY_ITEM		= "oms/v1/order/quantityItem/{orderId}";

	public static final String URL_OMS_GET_ORDER_INFO_ITEM		= "oms/v1/order/getInfoItemByOrder/{orderId}";
	public static final String URL_OMS_ORDER_CANCEL					= "oms/v1/order/orderCourierData";
	public static final String URL_OMS_ORDER_CANCEL_RX				= "oms/v1/order/update-status-rx";
	public static final String URL_OMS_ORDER_INFO                   = "oms/v1/order/getOrderInfo/{orderId}";
	public static final String URL_OMS_ORDER_INFO_AMPL              = "oms/v1/order/getOrderAmplitude/{orderId}";

	public static final String URL_OMS_ORDER_INFO_AMPLITUDE_BRAZE   = "oms/v1/order/getInfoOrderAmplitudeBraze/{orderId}";
	public final static String URL_OMS_VALID_COUPON		 			= "oms/v1/order/validFirstCoupon/{customerId}";
	public static final String URL_OMS_VALIDATE_COUPON              = "oms/v1/order/validateCoupon";
	public final static String URL_OMS__GET_ANTIFRAUDE_VALIDATE     = "oms/v1/order/antifraud/validate/{idCustomer}/{numberCard}";
	public final static String URL_OMS_POST_MICRO_CHARGE_GENERATE   = "oms/v1/order/micro-charge/generate";
	public final static String URL_OMS_POST_MICRO_CHARGE_VALIDATE   = "oms/v1/order/micro-charge/validate";
	public static final String URL_VALIDATE_ORDER_STATUS            = "oms/v1/order/validateLastStatus/{orderId}";

	public static final String URL_OMS_GET_TOKEN_FIREBASE            = "oms/firebase/v1/token";
	public static final String URL_OMS_GET_COURIER_UUID             = "oms/v1/order/getCourierUuid/{orderId}";
	public static final String URL_OMS_GET_DELIVERY            = "oms/v1/order/release/getOrder/{orderId}";

	/** BRAZE **/
	public static final String URL_SEND_EMAIL_BRAZE					= "braze/sendEmail";
	public static final String URL_UPLOAD_STRATUM_BRAZE             = "braze/stratum/{customerId}";
	public static final String URL_ADD_NON_STOCK_ITEM_BRAZE             	= "braze/user/addNonStockItem";
	public static final String URL_UPDATE_NOTIFICATION_PREFERENCES_BRAZE = "braze/user/notifications";
	public static final String URL_SEND_PUSH_NOTIFICATION_BRAZE     = "braze/sendPushTransactional";
	public static final String URL_UPDATE_USER_PROFILE_BRAZE     = "braze/user/profile";

	public static final String URL_SEND_EVENT_BRAZE				= "braze/v1/send-event-braze";
	public static final String URL_POST_SAVING_DATASTORE         =   "braze/savings";


	/** Prime **/
	public static final String URL_UPDATE_ATOM				= "atom/v1/customer";


	/** CRM **/
	public final static String URL_CRM_GET_CUSTOMER_PHOTOS			= "crm/v3/customer/{customerId}/photos";
	public final static String URL_CRM_GET_CUSTOMER_BY_EMAIL		= "crm/v3/customer/{email}/email";
	public final static String URL_CRM_GET_CUSTOMER_BY_EMAIL_LOWER_CASE		= "crm/v3/customer/{email}/lowercase/email/validator";
	public final static String URL_CRM_GET_CUSTOMER_EMAIL_VALIDATE	= "crm/v3/customer/email/{emailCustomer}/validate";
	public final static String URL_CRM_GET_CUSTOMER_EMAIL_VALIDATE_CALL	= "crm/v3/customer/email/{emailCustomer}/validateCall";
	public final static String URL_CRM_GET_CUSTOMER_DOCUMENT_NUMBER_VALIDATE	= "crm/v3/customer/documentNumber/{documentNumber}/validate";
	public final static String URL_CRM_POST_ALL_CUSTOMERS			= "crm/v3/customer/getAllById";
	public final static String UPDATE_CUSTOMER_ADDRESS	            = "crm/v3/customer/{customerId}/address";
	public final static String URL_CRM_CUSTOMER_ONLY				= "crm/v3/customer/customerOnly/{customerId}";

	public final static String URL_OMS_MESSENGER_NAME               = "oms/v1/order/{orderId}/messengerName";
	public final static String URL_CRM_CREATE_CUSTOMER				= "crm/v3/customer/createCustomerV2";
	public final static String URL_CRM_GET_CUSTOMER_CALL			= "crm/v3/customer/callcenter/search";
	public final static String URL_CRM_CUSTOMER 					= "crm/v3/customer";
	public final static String URL_CRM_VALIDATE_CUSTOMER  			= "crm/v3/customer/validateCustomerOracle/{email}";
	public static final String URL_CRM_IS_SAME_PASSWORD 			= "crm/v3/customer/isSamePasswordDataBase";

	public final static String URL_CRM_CUSTOMER_LOGIN_EMAIL			= "crm/v3/customer/login/email";
	public final static String URL_CRM_CUSTOMER_LOGIN_FACEBOOK		= "crm/v3/customer/login/facebook";
	public final static String URL_CRM_CUSTOMER_LOGIN_GOOGLE		= "crm/v3/customer/login/google";

	public final static String URL_CRM_GET_ADDRESS_BY_CUSTOMER  	= "crm/v3/customer/addresses/{customerId}";
	public final static String VALIDATE_ADDRESS			            = "crm/v3/customer/address/validateAddress";
	public final static String URL_CRM_DELETE_CUSTOMER_ADDRESS_BY_ID= "crm/v3/customer/address/{idAddress}";
	public final static String URL_CRM_GET_CUSTOMER_BY_ADDRESS      = "crm/v3/customer/address/{idAddress}";
	public final static String URL_CRM_CUSTOMER_ADDRESS 			= "crm/v3/customer/{customerId}/address";

	public final static String URL_CRM_UPDATE_CUSTOMER_ADDRESS_DEFAULT		= "crm/v3/customer/address/{customerId}/default/{addressId}";
	public final static String URL_CRM_CUSTOMER_FRAUD               = "crm/v3/customer/antifraud/{userId}";
	public final static String URL_CRM_CUSTOMER_CREDIT_CARD_FRAUD   = "crm/v3/customer/antifraudCreditCard/{creditCardId}";

	public final static String URL_CRM_GET_CUSTOMER_CREDIT_CARD     = "crm/v3/customer/creditCard/{customerId}";
	public final static String URL_CRM_UPDATE_EMAIL				    = "crm/v3/customer/email/update";
	public final static String URL_CRM_POST_CUSTOMER_CREDIT_CARD    = "crm/v3/customer/creditCard";
	public final static String PREVALIDATE_DELETE_CREDIT_CARD       = "crm/v3/customer/creditCard/prevalidate/creditCard/{creditCardId}";
	public final static String URL_CRM_DELETE_CREDIT_CARD_BY_ID_AND_CUSTOMER_ID = "crm/v3/customer/creditCard/{creditCardId}/{customerId}";
	public final static String URL_CRM_GET_CREDIT_CARD_TOKEN_BY_CUSTOMER_ID_AND_GATEWAY = "crm/v3/customer/creditCard/token/{gateway}/{customerId}";
	public final static String URL_CRM_GET_CREDIT_CARD_GATEWAY_ACTIVE= "crm/v3/customer/creditCard/gateway";
	public final static String URL_CRM_POST_CUSTOMER_CREDIT_CARD_DEF= "crm/v3/customer/creditCard/default";
	public final static String URL_CRM_CUSTOMER_LOGIN_DOCUMENT     = "crm/v3/customer/login/document";

	public static final String URL_CRM_POST_LIFEMILES_CUSTOMER      = "crm/v3/customer/lifeMiles";
	public static final String URL_CRM_POST_LIFEMILES_CALCULATE     = "crm/v3/customer/lifeMiles/calculate";
	public static final String URL_CRM_POST_LIFEMILES_NUMBER        = "crm/v3/customer/lifeMiles/byCustomer";
	public static final String URL_CRM_PUT_LIFEMILES_INACTIVE       = "crm/v3/customer/lifeMiles/inactive";

	public final static String URL_CRM_RESET_PASSWORD				= "crm/v3/customer/password/recover";
	public static final String URL_CRM_PUT_CUSTOMER_PASSWORD_CHANGE = "crm/v3/customer/password/change";
	public static final String URL_CRM_PUT_CUSTOMER_PASSWORD_CHANGEV2 = "crm/v3/customer/password/changeV2";

	public final static String URL_CRM_POST_ORIGIN_BY_EMAIL 		= "crm/v3/customer/origin";
	public final static String URL_CRM_POST_ORIGIN_BY_PHONE 		= "crm/v3/customer/origin/phone";
	public final static String URL_CRM_POST_ORIGIN_BY_UID			= "crm/v3/customer/origin/uid";

	public final static String URL_CRM_GET_CUSTOMER_BY_DOCUMENT 	= "crm/v3/customer/{countryId}/{documentNumber}";

	public final static String URL_CRM_GET_CUSTOMER_PRIME_CART 		= "crm/v3/customer/{customerId}/prime-cart";

	public final static String URL_CRM_GET_CUSTOMER_PRIME 			= "crm/v3/customer/{customerId}/prime-subscription";

	public final static String URL_CRM_PUT_SAVING_PRIME 			= "crm/v3/customer/prime-saving";

	public final static String URL_CRM_GET_IS_CUSTOMER_PRIME 			= "crm/v3/customer/{customerId}/prime";

	public final static String URL_CRM_SUBSCRIBE_PRIME_FREE_DAYS 	= "crm/v3/customer/subscribePrimeFreeMonthV2";
	public final static String URL_CRM_GET_SUGGESTED		 	    = "crm/v3/customer/suggested/{idSegment}";

	public final static String URL_CRM_POST_CUSTOMER_MONITOR	    = "crm/v3/customer/readCustomerMonitor";
	public final static String URL_CRM_DELETE_LOGIC_CUSTOMER	    = "crm/v3/customer/deleteLogicCustomer/{customerId}";

	public final static String URL_CRM_GET_DATA_CUSTOMER_LOGIN		= "crm/v3/customer/data/reset/password";
	public final static String URL_CRM_SEND_MAIL_CODE_LOGIN			= "crm/v3/customer/send/mail/code";
	public final static String URL_CRM_GET_PHONE_NUMBER				= "crm/v3/customer/phone/number/{idCustomer}";
	public final static String URL_CRM_FIND_CUSTOMER_DANE_CODE_CITY_BY_ID_CITY	= "crm/v3/customer/findCustomerDaneCodeCityByIdCity/{idCity}";
	// gateway - back2
	public static final String URL_BCK2_CLICK_EMAIL_UPDATE 			= "customerEmailChange";

	public final static String URL_CRM_VALIDATE_CUSTOMER_GOOGLE 	= "crm/v3/customer/validateEmailAndGoogleId";



	/** ANTIFRAUD **/
	public final static String URL_GET_CUSTOMER_BY_PHONE			= "backend/flexible/v2/antifraud/verifyDataCustomer";

	/** DATA_FOUNDATION **/

	public final static String URL_DTF_POST_VALIDATE_STOCK 			= "datafoundation/v3/item/validateStockRoute";
	public final static String URL_DTF_GET_BANNERS       			= "datafoundation/v1/banner";
	public static final String URL_DTF_ORDER_PROVIDER_STOCK_UPDATE  = "datafoundation/v3/item/provider/stock/update";
	public static final String URL_PAYMENT_METHODS_ACTIVE           = "datafoundation/v3/paymentMethod/v1/active";
	//new
	public static final String URL_GET_STORE_ACTIVE           		= "datafoundation/v3/store/active";
	public static final String URL_GET_CITY_ACTIVE           		= "datafoundation/v3/city/active";
	public static final String URL_GET_CLASSIFICATION_ACTIVE   		= "datafoundation/v3/classification/active";
	public static final String URL_GET_SHORTCUT_ACTIVE	 		    = "datafoundation/v1/shortcut/active";

	public static final String URL_GET_ITEM_BY_ID           		= "datafoundation/v3/item/{id}";
	public static final String URL_POST_GET_ITEMS           		= "datafoundation/v3/items";
	public static final String URL_POST_ITEM_START          		= "datafoundation/v3/item/start";
	public static final String URL_POST_ITEM_START_DONE        		= "datafoundation/v3/item/start/done";
	public static final String URL_GET_CROSS_SALES			   		= "datafoundation/v3/item/crossSales";
	public static final String URL_GET_ITEM_BARCODE			   		= "datafoundation/v3/item/barcode";

	public static final String URL_GET_HIGHLIGHT		   		    = "datafoundation/v3/highlight/active";
	public static final String URL_GET_OFFER_ACTIVE		   		    = "datafoundation/v3/offer/active";
	public static final String URL_GET_CLOSER_COORDINATES  		    = "datafoundation/v3/store/closer/coordinates/{latitude}/{longitude}";
	public static final String URL_GET_CREATE_PRODUCT_SAG           = "datafoundation/v3/item/createProductSag?itemBarcode=v1&idStoreGroup=v2";

	public static final String URL_POST_CREATE_PROVIDER  		    = "datafoundation/v3/external/provider/create";
	public static final String URL_GET_ACTIVE_PSE        		    = "datafoundation/v3/paymentMethod/pse";
	public static final String URL_PAYMENT_METHODS_V2        		= "datafoundation/v3/paymentMethod/v2";

	/** AUTH **/
	public static final String URL_PUT_AUTH_BY_CLIENT_ID_SECRET     = "auth/v1/token";


	/** CMS **/
	public final static String URL_CMS_BANNERS						= "backend/flexible/v2/cms/getBannersHome";
	public final static String URL_CMS_BANNERS_V2					= "datafoundation/v1/banner/getBannersHome?city={city}&category={category}&emailUser={emailUser}&isMobile={isMobile}";
	public final static String URL_BANNERS_MIN_LEFT_CMS_V2 = "datafoundation/v1/banner/getBannerMinLeft?city={city}&emailUser={emailUser}";

	/** SHOPPING CART **/
	public final static String URL_SHOPPING_PRICE_DELIVERY_ORDER 	= "shopping-cart/v1/";

	public final static String URL_ENDPOINT_STORE_STOCK 			= "store-stock/v1/";

	/** DeliveryWS */
	public static final String URL_SEND_ORDER_TO_SIM              	= "DeliveryWS/v1/order/sendOrderToSIM";
	public static final String URL_SIM_PING 						= "DeliveryWS/v1/sim/ping";
	public static final String URL_SIM_CREATE_FULFILLMENT_ORDER_DETAIL = "DeliveryWS/v1/sim/fulfillment";
	public static final String URL_RMS_PING 						= "DeliveryWS/v1/rms/ping";
	public static final String URL_RMS_CREATE_FULFIL_ORD_COL_DESC 	= "DeliveryWS/v1/rms/fulfillment";


	/*
	public static final String URL_CHARGE                           = "DeliveryWS/v1/order/charge";
	public static final String URL_EDIT_ORDERS                      = "DeliveryWS/v1/order/courier";
	public static final String URL_EDIT_ORDERS_COURIER              = "DeliveryWS/v1/order/courier/edit";
	//public static final String URL_GET_ORDER_INFO                   = "DeliveryWS/v1/order/getTrackingOrder/{idOrder}";
	public static final String URL_GET_ORDER_1_5_INFO               = "DeliveryWS/v1/order/getTrackingOrder15/{idOrder}";
	public static final String URL_ORDER_VALIDATE                   = "DeliveryWS/v1/order/validate";
	public static final String URL_ORDER                            = "DeliveryWS/v1/order";
	public static final String URL_PAYMENT_METHODS                  = "DeliveryWS/v1/paymentMeans/active";
	public static final String URL_GET_RATINGS                      = "DeliveryWS/v1/order/ratings";
	public static final String URL_QUALIFY_SERVICE                  = "DeliveryWS/v1/order/qualifyService";
	public static final String URL_GET_COURIES                      = "DeliveryWS/v1/courier/all";
	public static final String URL_ORDER_UPDATE_STATUS              = "DeliveryWS/v1/order/status/update";
	public static final String URL_ORDER_PROVIDER_UPDATE_STATUS     = "DeliveryWS/v1/order/provider/status/update";
	public static final String URL_ORDER_PROVIDER                   = "DeliveryWS/v1/order/provider/";
	public static final String URL_ORDER_PROVIDER_ITEM_STOCK_UPDATE = "DeliveryWS/v1/item/stock/update";
	public static final String URL_SEND_ORDER_TO_ROUTER             = "DeliveryWS/v1/order/sendOrderToRouter";
	public static final String URL_SEND_ORDER_TO_COURIER            = "DeliveryWS/v1/order/sendOrderToCourier";
	public static final String URL_SEND_ORDER_TO_STORES             = "DeliveryWS/v1/order/sendOrderToStores";
	public static final String URL_SEND_CANCEL_ORDER_TO_COURIER     = "DeliveryWS/v1/order/sendCancelOrderToCourier";
	public static final String URL_ORDER_SUBSCRIPTION             	= "DeliveryWS/v1/order/subscription";

	public static final String URL_SEND_ORDER_TO_RMS              	= "DeliveryWS/v1/order/sendOrderToRMS";
	public static final String URL_SEND_ORDER_TO_SIM              	= "DeliveryWS/v1/order/sendOrderToSIM";
	public static final String URL_UPDATE_PAYMENT_METHOD_ORDER      = "DeliveryWS/v1/order/update/paymentmethod";
	public static final String URL_UPDATE_ORDER                     = "DeliveryWS/v1/order/monitor";

	 */


	/** CallcenterWS */
	public static final String URL_LOGIN_EMPLOYER = "CallcenterWS/v1/chat/login";

	/** ServicesWS
	public static final String URL_VALIDATE_ADDRESS = "ServicesWS/v1/geo/address/";
	public static final String URL_STORES           = "ServicesWS/v1/store/active";
	public static final String URL_CITIES           = "ServicesWS/v1/city/active";
	public static final String URL_CLOSER_STORE     = "ServicesWS/v1/closerStore/";
	public static final String URL_SEGMENTS         = "ServicesWS/v1/segment/active";
	public static final String URL_PRODUCTS         = "ServicesWS/v1/item/";
	public static final String URL_START_PRODUCTS   = "ServicesWS/v1/item/start";
	public static final String URL_FINAL_PRODUCTS   = "ServicesWS/v1/item/done";
	public static final String URL_CATEGORIES       = "ServicesWS/v1/classification/active";
	public static final String URL_STOCK            = "ServicesWS/v1/item/stock/";
	public static final String URL_START_STOCK      = "ServicesWS/v1/item/stock/start";
	public static final String URL_STOP_STOCK       = "ServicesWS/v1/item/stock/done";
	public static final String URL_CROSS_SALES      = "ServicesWS/v1/item/crossSales?page=";
	public static final String URL_SUBSTITUTES      = "ServicesWS/v1/item/substitutes?page=";
	public static final String URL_HIGHLIGHT        = "ServicesWS/v1/highlight/active";
	public static final String URL_OFFER            = "ServicesWS/v1/offer/active";
	public static final String URL_LOCATION_LATLON  = "ServicesWS/v1/store/closer/coordinates/";
	public static final String SHORTCUT_URL         = "ServicesWS/v1/shortcut/active";
	public static final String URL_TOKEN            = "ServicesWS/v1/oauth/token/";
	public static final String URL_PROVIDER_CREATE  = "ServicesWS/v1/external/provider/create";
	*/

	/** Support Methods  */
	public static final String URL_SEND_SMS   = "https://support-methods-dot-stunning-base-164402.appspot.com/sendSms";
	public static final String URL_SEND_EMAIL = "https://support-methods-dot-stunning-base-164402.appspot.com/sendEmail";

	/** Google oauth2 */
	public static final String URL_GOOGLE_AUTH = "https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=";

	/** Monitor */
	//public static final String URL_ITEM_BARCODE                          = "ServicesWS/v1/itemByBarcode";
	//public static final String URL_ITEM_ID                               = "ServicesWS/v1/item";
	//public static final String URL_GET_ITEM_SUBSTITUTES                  = "ServicesWS/v1/getItemSubsistutes";

	public static final String URL_GET_ORDERS                            = "MonitorWS/v1/getOrders20";
	public static final String URL_GET_DETAIL_ORDER                      = "MonitorWS/v1/getOrderDetail20";
	public static final String URL_VALIDATE_BILL_ORDER                   = "MonitorWS/v1/validateBillOrder";
	public static final String URL_CANCEL_ORDER                          = "MonitorWS/v1/cancelOrder20";
	public static final String URL_VALIDATE_ORDER_TOKEN                  = "MonitorWS/v1/validateOrderToken";
	public static final String URL_SAVE_ORDER_OBSERVATION                = "MonitorWS/v1/saveOrderObservation20";
	public static final String URL_GET_ORDER_OBSERVATIONS                = "MonitorWS/v1/getOrderObservations20";
	public static final String URL_LOGIN_MONITOR_USER                    = "MonitorWS/v1/loginMonitor";
	public static final String URL_VALIDATE_SESSION_TOKEN                = "MonitorWS/v1/validateSessionToken";
	public static final String URL_RECOVERY_PASSWORD_USER                = "MonitorWS/v1/recoveryPassword";
	public static final String URL_CHANGE_PASSWORD_USER                  = "MonitorWS/v1/changePassword";
	public static final String URL_CREATE_USER                           = "MonitorWS/v1/createUserMonitor";
	public static final String URL_EDIT_USER                             = "MonitorWS/v1/editUserMonitor";
	public static final String URL_DELETE_USER                           = "MonitorWS/v1/deleteUserMonitor";
	public static final String URL_GET_ROLS_MONITOR                      = "MonitorWS/v1/getRolsMonitor";
	public static final String URL_GET_USERS_MONITOR                     = "MonitorWS/v1/getUsersMonitor";
	public static final String URL_SEARCH_ORDERS                         = "MonitorWS/v1/searchOrders20";
	public static final String URL_SEARCH_ORDERS_PROVIDERS               = "MonitorWS/v1/searchOrdersProvider";
	public static final String URL_GET_ORDERS_1_5                        = "MonitorWS/v1/getOrders15";
	public static final String URL_SEARCH_ORDERS_1_5                     = "MonitorWS/v1/searchOrders15";
	public static final String URL_SAVE_ORDER_OBSERVATION_1_5            = "MonitorWS/v1/saveOrderObservation15";
	public static final String URL_GET_ORDER_OBSERVATIONS_1_5            = "MonitorWS/v1/getOrderObservations15";
	public static final String URL_GET_DETAIL_ORDER_1_5                  = "MonitorWS/v1/getOrderDetail15";
	public static final String URL_CANCEL_ORDER_1_5                      = "MonitorWS/v1/cancelOrder15";
	public static final String URL_REASSIGN_ORDER                        = "MonitorWS/v1/reasignOrder20";
	public static final String URL_REASSIGN_ORDER_1_5                    = "MonitorWS/v1/reasignOrder15";
	public static final String URL_GET_STORES_COURIER                    = "MonitorWS/v1/getStoresByCourier";
	public static final String URL_GET_STORES_WITHOUT_COURIER            = "MonitorWS/v1/getStoresWithOutCourier";
	public static final String URL_UPDATE_STORES_WITHOUT_COURIER         = "MonitorWS/v1/updateStoresWithOutCourier";
	public static final String URL_UPDATE_STORES_COURIER                 = "MonitorWS/v1/updateStoresWithCourier";
	public static final String URL_GET_ORDER_QR_CODE                     = "MonitorWS/v1/getOrderQR";
	public static final String URL_SEARCH_ID_ORDERS                      = "MonitorWS/v1/searchIdOrders20";
	public static final String URL_SEARCH_ID_ORDERS_1_5                  = "MonitorWS/v1/searchIdOrders15";
	public static final String URL_GET_ID_ORDERS                         = "MonitorWS/v1/getIdOrders20";
	public static final String URL_GET_ID_ORDERS_1_5                     = "MonitorWS/v1/getIdOrders15";
	public static final String URL_GET_ID_ORDERS_PROGRAMMED              = "MonitorWS/v1/getIdOrdersProgrammed20";
	public static final String URL_GET_ID_ORDERS_PROVIDE                 = "MonitorWS/v1/getIdOrdersProvider20";
	public static final String URL_REPUSH_ORDER                          = "MonitorWS/v1/repushOrder";
	public static final String URL_GET_ORDER_TICKET_STORE                = "MonitorWS/v1/getOrderTicketStore";
	public static final String URL_GET_ORDER_PAYU_RESPONSE               = "MonitorWS/v1/getOrderPayUResponse";
	public static final String URL_MANAGER_ORDER                         = "MonitorWS/v1/manageOrder20";
	public static final String URL_MANAGER_ORDER_1_5                     = "MonitorWS/v1/manageOrder15";
	public static final String URL_GET_ID_ORDERS_1_5_MANAGERS            = "MonitorWS/v1/getIdOrdersGestionadas15";
	public static final String URL_GET_ID_ORDERS_MANAGERS                = "MonitorWS/v1/getIdOrdersGestionadas20";
	public static final String URL_GET_ID_ORDERS_BILLED                  = "MonitorWS/v1/getIdOrdersFacturadas20";
	public static final String URL_MANAGER_ORDER_SUMARY                  = "MonitorWS/v1/manageOrderSummary";
	public static final String URL_GET_ORDERS_SUMARY_BY_TYPE             = "MonitorWS/v1/getOrdersSummaryByType";
	public static final String URL_REASSIGN_ORDER_MANUAL                 = "MonitorWS/v1/reasignOrderManual20";
	public static final String URL_REASSIGN_ORDER_MANUAL_1_5             = "MonitorWS/v1/reasignOrderManual15";
	public static final String URL_FINALIZE_ORDER                        = "MonitorWS/v1/finalizeOrder20";
	public static final String URL_FINALIZE_ORDER_1_5                    = "MonitorWS/v1/finalizeOrder15";
	public static final String URL_COUNT_RESUME_ORDERS_2_0               = "MonitorWS/v1/getCountResumenOrders20";
	public static final String URL_GET_ID_ORDERS_NO_EXPRESS              = "MonitorWS/v1/getIdOrdersNoExpress20";
	public static final String URL_GET_ID_ORDERS_SS                      = "MonitorWS/v1/getIdOrdersSubscription20";
	public static final String URL_GET_ORDERS_ASSIGNED_MANUAL            = "MonitorWS/v1/getOrdersAssignedManual20";
	public static final String URL_GET_ORDERS_ASSIGNED_MANUAL_15         = "MonitorWS/v1/getOrdersAssignedManual15";
	public static final String URL_GET_DETAIL_ORDER_NO_EXPRESS           = "MonitorWS/v1/getOrderDetailNoExpress";
	public static final String URL_GET_ORDER_CREDIT_NOTE_TICKET          = "MonitorWS/v1/getOrderCreditNoteTicket";
	public static final String URL_MODIFY_STATUS_ORDER                   = "MonitorWS/v1/modifyOrderStatus20";
	public static final String URL_ADD_ORDER_GUIDE                       = "MonitorWS/v1/addOrderGuide";
	public static final String URL_INSERT_CLICK_WHATSAPP                 = "MonitorWS/v1/insertClickWhatsApp";
	public static final String URL_GET_REPORT_MANAGAMENT                 = "MonitorWS/v1/getReportManagamentMonitor20";
	public static final String URL_GET_REPORT_MANAGAMENT_15              = "MonitorWS/v1/getReportManagamentMonitor15";
	public static final String URL_ADD_ORDER_SUMMARY                     = "MonitorWS/v1/addOrderSummaryMonitor";
	public static final String URL_GET_POS_NUMBER_ORDER                  = "MonitorWS/v1/getPOSNumberOrder";
	public static final String URL_GET_ORDER_EVIDENCES                   = "MonitorWS/v1/getEvidencesOrder20";
	public static final String URL_GET_ORDER_EVIDENCES_15                = "MonitorWS/v1/getEvidencesOrder15";
	public static final String URL_SEARCH_ORDERS_NO_EXPRESS              = "MonitorWS/v1/searchOrdersNoExpress";
	public static final String URL_GET_ORDERS_MESSENGER_ASSIGNED         = "MonitorWS/v1/getOrdersMessengersAssigned20";
	public static final String URL_GET_ORDERS_MESSENGER_ASSIGNED_15      = "MonitorWS/v1/getOrdersMessengersAssigned15";
	public static final String URL_SEARCH_ID_ORDERS_PROVIDERS            = "MonitorWS/v1/searchIdOrdersProviders";
	public static final String URL_SEARCH_ORDERS_REPORTS                 = "MonitorWS/v1/searchOrdersReports20";
	public static final String URL_GET_ID_ORDERS_CANCELLED               = "MonitorWS/v1/getIdOrdersCanceladas20";
	public static final String URL_GET_ID_ORDERS_1_5_CANCELLED           = "MonitorWS/v1/getIdOrdersCanceladas15";
	public static final String URL_GET_ORDERS_GLUED                      = "MonitorWS/v1/getOrdersEncoladas20";
	public static final String URL_GET_ORDERS_1_5_GLUED                  = "MonitorWS/v1/getOrdersEncoladas15";
	public static final String URL_GET_IS_ORDER_REASSIGNED               = "MonitorWS/v1/isOrderReassigned20";
	public static final String URL_GET_IS_ORDER_REASSIGNED_1_5           = "MonitorWS/v1/isOrderReassigned15";
	public static final String URL_SEARCH_ORDERS_REPORTS_1_5             = "MonitorWS/v1/searchOrdersReports15";
	public static final String URL_ORDER_PROVIDER_MONITOR                = "MonitorWS/v1/getDeliveryValueProvider";
	public static final String URL_ORDER_STUCK_AVERAGE20                 = "MonitorWS/v1/getOrdersStuckAverage20";
	public static final String URL_ORDER_STUCK_AVERAGE20_CITY            = "MonitorWS/v1/getOrdersStuckAverage20ByCity";
	public static final String URL_ORDER_DELIVEY_AVERAGE20               = "MonitorWS/v1/getOrdersDeliveryTimeAverage20";
	public static final String URL_ORDER_DELIVEY_AVERAGE20_CITY          = "MonitorWS/v1/getOrdersDeliveryTimeAverage20ByCity";
	public static final String URL_ORDER_STUCK_AVERAGE15                 = "MonitorWS/v1/getOrdersStuckAverage15";
	public static final String URL_ORDER_STUCK_AVERAGE15_CITY            = "MonitorWS/v1/getOrdersStuckAverage15ByCity";
	public static final String URL_ORDER_DELIVEY_AVERAGE15               = "MonitorWS/v1/getOrdersDeliveryTimeAverage15";
	public static final String URL_ORDER_DELIVEY_AVERAGE15_CITY          = "MonitorWS/v1/getOrdersDeliveryTimeAverage15ByCity";
	public static final String URL_PUT_ORDERS_STUCK_AVERAGE              = "MonitorWS/v1/putOrdersStuckAverage";
	public static final String URL_PUT_ORDERS_STUCK_AVERAGE_CITY         = "MonitorWS/v1/putOrdersStuckAverageByCity";
	public static final String URL_PUT_ORDERS_DELIVERY_TIME_AVERAGE      = "MonitorWS/v1/putOrdersDeliveryTimeAverage";
	public static final String URL_PUT_ORDERS_DELIVERY_TIME_AVERAGE_CITY = "MonitorWS/v1/putOrdersDeliveryTimeAverageByCity";
	public static final String URL_GET_ORDERS_STUCK_AVERAGE              = "MonitorWS/v1/getOrdersStuckAverage";
	public static final String URL_GET_ORDERS_STUCK_AVERAGE_CITY         = "MonitorWS/v1/getOrdersStuckAverageByCity";
	public static final String URL_GET_ORDERS_DELIVERY_TIME_AVERAGE      = "MonitorWS/v1/getOrdersDeliveryTimeAverage";
	public static final String URL_GET_ORDERS_DELIVERY_TIME_AVERAGE_CITY = "MonitorWS/v1/getOrdersDeliveryTimeAverageByCity";
	public static final String URL_GET_ID_ORDERS_ISSUED20                = "MonitorWS/v1/getIdOrdersEmitidas20";
	public static final String URL_GET_ID_ORDERS_ISSUED15                = "MonitorWS/v1/getIdOrdersEmitidas15";
	public static final String URL_GET_ORDERS_COODINATES20               = "MonitorWS/v1/getOrderCoordinates20";
	public static final String URL_GET_ORDERS_COODINATES15               = "MonitorWS/v1/getOrderCoordinates15";
	public static final String URL_ACTIVATE_ORDER_CANCELED               = "MonitorWS/v1/activateOrderCanceled";
	public static final String URL_SAVE_ORDER_ACTIVATED_ASSOCIATION      = "MonitorWS/v1/saveOrderActivedAssociation";
	public static final String URL_GET_ORDERS_ACTIVATED_ASOCIATION       = "MonitorWS/v1/getOrderActivedAssociation";
	public static final String URL_GET_ORDERS_CANCELED_ASOCIATION        = "MonitorWS/v1/getOrderCanceledAssociation";
	public static final String URL_UPDATE_ORDER_EXPRESS_PICKING_DATE     = "MonitorWS/v1/updateOrderExpressPickingDate";
	public static final String URL_GET_EVIDENCES_ORDER20                 = "MonitorWS/v1/getEvidencesOrder20";


	public static final String URL_MONITOR_SERVER                  = "https://monitor-server-dot-web-farmatodo.appspot.com/";
	public static final String URL_SEND_CUSTOMER_ATOM              = "CustomerWS/v1/customer/atom/send";

	/** Https Methods */
	public final static String GET    = "GET";
	public final static String PUT    = "PUT";
	public final static String POST   = "POST";
	public final static String DELETE = "DELETE";

	/** Properties */
	public final static String CONTENT_TYPE_JSON = "application/json";

	/** Errors  */
	public final static String NO_CONTENT      = "No hay contenido.";
	public final static String BAD_REQUEST     = "Hay un problema en el llamado.";
	public final static String SERVER_ERROR    = "Hay un problema con el servidor.";
	public final static String DEFAULT         = "C�digo de error no identificado.";
	public final static String SUCCESS_MESSAGE = "Proceso finalizado.";

	/** Ciudades Principales */
	//public final static String MAIN_CITIES = "BAR,BOG,VVC,CHI,CTG,SMR,SOA,VUP,SOL,MED,ALI,BUC";

	/** ALGOLIA */
    public final static String ALGOLIA_MESSAGE_ITEMS_NOT_BILLED = "MESSAGE.ITEMS.NOT.BILLED";
//    public final static String ALGOLIA_TIME_FINALIZE_ORDERS		= "TIME_FINALIZE_ORDERS.DEV";
    public final static String ALGOLIA_TIME_FINALIZE_ORDERS		= "TIME_FINALIZE_ORDERS";
    public final static String ALGOLIA_USERS_CALL_CENTER		= "CUSTOMER_CALL_CENTER";
    public final static String ALGOLIA_PASSWORD_INCORRECT		= "PASSWORD_INCORRECT";
    public final static String ALGOLIA_CONFIG_LOGIN_MESSAGE		= "CONFIG_LOGIN_MESSAGE";
    public final static String ALGOLIA_APPS_URL_DEPARTMENTS    	= "APPS.URL.DEPARTMENTS";
    public final static String ALGOLIA_APPS_VERSION_CONFIG 		= "APPS.VERSION.CONFIG";
    public final static String ALGOLIA_SECURITY_WEB_CONFIG 		= "WEB.SECURITY.CONFIG";
	public final static String ALGOLIA_FILTERS_CONFIG			= "FILTERS.CONFIG";
    public final static String ALGOLIA_TIPS_CONFIG 				= "TIPS.CONFIG";

    public final static String CONFIG_EXPLO_STORE 				= "CONFIG_EXPLO_STORE";
    public final static String ALGOLIA_NOT_ALLOWED_TIPS 		= "NOT_ALLOWED_TIPS.CONFIG";
	public final static String ALGOLIA_FLAG_REGISTRY 			= "FLAG.COUNTRIES.REGISTRY";
	public final static String ALGOLIA_COUPONS_FILTER           = "COUPONS.FILTER.CONFIG";
	public final static String ALGOLIA_SMS_MESSAGGES_CONFIG     = "MESSAGGES.SMS.CONFIG";
	public final static String ALGOLIA_ORDER_MESSAGES			= "NOTIFICATION.ORDER.CONFIG.V2";
	public final static String ALGOLIA_FREE_DELIVERY         = "FREE.DELIVERY.CONFIG";
    //public final static String ALGOLIA_SECURITY_WEB_CONFIG 		= "WEB.SECURITY.CONFIG.DEV";
	public final static String ALGOLIA_FTD_PRIME_CONFIG    	= "FTD_PRIME_CONFIG_V2";
	public final static String ALGOLIA_CARRUSEL_RECOMMEND = "CARRUSEL.DEPARMENTS.CONFIG";
	public final static String ALGOLIA_PHOTO_SLURP_CONFIG = "PHOTO_SLURP_CONFIG";
	public final static String ALGOLIA_RECOMMEND_CONFIG = "RECOMMEND.ALGOLIA.CONFIG";
	public final static String MESSAGE_VALIDATE_COUPON_CONFIG = "MESSAGE_VALIDATE_COUPON_CONFIG";

	public final static String ALGOLIA_RATE_CONFIG_LIMIT = "RATE.LIMIT.LOGIN.CONFIG";


	public final static String ALGOLIA_BANNER_PRIME="HOME.SAVING.PRIME";

	//optics

	public final static String OPTICS_ADDITIONAL_INFORMATION_CONFIG = "OPTICS_ADDITIONAL_INFORMATION.CONFIG";
	public final static String PARAMETERS_LABELS_OPTICS = "PARAMETERS_LABELS_OPTICS";
	public final static String MESSAGE_CONFIG_OPTICS = "MESSAGE_CONFIG_OPTICS";

	public final static String MESSAGE_CONFIG_OPTICS_APPS = "MESSAGE_CONFIG_OPTICS_APPS";
	public final static String OPTICS_CONFIG_DELIVERY_TIME = "OPTICS_CONFIG_DELIVERY_TIME";

	/** GEOCODER **/
    public final static String GEOCODER_LUPAP_PLACEID = "geo-reference/geocoder/{placeId}";
	public final static String GEOCODER_LUPAP_PLACEID_V2 = "geo-reference/geocoder-by-id/lupap/{placeId}";
	public final static String GEOCODER = "geo-reference/geocoder";
	public final static String GEOCODER_V2 = "geo-reference/v2/geocoder";
	public final static String GEOCODER_BY_LUPAP_SERVI = "geo-reference/geocoder-by-lupap-servi";

	public final static String GEO_ZONE_BY_LAT_LNG = "geo-zones/validate-geo-zone";
	public final static String GEOCODER_INVERSO = "geo-reference/geoinverso";
	public final static String GEOCODER_INVERSO_V2 = "geo-reference/v2/geoinverso";

	public final static String GEOCODER_INVERSO_V3 = "geo-reference/v3/geoinverso";
	public final static String AUTOCOMPLETE = "autocomplete";

	/*growthBook */
	public final static String GB_CONFIG_GOOGLE_GEO_REFERENCING_CO = "google-georeferencing-co";
	public final static String FEATURE_MARKETPLACE_ACTIVE = "is-marketplace-active-co";
	public final static String CACHE_TIME_SETTINGS = "ttl-cache-algolia-recommend-co";

	public final static String GB_KEY_DELIVERY_TYPE_TIME = "delivery-type-time-prod-co";

	/** NestUtilities **/

	public static final String URL_BRAZE_CREATE_USER_BY_EMAIL       = "braze/createUserBrazeByEmail";
	public static final String URL_BRAZE_GET_UUID       = "braze/getUserIdBrazeByEmail";
	public static final String URL_LOGINBYUID = "authentication/loginByUID";
	public static final String URL_AMPLITUDE_PRODUCT_BOUGHT = "amplitude/productBougth";
	public static final String URL_AMPLITUDE_ORDER_COMPLETE = "amplitude/orderCompleted";

	public static final String URL_AMPLITUDE_ORDER_COMPLETE_V2 = "amplitude/orderCompletedV2";
	public static final String URL_AMPLITUDE_SESSION = "firebase/saveAmplitudeSessionId";
	public static final String URL_TRENDING_ITEMS = "algolia/recommend/trending-items/department";

	public static final String URL_FIREBASE_GET_CODE_LOGIN =  "firebase/getCodeLoginUser/{userId}";

	public static final String URL_FIREBASE_ADD_CODE_LOGIN = "firebase/addCodeLoginUser";

	public static final String URL_FIREBASE_DELETE_CODE_LOGIN = "firebase/deleteCodeLoginUser/{userId}";

	/** Flask - RMS **/
	public static final String URL_RMS_BUSINESS_ITEM = "/rms/business/item/{id}";
	public static final String URL_RMS_BUSINESS_ORDER = "/rms/business/itemlist";

	public static final String URL_ADD_DELIVERY_ORDER_ITEM_ASYNC = "/_ah/api/orderEndpoint/addDeliveryOrderItem";

	public static String URL_TALON_ONE = "https://talonone-dot-sandbox-domicilios-farmatodo.uk.r.appspot.com/";
	public static String URL_TALON_ONE_TRACK_EVENT = "talon-one/track-event/get";
	public static String URL_TALON_ONE_TRACK_EVENT_ITEM_PURCHASED = "talon-one/track-event/getItemPurchased";

	public static String URL_TALON_ONE_CUSTOMER_SESSION = "talon-one/customer-session/{id}";

	public static String URL_TALON_ONE_RETRIEVE_DEDUCT_DISCOUNT="discount/retrieveDeduct/{orderId}";
	//flag send errors by text sms
	public static boolean SEND_ERROR_BY_TEXT_SMS;

	public static String URL_KUSTOMER = "https://ftd-kustomer-services-voqp7ipqwq-uc.a.run.app/";


}
