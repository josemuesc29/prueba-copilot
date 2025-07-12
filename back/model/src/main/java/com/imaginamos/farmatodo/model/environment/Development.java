package com.imaginamos.farmatodo.model.environment;

public class Development extends Enviroment{

	public static Development instance = null;

	private Development() {
		this.ENVIROMENT_NAME           				= "DEV";
		this.IP_BASE                   				= "http://35.202.35.31:11103/";
		this.IP_BASE_MONITOR                   		= "http://35.202.35.31:11103/";
		this.BACKEND_CORE_30_HOST_BASE 				= "https://sas-v30-dot-dev-domicilios-farmatodo.uk.r.appspot.com/";
		this.API_GATEWAY 							= "https://gateway-dot-dev-domicilios-farmatodo.uk.r.appspot.com/";
		this.OMS_DIRECT								= "https://oms-dot-dev-domicilios-farmatodo.uk.r.appspot.com/";
		this.DTF_DIRECT								= "https://datafoundation-dot-dev-domicilios-farmatodo.uc.r.appspot.com/";
		this.CMS_DIRECT								= "https://cms-dot-stunning-base-164402.appspot.com/";
		this.ANTIFRAUD								= "https://dev-dot-antifraud-dot-stunning-base-164402.uc.r.appspot.com/";
		this.NEST_UTILITIES_DIRECT                  = "https://utilities-dot-dev-domicilios-farmatodo.uk.r.appspot.com/";
		this.ALGOLIA_PRODUCTS          				= "products";
		this.ALGOLIA_APP_ID            				= "testingYA3RG061BL";
//		this.ALGOLIA_APP_ID            				= "WOLGP9ZFDO";
		this.ALGOLIA_API_KEY           				= "3884ebb0619b13dcec74002f555c8990";
//		this.ALGOLIA_API_KEY           				= "2fd1e2c3002c8cd247c63c0a1122e6d7";
		this.ALGOLIA_STORE_CONFIG      				= "store_config_dev";
		this.ALGOLIA_HOLIDAYS          				= "holidays";
		this.ALGOLIA_INDEX_PROPERTIES  				= "properties";
		this.ALGOLIA_INDEX_LANDING_PAGES			= "Landing_pages";
		this.ALGOLIA_INDEX_PROD_VIDEO 				= "prod_video";
		this.ALGOLIA_INDEX_ITEMS_SEO				= "items_seo";
		this.ALGOLIA_INDEX_CONFIG_SEO				= "APP.HTML.SEO_CSS";
		this.ALGOLIA_PROPERTIES                     = "FILTERS.PROD";
		this.ALGOLIA_PAYMENTMETHODS_DELIVERYTYPE    = "PAYMENTMETHODS.DELIVERYTYPE";
		this.ALGOILIA_DISTANCE_PROPERTIES           = "MIN.DISTANCE.PROP";
		this.ALGOLIA_AUTOCOMPLETE_GOOGLE_PROPERTIES = "AUTOCOMPLETE.PROPERTIES";
		this.ALGOLIA_COURIER_SOCKET_ACTIVE          = "COURIERSOCKET.PROPERTIES";
		this.ALGOLIA_DEFAULT_STORES_BY_CITY         = "DEFAULT.STORES.BY.CITY";
		this.ALGOLIA_KEYWORDS_CONFIG         		= "KEYWORDS_CITY_CONFIG";
		this.ALGOLIA_DEFAULT_STORES_BY_ID           = "DEFAULT.STORES.BY.ID";
		this.ALGOLIA_PAYMENTMETHODS_PROPERTIES      = "PAYMENTMETHODS";
		this.ALGOLIA_SUPPORT_NUMBERS                = "support_numbers";
		this.ALGOLIA_HTTPS_WEBSOCKET_URL            = "HTTPS.WEBSOCKET.URL";
		this.ALGOLIA_HTTP_WEBSOCKET_URL             = "HTTP.WEBSOCKET.URL";
		this.ALGOLIA_INDEX_SORTED_HIGHLIGHT         = "sorted_highlight";
		this.URL_PASSWORD_FIREBASE                  = "https://firebase-farmatodo-dot-stunning-base-164402.appspot.com/_ah/api/firebaseEndpoint/changePassword";
		this.URL_GET_USER_FIREBASE                  = "https://firebase-farmatodo-dot-stunning-base-164402.appspot.com/_ah/api/firebaseEndpoint/getUserUid";
		this.URL_PAYU                               = "https://sandbox.api.payulatam.com/payments-api/4.0/service.cgi";
		this.PAYU_API_LOGIN                         = "pRRXKOl8ikMmt9u";
		this.PAYU_API_KEY                           = "4Vj8eK4rloUd272L48hsrarnUA";
		this.MAIN_ID_STORE                          = "83";
		this.ALGOLIA_FILTERS_PRODUCTS				= "dev_filters";
		this.ALGOLIA_CART_LABEL_DELIVERY_VALUE      = "CART.DELIVERY.LABEL.CONFIG_DEV";
		this.ALGOLIA_OPTIMAL_ROUTE_DISTANCES	    = "OPTIMAL.ROUTE.DISTANCES.CO";
		this.ALGOLIA_DELIVERY_TIME_LABEL_CONFIG     = "DELIVERY.TIME.LABEL.CONFIG";
		this.ALGOLIA_DELIVERY_TIME_LABEL_TEMPLATE   = "DELIVERY.TIME.LABEL.TEMPLATE";
		this.ALGOLIA_DELIVERY_TIME_FOR_DELIVERY_TYPE= "DELIVERY.TIME.FOR.DELIVERY.TYPE";
		this.ALGOLIA_CREATE_ORDER_CONFIG			= "CREATE.ORDER.CONFIG_DEV";
		this.ALGOLIA_SCAN_AND_GO_PUSH_NOTIFICATION  = "SCAN_AND_GO.PUSH_NOTIFICATION";
		this.ALGOLIA_ETA_CONFIG                     = "ETA.CONFIG";
		this.TIME_OUT_CONFIG                     = "TIME.OUT.ORDER";
		this.ALGOLIA_SCAN_AND_GO_PUSH_REGISTERED_ORDER = "SCAN_AND_GO.PUSH_REGISTERED_ORDER";
		this.ALGOLIA_DELIVERY_FAST					= "DELIVERY.PUSH_NOTIFICATION_FOR_DELIVERY_FAST";
		this.ALGOLIA_EXCLUDE_STORES_CREATE_ORDER = "EXCLUDE.STORES.CREATEORDER";
		this.ALGOLIA_HOME_CONFIG 					= "HOME.CONFIG";
		this.ALGOLIA_HOME_V2_CONFIG 				= "HOME.V2.CONFIG";
		this.ALGOLIA_GEO_GRIDS 						= "GEOMALLAS.EXPRESS";
		this.ALGOLIA_LANDING_PAGES 					= "LANDING.PAGES";
		this.ALGOLIA_PRODUCT_DETAIL 				= "PRODUCT.DETAIL";
		this.ALGOLIA_AUTOCOMPLETE_CONFIG            = "AUTOCOMPLETE.BY.CITY.CONFIG";
		this.ALGOLIA_HOME_DEAFULT_BANNERS           = "DEFAULT.HOME.BANNERS";
		this.ALGOLIA_LOGIN_EMAIL_CONFIG           	= "LOGIN.EMAIL.CONFIG";
		this.ALGOLIA_DATA_AUTOCOMPLETE              = "data_autocomplete";
		this.ALGOLIA_DATA_GEOCODING                 = "data_geocoding";
		this.ALGOLIA_LANDING_PAGES_PROVIDER        	= "_LANDING_PAGES";
		this.ALGOLIA_LANDING_PROVIDERS_AND_ACTIVE 	= "LANDING_PROVIDERS_AND_ACTIVE";
//		this.ALGOLIA_LANDING_PROVIDERS_AND_ACTIVE 	= "LANDING_PROVIDERS_AND_ACTIVE.DEV";
		this.ALGOLIA_CONFIG_ITEM_OFFER_POPUP3X2        = "ALGOLIA.CONFIG.ITEM.OFFER.POPUP.3X2";
		this.ALGOLIA_CONFIG_ITEM_OFFER_POPUP2X1        = "ALGOLIA.CONFIG.ITEM.OFFER.POPUP.2X1";
        this.ALGOLIA_BROWSE_HISTORY_USERS 			= "browse_history_users";
		this.ALGOLIA_CHARGE_ORDER_ACTIVE 			= "CHARGE_ORDER";
		this.ALGOLIA_SAG_BASE_PROPERTIES 			= "SAG.BASE.PROPERTIES_DEV";
		this.ALGOLIA_COUPON_POPUP                   = "COUPONS_TO_VALIDATE";
		this.ALGOLIA_PRODUCTS_SCAN_AND_GO          	= "products_scan_and_go";
		this.ALGOLIA_TALON_ONE          			= "TALON.ONE.ENABLE.CONFIG";
		this.ALGOLIA_PETALO_TALON_ONE          		= "TALON.ONE.ENABLE.PETALO.CONFIG";
		this.REDIS_DB_PORT = 6379;
		this.REDIS_DB_IP = "35.239.209.17";

		//flag send errors by text sms
		this.SEND_ERROR_BY_TEXT_SMS  = false;
	}

	public static Development getInstance() {
        if (instance == null) {
        	instance = new Development();
        }
        return instance;
	}

}
