package com.imaginamos.farmatodo.model.environment;

public class Production extends Enviroment{

	private static Production instance = null;

	private Production() {
		this.ENVIROMENT_NAME           				= "PROD";
		this.IP_BASE                   				= "http://35.184.58.90:11103/";
		this.IP_BASE_MONITOR                   		= "http://35.184.59.1:11103/";
		this.BACKEND_CORE_30_HOST_BASE 				= "https://sas-v30-dot-stunning-base-164402.appspot.com/";
		this.SHOPPING_DIRECT_FAIL_OVER				= "https://shopping-cart-failover-voqp7ipqwq-uc.a.run.app/";
		this.SHOPPING_DIRECT						= "https://shopping-cart-voqp7ipqwq-uc.a.run.app/";
		this.API_GATEWAY 							= "https://api-gateway-co-voqp7ipqwq-uc.a.run.app/";
		this.OMS_DIRECT								= "https://oms-voqp7ipqwq-uc.a.run.app/";
		this.DTF_DIRECT								= "https://datafoundation-voqp7ipqwq-uc.a.run.app/";
		this.CMS_DIRECT								= "https://api-gateway-co-voqp7ipqwq-uc.a.run.app/";
		this.ANTIFRAUD								= "https://antifraud-dot-stunning-base-164402.uc.r.appspot.com/";
		this.NEST_UTILITIES_DIRECT                  = "https://ftd-services-utilities-nest-voqp7ipqwq-uc.a.run.app/";
		this.ALGOLIA_PRODUCTS          				= "products";
		this.ALGOLIA_OPTICAL_PRODUCTS          		= "optical_parameters";
		this.ALGOLIA_APP_ID            				= "VCOJEYD2PO";
		this.ALGOLIA_API_KEY           				= "95b4b2df9c92207e52d472b1b66db8c8";
		this.ALGOLIA_STORE_CONFIG      				= "store_config";
		this.ALGOLIA_HOLIDAYS          				= "holidays";
		this.ALGOLIA_INDEX_PROPERTIES  				= "properties";
		this.ALGOLIA_SUBSCRIBE_AND_SAVE_CONFIG_INDEX  	= "subscribe_and_save_config";
		this.ALGOLIA_INDEX_LANDING_PAGES			= "landing_pages";
		this.ALGOLIA_INDEX_PROD_VIDEO 				= "prod_video";
		this.ALGOLIA_INDEX_ITEMS_SEO				= "items_seo";
		this.ALGOLIA_INDEX_CONFIG_SEO				= "APP.HTML.SEO_CSS";
		this.ALGOLIA_PROPERTIES                     = "FILTERS.PROD";
		//this.ALGOLIA_PAYMENTMETHODS_DELIVERYTYPE    = "PAYMENTMETHODS.DELIVERYTYPE";
		this.ALGOLIA_PAYMENTMETHODS_DELIVERYTYPE    = "PAYMENTMETHODS.DELIVERYTYPE_V2"; /// v2
		this.ALGOILIA_DISTANCE_PROPERTIES           = "MIN.DISTANCE.PROP";
		this.ALGOLIA_AUTOCOMPLETE_GOOGLE_PROPERTIES = "AUTOCOMPLETE.PROPERTIES";
		this.ALGOLIA_COURIER_SOCKET_ACTIVE          = "COURIERSOCKET.PROPERTIES";
		this.ALGOLIA_DEFAULT_STORES_BY_CITY         = "DEFAULT.STORES.BY.CITY";
		this.ALGOLIA_KEYWORDS_CONFIG         		= "KEYWORDS_CITY_CONFIG";
		this.ALGOLIA_CONFIG_ADDRESS         		= "VALIDATE.ADDRESS.CONFIG";
		this.ALGOLIA_DEFAULT_STORES_BY_ID           = "DEFAULT.STORES.BY.ID";
		this.STORE_ID_DEFAULT_OPTICS                = "STORE_ID_DEFAULT_OPTICS";

		//this.ALGOLIA_PAYMENTMETHODS_PROPERTIES      = "PAYMENTMETHODS";
		this.ALGOLIA_PAYMENTMETHODS_PROPERTIES      = "PAYMENTMETHODS_DEV";
		this.ALGOLIA_SUPPORT_NUMBERS                = "support_numbers";
		this.ALGOLIA_HTTPS_WEBSOCKET_URL            = "HTTPS.WEBSOCKET.URL";
		this.ALGOLIA_IMAGE_TRACKING                 = "ALGOLIA_IMAGE_TRACKING";
		this.ALGOLIA_MESSAGE_CONFIG_CREATE_ORDER    = "ALGOLIA_MESSAGE_CONFIG_CREATE_ORDER";
		this.ALGOLIA_HTTP_WEBSOCKET_URL             = "HTTP.WEBSOCKET.URL";
		this.ALGOLIA_INDEX_SORTED_HIGHLIGHT         = "sorted_highlight";
		this.URL_PASSWORD_FIREBASE                  = "https://firebase-farmatodo-dot-stunning-base-164402.appspot.com/_ah/api/firebaseEndpoint/changePassword";
		this.URL_GET_USER_FIREBASE                  = "https://firebase-farmatodo-dot-stunning-base-164402.appspot.com/_ah/api/firebaseEndpoint/getUserUid";
		this.URL_PAYU                               = "https://api.payulatam.com/payments-api/4.0/service.cgi";
		this.PAYU_API_LOGIN                         = "VeGmD6jC8bUhEvk";
		this.PAYU_API_KEY                           = "19s6Afo75yW1BAZ3ILk3ZreYAd";
		this.MAIN_ID_STORE                          = "26";
		this.ALGOLIA_FILTERS_PRODUCTS				= "prod_filters";
		this.ALGOLIA_CART_LABEL_DELIVERY_VALUE      = "CART.DELIVERY.LABEL.CONFIG";
		this.ALGOLIA_OPTIMAL_ROUTE_DISTANCES	    = "OPTIMAL.ROUTE.DISTANCES.CO";
        this.ALGOLIA_DELIVERY_TIME_LABEL_CONFIG     = "DELIVERY.TIME.LABEL.CONFIG";
		this.ALGOLIA_DELIVERY_TIME_LABEL_TEMPLATE   = "DELIVERY.TIME.LABEL.TEMPLATE";
		this.ALGOLIA_DELIVERY_TIME_FOR_DELIVERY_TYPE= "DELIVERY.TIME.FOR.DELIVERY.TYPE";
		this.ALGOLIA_CREATE_ORDER_CONFIG			= "CREATE.ORDER.CONFIG";
		this.ALGOLIA_SCAN_AND_GO_PUSH_NOTIFICATION  = "SCAN_AND_GO.PUSH_NOTIFICATION";
		this.ALGOLIA_ETA_CONFIG                     = "ETA.CONFIG";
		this.TIME_OUT_CONFIG                     	= "TIME.OUT.ORDER";
		this.ALGOLIA_SCAN_AND_GO_PUSH_REGISTERED_ORDER = "SCAN_AND_GO.PUSH_REGISTERED_ORDER";
		this.ALGOLIA_DELIVERY_FAST					= "DELIVERY.PUSH_NOTIFICATION_FOR_DELIVERY_FAST";
		this.ALGOLIA_EXCLUDE_STORES_CREATE_ORDER 	= "EXCLUDE.STORES.CREATEORDER";
		this.ALGOLIA_HOME_CONFIG 					= "HOME.CONFIG";
		this.ALGOLIA_HOME_V2_CONFIG 				= "HOME.V2.CONFIG";
		this.ALGOLIA_LANDING_PAGES 					= "LANDING.PAGES";
		this.ALGOLIA_PRODUCT_DETAIL 				= "PRODUCT.DETAIL";
		this.ALGOLIA_GEO_GRIDS 						= "GEOMALLAS.EXPRESS";
		this.ALGOLIA_AUTOCOMPLETE_CONFIG            = "AUTOCOMPLETE.BY.CITY.CONFIG";
		this.ALGOLIA_HOME_DEAFULT_BANNERS           = "DEFAULT.HOME.BANNERS";
		this.ALGOLIA_LOGIN_EMAIL_CONFIG           	= "LOGIN.EMAIL.CONFIG";
		this.ALGOLIA_DATA_AUTOCOMPLETE              = "data_autocomplete";
		this.ALGOLIA_DATA_GEOCODING                 = "data_geocoding";
		this.ALGOLIA_CONFIG_ITEM_OFFER_POPUP3X2     = "ALGOLIA.CONFIG.ITEM.OFFER.POPUP.3X2";
		this.ALGOLIA_CONFIG_ITEM_OFFER_POPUP2X1     = "ALGOLIA.CONFIG.ITEM.OFFER.POPUP.2X1";
		this.ALGOLIA_LANDING_PAGES_PROVIDER        	= "_LANDING_PAGES";
        this.ALGOLIA_LANDING_PROVIDERS_AND_ACTIVE 	= "LANDING_PROVIDERS_AND_ACTIVE";
        this.ALGOLIA_BROWSE_HISTORY_USERS 			= "browse_history_users";
		this.ALGOLIA_CHARGE_ORDER_ACTIVE 			= "CHARGE_ORDER";
		this.ALGOLIA_SAG_BASE_PROPERTIES 			= "SAG.BASE.PROPERTIES.V2";

		this.ALGOLIA_MAIN_CITIES					= "MAIN_CITIES";


//        this.ALGOLIA_LANDING_PROVIDERS_AND_ACTIVE 	= "LANDING_PROVIDERS_AND_ACTIVE.DEV";
		this.ALGOLIA_COUPON_POPUP                   = "COUPONS_TO_VALIDATE";
		this.ALGOLIA_PRODUCTS_SCAN_AND_GO          	= "products_sag";
		this.ALGOLIA_TALON_ONE          			= "TALON.ONE.ENABLE.CONFIG";
		this.ALGOLIA_PETALO_TALON_ONE          		= "TALON.ONE.ENABLE.PETALO.CONFIG";
		this.URL_TALON_ONE 							= "https://talonone-voqp7ipqwq-uc.a.run.app/";
		this.URL_KUSTOMER 							= "https://ftd-kustomer-services-voqp7ipqwq-uc.a.run.app/";


		// Firebase Authentication
		this.FIREBASE_AUTHENTICATION_EMAIL = "firebasebackend@stunning-base-164402.iam.gserviceaccount.com";
		this.FIREBASE_AUTHENTICATION_PASSWORD =  "c520ae2a22f1b08ff5f069e73b7934da0636e45e";
		this.FIREBASE_API_KEY = "?key=AIzaSyCUq3JkyEgJ2WJuTj-KQyaQXgY59d8z4MQ";
		this.REDIS_DB_PORT = 6379;
		this.REDIS_DB_IP = "10.225.199.59";

		//flag send errors by text sms
		this.SEND_ERROR_BY_TEXT_SMS  = true;
	}

	public static Production getInstance() {
        if (instance == null) {
        	instance = new Production();
        }
        return instance;
	}

}