package com.imaginamos.farmatodo.model.util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by mileniopc on 10/25/16.
 * Property of Imaginamos.
 */

public class Constants {
  /*------------- Configuration variables ------------*/
  public static final String GCP_PROJECT_ID = "211585366551";
  public static final String WEB_CLIENT_ID = "811161882535-t4sck5h1hnh8tl49tigpsv1a0l40o483.apps.googleusercontent.com";
  public static final String ANDROID_CLIENT_ID_DEVELOPER = "843033286668-ouk094811q1o1klj1bln03vvk8r5h5ou.apps.googleusercontent.com";
  public static final String ANDROID_CLIENT_ID_RELEASE = "211585366551-cn1dcbhhue2tsakh5gfnhg5afn0tmi29.apps.googleusercontent.com";
  public static final String ANDROID_CLIENT_ID_DEBUG = "211585366551-vjli41u11ir3l5fem5t3ocuv9mgtjn5t.apps.googleusercontent.com";
  public static final String ANDROID_AUDIENCE = ANDROID_CLIENT_ID_RELEASE;
  public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

  public static final String URL_ORDER_DETAIL = "https://www.farmatodo.com.co/detalle-orden/";
  public static final String URL_ORDER_REVIEW = "https://www.farmatodo.com.co/calificar/";
  public static final String PHONE_SCOPE = "https://www.googleapis.com/auth/user.phonenumbers.read";
  public static final String OWNER_DOMAIN = "backend.imaginamosapi.imaginamos.com";
  public static final String OWNER_NAME = "imaginamosapi";
  public final static String KEY_SECURE_CLIENT = "12345";
  public final static int ID_EFECTIVO = 1;
  public final static int ID_DATAFONO = 2;
  public final static int ID_CARD = 3;
  public final static int ID_PSE = 6;

  public final static int STOCK_ZERO_ALGOLIA = 0;

  public final static int ID_INDEX_SAVE_AND_GET_REDIS = 1;
  public final static int TIME_SAVE_REDIS_AND_SECONDS = 30;
  public final static int ID_CUSTOMER_ANONYMOUS = 0;
  public final static String CUSTOMER_ANONYMOUS = "ANONYMOUS";

  //public final static String GEO_APIKEY = "AIzaSyCr8Mr53K47c0wBTN2BcoRbymwgPhrgJWE";
  public final static String GEO_APIKEY = "AIzaSyBypENJeBU6bykF7KY5mSEUbqLf9lwwDJI";
  public final static String GEO_APIKEY_ANDROID = "AIzaSyCHlAnVWTPK_iLorKRYhXUzLLFuOXn5168";
  //public final static String APP_ID_FACEBOOK = "1866360120307988";
  public final static String APP_ID_FACEBOOK = "1783292855316938";
  //public final static String APP_SECRET_FACEBOOK = "f6cb8c2bce0f0cdef17460f060f51f6e";
  public final static String APP_SECRET_FACEBOOK = "9ced03290084a60f7431507f479aa881";
  public final static String URL_FIREBASE = "https://fcm.googleapis.com/fcm/send";
  //public final static String FIREBASE_SERVER_KEY = "AAAA47NQs90:APA91bGW0fY6S29VAS1EAWSn1vmOH8qQk-I5Rxookj9Z8oGP8Ob9_jNe1e-1CXTrRVV224o0ZnD7MnWYl0HG0smqqCzbThEqeBCiZYOlQwpR0PhhgzZseK50Wy52e3-C3Rio0LSM5GSY63AN6ywLrizzCok3ChPx-A";
  public final static String FIREBASE_SERVER_KEY = "AAAAMUN4ehc:APA91bFqicqzBiEqaG0ffC8LAni0OE_yA7k80Ql3m__EFN2-RdIciIoHEEpdnpYv1KwJSqKEpbCDYxzrhc0EdYu4JJ4GxUPKu3t7xE8DPJ5XBncFjaoWPilr0pmjsYeF-ggNi0e8QFzu";
  public final static String JWT_SECRET = "fc2aab5f-25af-465d-b67e-47879784fc19";
  public final static String DIGITAL = "MEDIOS_DIGITALES";
  public final static String[] CALL_EMAILS = {"juanc.gonzalez@farmatodo.com", "coord.farmatodo01@sistemcobro.com", "adriana.franco@farmatodo.com", "deissy.Ruiz@farmatodo.com"};
  public final static String[] CALL_PHONES = {"573108624898", "573132653074", "573164727196", "573222185686"};

  public static final String URL_ALGOLIA_RECOMMEND = "https://vcojeyd2po-dsn.algolia.net/1/indexes/*/";
  public static final String URL_TRANSACTIONAL = "https://api-transactional.farmatodo.com";
  public static final String URL_ALGOLIA_PROXY = "https://api-search.farmatodo.com";

  public static final String CLOUD_FUNCTION_SERVICE_VEN = "https://us-central1-oracle-services-vzla.cloudfunctions.net/";
  public final static String DATASTORE_KEY_PROPERTY_CODE_PHONE = "codePhone";
  public final static String DATASTORE_KEY_PROPERTY = "key";

  public final static String DATASTORE_DELIVERY_ORDER = "idFarmatodo";
  public final static String DATASTORE_IS_ACTIVE = "isActive";

  public final static String OFFER_TEXT_TWO_FOR_ONE = "2x1";
  public final static String OFFER_TEXT_THREE_FOR_TWO= "3x2";
  public final static String OFFER_TEXT_FIVE_FOR_FOUR= "5x4";


  public final static String SOURCE_CALL_CENTER= "CALLCENTER";

  public final static String CUSTOMER_ID_IS_NULL = "El Id del usuario es requerido";
  public final static String SOURCE_ANDROID= "ANDROID";
  public final static String USER_CANCEL_ORDER_DEFAULT = "prueba405@farmatodo.com";
  public final static String SOURCE_IOS= "IOS";
  public final static String SOURCE_WEB= "CALLCENTER";
  public final static String REGISTEREDBYCALL= "CALLCENTER";
  public final static String REGISTEREDBYEMAIL= "EMAIL";

  public final static String GCP_GROWTHBOOK_SECRET_ID = "growthbook-apikey";
  public final static String GROWTHBOOK_URI = "https://cdn.growthbook.io/api/features";

  /* Special letters */
  public final static String N_SPECIAL = new String(Character.toChars(0x00F1));
  public final static String A_SPECIAL = new String(Character.toChars(0x00E1));
  public final static String E_SPECIAL = new String(Character.toChars(0x00E9));
  public final static String I_SPECIAL = new String(Character.toChars(0x00ED));
  public final static String O_SPECIAL = new String(Character.toChars(0x00F3));
  public final static String U_SPECIAL = new String(Character.toChars(0x00FA));
  public final static String ADMIRATION_SPECIAL = new String(Character.toChars(0x00A1));

  /* Messages */
  public static final String LOGIN_MESSAGE = ADMIRATION_SPECIAL + "Hola, Anteriormente ya te hab" + I_SPECIAL + "as registrado con ";
//  public static final String EMAIL_MESSAGE = ". Coloca este correo y  tu contrase" + N_SPECIAL + "a en el Log in o recupera tu contrase" + new String(Character.toChars(0x00F1)) + "a si la olvidaste!";
  public static final String EMAIL_MESSAGE = " Para ingresar inicia sesi"+O_SPECIAL+"n con tu correo electr"+O_SPECIAL+"nico ";
//  public static final String GMAIL_MESSAGE = ". Presiona contin" + U_SPECIAL + "a con Gmail y entrar" + A_SPECIAL + "s sin problema!";
  public static final String GMAIL_MESSAGE = " Para ingresar presiona contin" + U_SPECIAL + "a con Gmail ";
//  public static final String FACEBOOK_MESSAGE = ". Presiona contin" + U_SPECIAL + "a con Facebook y entrar" + A_SPECIAL + "s sin problema!";
  public static final String FACEBOOK_MESSAGE = " Para ingresar presiona contin" + U_SPECIAL + "ar con Facebook ";
  public static final String APPLE_MESSAGE = " Para ingresar presiona contin" + U_SPECIAL + "ar con Apple ";
  public static final String MESSAGE_COMPLETE = "o si prefieres tambi" + E_SPECIAL + "n puedes hacerlo con tu n" + U_SPECIAL + "mero celular.";
  public static final String COUPON_VALIDATED = ADMIRATION_SPECIAL + "Listo! Has redimido tu cup" + O_SPECIAL + "n.";
  public static final String COUPON_TALONONE_VALIDATED = "Cup" + O_SPECIAL + "n ";
  public static final String COUPON_DELETE = "Haz eliminado tu cup" + O_SPECIAL + "n.";
  public static final String COUPON_NOT_DELETE = "No se pudo eliminar el cup" + O_SPECIAL + "n.";
  public static final String PROVIDER_NOT_FOUND = "No se encontr"+ O_SPECIAL +" este proveedor.";

  public static final String COUPON_VALIDATED_NO_DISCOUNT = ADMIRATION_SPECIAL + "Registro exitoso!";
  public static final String TRUE_CONFIRMATION = "Item successfully updated";
  public static final String TRUE_CONFIRMATION_DELETED_OFFER = "Offer successfully Deleted";
  public static final String CONFIRMATION_CHANGE_PASSWORD = "Se actualiz" + O_SPECIAL + " tu contrase" + N_SPECIAL + "a correctamente.";
  public static final String MESSAGE_VALIDATION_TOKEN_PHONE = "Bienvenido a Farmatodo. Por favor coloca el codigo {CODE} para completar tu registro.";

  /* Error messages */
  public final static String ERROR_DELIVERY_TYPE_NULL = "Error, delivery type es nulo.";
  public final static String ERROR_DELIVERY_TYPE_VACIO = "Error, delivery type es vacio.";
  public final static String ERROR_DELIVERY_TYPE_NO_VALIDO = "Error, delivery type no existe.";
  public final static String ERROR_ACCESS_DENIED = "Error de validaci" + O_SPECIAL + "n.";
  public final static String CODE_INVALID = "C" + O_SPECIAL + "digo de validaci" + O_SPECIAL + "n no es v" + A_SPECIAL + "lido.";
  public final static String INVALID_TOKEN = "Lo sentimos, se produjo un error. Para continuar, te recomendamos cerrar sesion y volver a iniciarla.";
  public final static String INVALID_ADDRESS_NATIONAL_0R_ENVIALOYA = "Lo sentimos, tenemos problemas con la direcci"+ O_SPECIAL +"n agregada, por favor agregarla nuevamente";
  public final static String INVALID_TOKEN_ID_WEBSAFE = "Lo sentimos, se produjo un error. Para continuar solo debes cerrar sesi" + O_SPECIAL + " y volver a iniciar... TokenIdWebSafe nulo.";
  public final static String INVALID_ADDRESS = "Lo sentimos, la direccion es invalida. Por favor, verifica la direccion y vuelve a intentar.";
  public final static String MANDATORY_PAYMENTCARD_ID = "Lo sentimos, no se pudo crear tu orden. Por favor, vuelve a intentar.";
  public final static String USER_BLOCKED = "Lo sentimos, tu cuenta de usuario ha sido bloqueada y no puedes continuar en este momento. Por favor, comunicate con soporte.";
  public final static String CREDIT_CARD_BLOCKED = "Lo sentimos, la tarjeta de credito ha sido bloqueada y no se puede continuar.";
  public final static String INVALID_PROGRAMMING = "Lo sentimos, la fecha de programacion de la orden no es valida.";
  public final static String INVALID_NATIONAL_STORE = "Lo sentimos, tu direccion no coincide con el tipo de envio seleccionado.";
  public final static String INVALID_ENVIALOYA_STORE = "Lo sentimos, tu direccion no coincide con el tipo de envio seleccionado.";
  public final static String MULTIPLE_CALL_FOR_SERVICE = "Tu solicitud ya se encuentra en proceso.";
  public final static String EMPTY_SHOPPING_CART = "Lo sentimos, se produjo un error y el carrito de compras esta vacio. Por favor, vuelve a agregar tus productos al carrito.";
  public final static String USER_NULL = "Lo sentimos, se produjo un error. Para continuar solo debes cerrar sesi\" + O_SPECIAL + \" y volver a iniciar...CustomerId nulo.";

  public final static String USER_TYPE_ERROR = "El tipo de usuario es equivocado.";
  public final static String ERROR_DE_CREDENTIAL = "Contrase" + N_SPECIAL + "a incorrecta.";
  public final static String DELIVERY_ORDER_NOT_FOUND = "Lo sentimos, no se pudo crear tu orden. Por favor, vuelve a intentar.";
  public final static String CHAT_USER_NOT_FOUND = "Usuario no encontrado.";
  public final static String CUSTOMER_NOT_FOUND = "Cliente no encontrado.";

  public final static String CUSTOMER_CREDIT_CARD_TOKENNOT_FOUND = "Token no encontrado.";
  public final static String PRODUCT_NOT_EXISTS = "Producto no encontrado.";
  public final static String PRODUCT_OUT_OF_STOCK = "Producto sin stock.";
  public final static String PRODUCT_OPTICS_NOT_EXISTS = "Lo sentimos, Producto de la optica Farmatodo no encontrado.";
  public final static String ROUTE_NOT_FOUND = "No se encontro ruta";
  public final static String OUTSTANDING_PRODUCT_NOT_EXISTS = "Productos destacados no encontrados.";
  public final static String PROFILE_NOT_FOUND = "Perfil de usuario no encontrado.";
  public final static String CUSTOMER_INITIALIZATION = "El cliente no está inicializado.";
  public final static String PHONE_NOT_CORRECT = "No se encontro un usuario con ese tel" + E_SPECIAL + "fono.";
  public final static String EMAIL_NOT_CORRECT = "El correo electrónico tiene un formato equivocado.";
  public final static String EMAIL_EXISTS = "Estimado usuario, si es primera vez que usa esta aplicaci" + O_SPECIAL + "n deber" + A_SPECIAL + " registrarse de nuevo si no valide su correo";
  public final static String EMAIL_NOT_EXISTS = "Si es la primera vez que ingresas deber"+A_SPECIAL+"s registrarte, si no, verifica tu correo o ingresa con tu n"+U_SPECIAL+"mero celular.";
  public final static String EMAIL_EXISTS_TO_TFD = "Te registraste con el correo electr"+ O_SPECIAL +"nico {customerEmail} a tr"+A_SPECIAL+"ves de {origin}. Por favor inicia sesi"+O_SPECIAL+"n por este medio." +
          "\n\nSi prefieres tambi"+E_SPECIAL+"n puedes hacerlo con tu n"+U_SPECIAL+"mero celular.";
  public final static String EMAIL_NOT_REGISTERED = "Estimado usuario, este correo no se encuentra registrado en nuestro sistema";
  public final static String OFFER_NOT_FOUND = "Esta oferta ya no est" + A_SPECIAL + " disponible";
  public final static String TERMS_ACCEPTANCE = "Debe aceptar términos y condiciones, y manejo de datos.";
  public final static String INVALID_CATEGORY = "Esa categoría no existe.";
  public final static String INVALID_DIRECTION_CREATE_BANNER = "No se puede crear un banner de este tipo.";
  public final static String ERROR_BAD_REQUEST = "Bad request farmatodo";
  public final static String INTERNAL_ERROR = "Internal server error farmatodo";
  public final static String TRACING_INITIALIZATION = "Tracing no inicializado";
  public final static String USER_NOT_FOUND = "Lo sentimos, usuario no encontrado. Para continuar solo debes cerrar sesion y volver a iniciar.";
  public final static String USER_ANONYMOUS_ERROR = "Usuario Anonimo";
  public final static String USER_NOT_FOUND_TO_PARAMETER = "No se encontr"+ O_SPECIAL +" usuario asociado a este n"+ U_SPECIAL +"mero.";
  public final static String USER_NOT_FOUND_TO_EMAIL = "El correo electr"+ O_SPECIAL + "nico no se encuentra registrado.";
  public final static String USER_NOT_FOUND_TO_PHONE = "El n" + U_SPECIAL + "mero celular no coincide con el registrado anteriormente.";
  public final static String PASSWORD_INITIALIZATION = "Password o tokens invalidos";
  public final static String PASSWORD_VALIDATE = "Recuerda ingresar una contrase" + N_SPECIAL + "a que contenga letras y n" + U_SPECIAL + "meros con m" + I_SPECIAL + "nimo 6 caracteres.";
  public final static String DEFAULT_MESSAGE = "Lo sentimos, en este momento no es posible procesar su peticion";
  public final static String DEFAULT_MESSAGE_ANONYMOUS = "Lo sentimos, en este momento no es posible procesar su peticion, Por favor revise e intente nuevamente";
  public final static String WRONG_ADDRESS = "No podemos encontrar la direccion. Por favor revise e intente nuevamente o chatee con nosotros";
  public final static String COORDINATES_ARE_NOT_VALID = "Al parecer no te encuentras cerca a una tienda.";
  public final static String BODY_NOT_INITIALIZED = " Body no inicializado";
  public final static String NO_VERSION = "No hay versión guardada";
  public final static String ERROR_COUPON_NAME_NULL = "No se ingreso nombre.";
  public final static String ERROR_COUPON_SEARCH_CUSTOMER = "No se logro encontrar un cliente con el documento suministrado.";
  public final static String ERROR_COUPON_DOCUMENT_NUMBER_NULL = "No se ingreso el dumento de identidad.";
  public final static String ERROR_COUPON_DOCUMENT_NUMBER_NOT_EXIST = "No se encontro un usuario con ese documento.";
  public final static String ERROR_COUPON_DESCRIPTION_NULL = "No se ingreso descripción.";
  public final static String ERROR_COUPON_STARTS = "No se indica si el cupón empieza después de la fecha de creación.";
  public final static String ERROR_COUPON_START_DATE = "No se ingreso la fecha inicial.";
  public final static String ERROR_COUPON_END_DATE = "No se ingreso la fecha final.";
  public final static String ERROR_COUPON_DATE_START = "No se ingreso fecha de inicio del cup" + O_SPECIAL + "n.";
  public final static String ERROR_COUPON_DATE_WRONG = "La fecha de incio es mayor a la de expiración";
  public final static String ERROR_COUPON_NOT_FOUND = "No existen Cupones";
  public final static String ERROR_COUPON_DATE_EXPIRES = "No se ingreso fecha de expiraci" + O_SPECIAL + "n del cup" + O_SPECIAL + "n.";
  public final static String ERROR_COUPON_MAX_NUMBER = "No se ingreso el n" + U_SPECIAL + "mero m" + A_SPECIAL + "ximo de usos.";
  public final static String ERROR_COUPON_DISCOUNT_VALUE = "No se ingreso el valor de descuento.";
  public final static String ERROR_COUPON_NAME = "Ya existe un cup" + O_SPECIAL + "n con ese nombre.";
  public final static String ERROR_COUPON_EXPIRES = "No se indic" + O_SPECIAL + " si este cup" + O_SPECIAL + "n expira.";
  public final static String ERROR_COUPON_LIMIT = "No se indic" + O_SPECIAL + " si este cup" + O_SPECIAL + "n tiene l" + I_SPECIAL + "mite.";
  public final static String ERROR_COUPON_DISCOUNT = "No se indic" + O_SPECIAL + " si este cup" + O_SPECIAL + "n tiene descuento.";
  public final static String ERROR_COUPON_EXISTS = "Este cup" + O_SPECIAL + "n no existe.";
  public final static String ERROR_NEW_COUPON_EXISTS = "El cup" + O_SPECIAL + "n por el que quiere reemplazar no existe.";
  public final static String ERROR_COUPON_EXPIRED = "Este cup" + O_SPECIAL + "n ha expirado.";
  public final static String ERROR_COUPON_FOR_PRIME = "Cup" + O_SPECIAL + "n no aplica para la compra de membres" + I_SPECIAL+ "a Prime.";
  public final static String ERROR_COUPON_STARTED = "Este cup" + O_SPECIAL + "n no ha entreado en vigencia.";
  public final static String ERROR_COUPON_ID_CUSTOMER = "No se ingreso idCustomerWebSafe.";
  public final static String ERROR_KEY_CLIENT = "No se indic" + O_SPECIAL + " el key client.";
  public final static String ERROR_COUPON_CLAIMED = "Ya redimiste este cup" + O_SPECIAL + "n.";
  public final static String ERROR_COUPON_SOLD = "Este cup" + O_SPECIAL + "n se ha agotado.";
  public final static String ERROR_COUPON_TYPE = "No se indic" + O_SPECIAL + " el tipo de cup" + O_SPECIAL + "n.";
  public final static String ERROR_COUPON_OFFER = "Este tipo de cup" + O_SPECIAL + "n requiere un id de oferta.";
  public final static String FRANCHISE_MESSAGE = "Este cup" + O_SPECIAL + "n es v" + A_SPECIAL + "lido " + U_SPECIAL + "nicamente para pago con Tarjeta.";
  public final static String COUPON_FIRST_PURCHASE = "Este cup" + O_SPECIAL + "n s" + O_SPECIAL + "lo es valido para tu primera compra.";
  public final static String ERROR_COUPON_VALIDATE = "Solo puedes redimir un cup" + O_SPECIAL + "n a la vez.";
  public final static String ERROR_COUPON_FILTER = "Este cup"+O_SPECIAL+ "n no es valido para esta plataforma.";
  public final static String ERROR_COUPON_FILTER_ORDER = "Lo sentimos, el cupón no es valido para esta orden. Para continuar debes eliminarlo.";
  public final static String ERROR_COUPON_DATA_FILTER = "Lo sentimos, tu orden no cumple con las condiciones requeridas para redimir el cupon. Para continuar debes eliminarlo.";
  public final static String INFO_COUPON_NO_APPLY_MARKETPLACE = "Los cupones no son aplicables a productos Marketplace";

  public final static String ERROR_COUPON_PAYMETHOD_FILTER_ORDER = "Lo sentimos, el cupon no es valido para este metodo de pago. Para continuar debes eliminarlo.";
  public final static String ERROR_COUPON_PROBLEM = "Lo sentimos, presentamos problemas para validar este cupon. Para continuar debes eliminarlo.";
  public final static String ACTIVO = "A";
  public final static String BLOQUEADO = "B";
  public final static String ELIMINADO = "E";
  public final static String CUSTOMER_LIFEMILE_VALIDATION = "El n" + U_SPECIAL + "mero lifemile es obligat" + O_SPECIAL + "rio.";
  public final static String CUSTOMER_LIFEMILE_NOT_VALID = "El n" + U_SPECIAL + "mero lifemile no es valido.";
  public final static String CUSTOMER_LIFEMILE_VALUE_VALIDATION = "El valor es obligat" + O_SPECIAL + "rio.";
  public final static String LOCATION_PLACE_ID_NOT_FOUND = "La direcci" + O_SPECIAL + "n ingresada no es v" + A_SPECIAL + "lida y no puede ser georeferenciada.";
  public final static String LOCATION_FOUND = "Direcci" + O_SPECIAL + "n valida";
  public final static String ERROR_UNAUTHORIZED = "Unauthorized";
  public final static String LOCATION_ZONE_OUT = "Fuera de zona de cobertura";
  public final static String ERROR_ZONE_OUT = "FUERZA DE ZONA";
  public final static String ERR_ADDRESS_ALREADY_EXISTS = "Existe otro registro con la misma direcci" + O_SPECIAL + "n";
  public final static String NOT_CONTENT_ADDRESS = "El usuario no tiene direcciones registradas, Te invitamos a agrega direcciones nuevamente";
  public final static String ADDRESS_ERROR_DELIVERY_TYPE = "No es posible registrar esta direccion, mal georeferenciada";
  public final static String NOT_CONTENT = "No Data Found";
  public final static String PARAM_IS_EMPTY = "Parametro vacio o nulo";
  public final static String ADDRESS_EXIST = "La direccion ingresada ya existe";

  public final static String INVALID_KEY_CLIENT = "KeyClient inv" + A_SPECIAL + "lido";
  public final static String ERROR_ADDRESS_NULL = "Error, idAddress es nulo.";
  public final static String ERROR_PAYMENT_TYPE_INVALID = "Error, paymentType es inv" + A_SPECIAL + "lido.";
  public final static String ERROR_ID_CLIENTE_NULL = "Error, idFarmatodo es nulo.";
  public final static String ERROR_ID_STORE_GROUP_NULL = "Error, idStoreGroup es nulo.";
  public final static String ERROR_SOURCE_NULL = "Error, source es nulo.";
  public final static String ERROR_PAYMENT_CARD_ID_INVALID = "Error, paymentCardId es inv" + A_SPECIAL + "lido.";
  public final static String ERROR_ITEM_LIST_NULL = "Error, itemList es nulo.";
  public final static String ERROR_QUOTAS_NULL = "Error, quotas es inv" + A_SPECIAL + "lido.";
  public final static String ERROR_CUSTOMER_PHONE_NULL = "Error, customerPhone es nulo.";
  public final static String ERROR_DISCOUNT_INVALID = "Error, discountRate es inv" + A_SPECIAL + "lido.";
  public final static String NO_ADDED_ITEMS = "NO HAY ITEMS AGREGADOS";
  public final static String INVALID_DATE = "Debe ingresar fecha mayor a la actual";
  public final static String INVALID_QUANTITY = "La cantidad debe ser mayor a cero.";
  public final static String ORDER_NO_EXISTE = "La orden no existe.";
  public final static String ORDER_ACTIVE_MODIFIED = "La orden ha sido modificada.";
  public final static String ERROR_FINALIZED_ORDER = "Ocurrio un error inesperado, intente nuevamente.";

  public final static String FAIL = "Fail";
  public final static String CUSTOMER_WEB_SAFE_ID_NULL = "Error, customer web safe id nulo";



  // tipos de domicilio.
  public static final String EXPRESS = "EXPRESS";
  public static final String ENVIO_NACIONAL = "NATIONAL";
  public static final String ENVIALO_YA = "ENVIALOYA";

  // Api URls Constants
  public static final String BASE_SI_URL = "https://sitidata-stdr.appspot.com/api/";
  public static final String BASE_GEO_GOOGLE_URL = "https://maps.googleapis.com/maps/api/";

  public static final String BASE_URL_OSRM = "https://ftd-td-osrm-server-822522689428.us-central1.run.app";

  // Store Data
  public static final double LATITUDE_STORE = 4.68197251;
  public static final double LONGITUDE_STORE = -74.04383515;
  public static final String DEFAULT_CITY = "Bogota";

  //Data for images API.
  public static final String IMAGES_API_KEY_NAME = "apiKey";
  public static final String IMAGES_ID_API_KEY_NAME = "idApiKey";
  public static final String IMAGES_PHOTO_NAME = "photo";
  public static final String IMAGES_SERVING_URL = "servingUrl";
  public static final String IMAGES_API_KEY = "197b647a-1a67-41f3-ae21-ab91aa84e523";
  public static final String IMAGES_ID_API_KEY = "ahZzfnN0dW5uaW5nLWJhc2UtMTY0NDAycmELEgdQcm9qZWN0IiQ0ZGM5NTBhYS02MWY1LTRmZjEtODIzMy03MjViOGVlZTVhNmUMCxIGQXBpS2V5IiRiODFkZGYyYS0zNjM5LTQ1NWEtOTdlZS0yOTQ0YjI2OTg3YzAM";

  public static final String PATTERN_FOR_VALIDATE_BASE64 = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";

  public static final String CLOSED_STORE = "Lo sentimos, en este momento la tienda de tu ciudad se encuentra cerrada. Te invitamos a programar tu pedido.";
  public static final String ERROR_STORE_CONFIG_NOT_FOUND = "No se encontro la configuracion de horario de la tienda. Se asume como tienda abierta y deja crear la orden para no perder la venta.";
  public static final String ERROR_CREATE_ORDER = "No fue posible crear la orden.";
  public static final String SUCCESSFULLY_CREATE_ORDER = "Orden creada exitosamente";
  public static final String PAYMENT_DECLINED_CREATE_ORDER = "Orden no creada, el pago salio rechazado o fondos insuficientes.";
  public static final String ERROR_CREATE_ORDER_ALGOLIA = "Lo sentimos, no fue posible crear la orden por fallas en nuestra plataforma con los productos agregados, te invitamos a volver a crear la orden nuevamente.";
  public static final String ERROR_CREATE_ORDER_OMS = "Lo sentimos, no fue posible crear la orden por fallas en nuestra plataforma, te invitamos a volver a crear la orden nuevamente.";
  public static final String ERROR_CREATE_ORDER_PRIME = "Lo sentimos, no fue posible crear la orden de la membresia Prime debido a fallas en nuestra plataforma. Por favor, te invitamos a intentarlo nuevamente.";
  public static final String ERROR_CREATE_ORDER_NO_PRIME = "Tu membresia Prime ha sido creada exitosamente. Sin embargo, no fue posible crear la orden con los productos adicionales. Por favor, vuelve a intentar.";
  public static final String ERROR_ADDRESS_CREATE_ORDER = "Lo sentimos, esta dirección presento problemas al momento de crear la orden, te invitamos a agregarla nuevamente.";
  public static final String ERROR_CREATE_ORDER_TIP_ONLY = "Lo sentimos, los productos que agregaste no se encuentran disponibles.";
  public static final String ERROR_CREATE_ORDER_PAYMENT = "La transacción fue rechazada por el sistema anti-fraude.";
  public static final String ERROR_CREATE_ORDER_PAYMENT_USER = "La transacción fue rechazada por el sistema anti-fraude.";

  //MONITOR
  public final static String ERROR_EMPLOYEE_NUMBER = "Debe enviar el n" + U_SPECIAL + "mero de empleado";
  public final static String ERROR_USER_PASSWORD = "Debe enviar el password del usuario";
  public final static String ERROR_TOKEN_SESSION = "Debe enviar el token de la sesi" + O_SPECIAL + "n";
  public final static String ERROR_USER_OLD_PASSWORD = "Debe enviar el password anterior del usuario";
  public final static String ERROR_USER_NEW_PASSWORD = "Debe enviar el password nuevo del usuario";
  public final static String ERROR_EMPLOYEE_NAME = "Debe enviar el nombre de empleado";
  public final static String ERROR_STORE_ID = "Debe enviar el id de la tienda";
  public final static String ERROR_ROL_USER = "Debe enviar el rol del usuario";
  public final static String ERROR_EMAIL_USER = "Debe enviar el email del usuario ";
  public final static String ERROR_BARCODE_PRODUCT = "Debe enviar el barcode del item a consultar";
  public final static String ERROR_STORE_ID_PRODUCT = "Debe enviar el store id del item a consultar";
  public final static String ERROR_ID_PRODUCT = "Debe enviar el id del item a consultar";
  public final static String ERROR_ID_ORDER = "Debe enviar el id de la orden";
  public final static String ERROR_ORDER_TOKEN = "Debe enviar el token de la orden";
  public final static String ERROR_ORDER_OBSERVATION = "Debe enviar la observaci" + O_SPECIAL + "n de la orden";
  public final static String ERROR_ORDER_BILL_ID = "Debe enviar el id de la factura";
  public final static String ERROR_ORDER_BILL_DATE = "Debe enviar la fecha de la factura";
  public final static String ERROR_ORDER_BILL_STORE_ID = "Debe enviar el id de la tienda";
  public final static String ERROR_USER_ROL = "Debe enviar el rol del usuario";
  public final static String ERROR_USER_EMAIL = "Debe enviar el correo del usuario";
  public final static String ERROR_STATUS_ORDER = "Debe enviar el estado de la orden";
  public final static String ERROR_ORDER_GUIDE = "Debe enviar el n" + U_SPECIAL + "mero de guia";
  public final static String ERROR_SEARCH_ORDER_FILTER = "Debe enviar el rango de fechas o el n" + U_SPECIAL + "mero de documento o n" + U_SPECIAL + "mero de pedido para realizar la b" + U_SPECIAL + "squeda";
  public final static String ERROR_LIST_STORE_COURIER = "Debe enviar la lista de tiendas";
  public final static String ERROR_LIST_ITEMS_ORDER = "Debe enviar la lista de items de la orden";
  public final static String ERROR_DOMICILIO_ORDER = "Debe enviar el tipo de domicilio de la orden";
  public final static String ERROR_BLOCK_USER = "Debe enviar la información para el bloqueo de usuario";
  public final static String ERROR_DELIVERY_TYPE = "Debe enviar el tipo de envio";
  public static final String CODE_SUCCESS = "200";
  public static final String CODE_NOT_FOUND_ORDER_QR = "202";
  public static final int QR_CODE_HEIGHT = 500;
  public static final int QR_CODE_WIDTH = 500;
  public final static String ERROR_NUMBER_ORDERS = "Debe enviar el n" + U_SPECIAL +"mero de " + O_SPECIAL + "rdenes";
  public final static String ERROR_TOKEN_FIREBASE_AUTH = "Debe enviar el token de firebase para autenticaci" + O_SPECIAL + "n";
  public final static String ERROR_TOKEN_FIREBASE_PUSH = "Debe enviar el token de firebase para push";
  public final static String ERROR_ORDER_TYPE = "Debe enviar el tipo de orden";
  public final static String ERROR_SEARCH_GRAPH_FILTER = "Debe enviar el rango de fechas para realizar la b" + U_SPECIAL + "squeda";
  public final static String ERROR_ORDER_UUID = "Debe enviar el uuid";
  public final static String ERROR_ORDER_PAYMENT_METHOD = "Debe enviar el método de pago";
  public final static String ERROR_CANCELLATION_REASON = "Debe enviar el id de la raz" + O_SPECIAL + "n de la cancelaci" + O_SPECIAL + "n";

  public final static String PARAM_EXPRESS = "EXPRESS";
  public final static String PARAM_MESSENGERS = "MESSENGERS";
  public final static String PARAM_GESTIONADAS = "GESTIONADAS";
  public final static String PARAM_CANCELED = "CANCELED";
  public final static String PARAM_PROGRAMMED = "PROGRAMMED";
  public final static String PARAM_SUBSCRIPTION = "SUBSCRIPTION";
  public final static String PARAM_FACTURADAS = "FACTURADAS";
  public final static String PARAM_CANCELED_NO_EXPRESS = "CANCELED_NO_EXPRESS";
  public final static String PARAM_INCOMPLETED_NO_EXPRESS = "INCOMPLETED_NO_EXPRESS";
  public final static String PARAM_CANCELED_SUBSCRIPTION = "CANCELED_SUBSCRIPTION";
  public final static String PARAM_PROVIDER = "PROVIDER";
  public final static String PARAM_NO_EXPRESS = "NO_EXPRESS";
  public final static String PARAM_CANCELED_PROVIDER = "CANCELED_PROVIDER";

  //Tracking courier
  public static final String TRACKING_SERVIENTREGA = "https://bit.ly/2vxWVAf";
  public static final String TRACKING_LIBERTY = "https://bit.ly/2Fw1Kdn";

  //SMS HABLAME
  public static final String SMS_API = "Kyu4tgmR681WysPCSAQpfo9pwJ0sKH";
  public static final String SMS_CLIENT = "10010835";
  public static final String SMS_TEXT_SEND_ORDER = "Estimado cliente su pedido de FARMATODO :order se encuentra en camino con el numero de guia :guide de :courier:tracking.";

  //Delivery Type Time
  public static final String OBJECT_ID_DELIVERY_TYPE_TIME = "DELIVERY_TYPE.TIME.PROD";
  public static final String DELIVERY_TYPE_REQUIRED = "deliveryType required";
  public static final String KEY_CLIENT_UNAUTHORIZED = "KeyClient unauthorized";
  public static final String SUCCESS = "Success";
  public static final String DELIVERY_TIME_NOT_FOUND = "Delivey times by deliveryType not found in Algolia";
  public static final String UNEXPECTED_ERROR = "Unexpected error. Try again";

  public static final String ADVISED_ITEM_INDEX = "ADVISED.ITEM.IN.CART";
  public static final String PRODUCTS_COL_INDEX = "products-colombia";

  public static final String ARROBA = "@";
  public static final String EMAIL_PATTERN = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
  public static final String INVALID_EMAIL = "Invalid email";
  public static final String REQUIRED_EMAIL = "Required email";
  public static final String MASK_6X = "xxxxxx";
  public static final String MESSAGE_MANUAL = "registro manual con correo electr\u00f3nico. Por favor inicie sesi\u00f3n por medio de ingreso manual con su correo electr\u00f3nico.";
  public static final String MESSAGE_FACEBOOK = "Facebook. Por favor inicie sesi\u00f3n por medio de Facebook.";
  public static final String MESSAGE_GMAIL = "Google. Por favor inicie sesi\u00f3n por medio de Google.";
  public static final String MESSAGE_ERROR_OBFUSCAR = "Error al ofuscar el email. Esto no es grave, el flujo no se detiene.Simplemente no se ofuscara el email.";
  public static final String MESSAGE_YOUR_REGISTER = "Usted se registr\u00f3 con el correo electr\u00f3nico ";
  public static final String MESSAGE_BY = " a trav\u00e9s de ";
  public static final String MANUAL = "Email";
  public static final String FACEBOOK = "Facebook";
  public static final String GMAIL = "Gmail";
  public static final String HOURS = "HOURS";
  public static final String DAYS = "DAYS";
  public static final String MESSAGE_MAIL_NOT_VALID = "El email no es valido";
  public static final String ERROR_MAIL_ALREADY_EXISTS = "El email que ingresado ya se encuentra registrado";
  public static final String ERROR_MAIL_CONFIG = "No se encuentra config de email";
  public static final String ERROR_MAIL_CHANGE = "Solo se permite cambiar el email una vez, por favor contacte a soporte";
  public static final String NEARBY_STORES_REGEX_GET_ERROR = "Error regex get nearby stores";
  // Origin
  public static final String ORIGIN_LIST = "ORIGIN";

  // Next payment attempt
  public final static String NEXT_PAYMENT_ATTEMPT = "NEXT.PAYMENT.ATTEMPT";

  // Main store
  public static final List<String> MAIN_STORES = Arrays.asList("26","43","52","69","62","65","51","28","64");
  public static final String MAIN_ID_STORE = "26";
  public static final Long DEFAULT_STORE_CO = 26L;
  public static final String ERROR_PAYMENT_ONLINE = "Lo sentimos, no fue posible crear la orden, tu pago en linea fue rechazado.";

  // REGEX

  public static final String mailRegexIsValid = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

  // Algolia
  public static final Integer HITS_PER_PAGE = 50;
  public static final Integer RECENTLY_VIEWED_CAROUSEL_LIMIT = 15;
  public static final Integer SUBSTITUTES_ITEMS_LIMIT = 10;

  public static final Integer MAX_RETRIES = 3;
  public static Integer ATTEMPTS = 0;

  //
  public static final String HTML_TEST_PRODUCT_DETAIL = "Jmx0O3AmZ3Q7SWRlYWwgcGFyYSBwaWVsZXMgc2Vuc2libGVzIHkgZGVsaWNhZGFzLiBGb3J0YWxlY2UgZSBoaWRyYXRhIHR1IHBpZWwsIG1hbnRlbmkmYW1wO2VhY3V0ZTtuZG9sYSBmcmVzY2EgeSBzdWF2ZSwgZXZpdGFuZG8gYSBzdSB2ZXogbGEgcGVyZGlkYSBkZSBodW1lZGFkIG5hdHVyYWwuJmx0Oy9wJmd0OyZsdDticiAvJmd0OyZsdDtoMiZndDtDYXJhY3RlciZhbXA7aWFjdXRlO3N0aWNhcyBkZWwgSGlkcmF0YW50ZSBOZXV0cm9nZW5hIEh5ZHJvIEJvb3N0IFdhdGVyIEdlbCB4IDUwIGdyJmx0Oy9oMiZndDsmbHQ7dWwmZ3Q7Jmx0O2xpJmd0O0YmYW1wO29hY3V0ZTtybXVsYSBjb24gJmFtcDthYWN1dGU7Y2lkbyBoaWFsdXImYW1wO29hY3V0ZTtuaWNvLiZsdDsvbGkmZ3Q7Jmx0O2xpJmd0O1RleHR1cmEgYXF1YSBnZWwuJmx0Oy9saSZndDsmbHQ7bGkmZ3Q7SGlkcmF0YSBsYSBwaWVsIGhhc3RhIHBvciA0OCBob3Jhcy4mbHQ7L2xpJmd0OyZsdDtsaSZndDtQaWVsIHNhbHVkYWJsZSBlIGhpZHJhdGFkYSB0b2RvcyBsb3MgZCZhbXA7aWFjdXRlO2FzLiZsdDsvbGkmZ3Q7Jmx0O2xpJmd0O1Jlc3RhYmxlY2UgbG9zIG5pdmVsZXMgZGUgYWd1YSBlbiBsYSBwaWVsLiZsdDsvbGkmZ3Q7Jmx0O2xpJmd0O0luZGljYWRvIHBhcmEgdG9kbyB0aXBvIGRlIHBpZWwuJmx0Oy9saSZndDsmbHQ7L3VsJmd0OyZsdDticiAvJmd0OyZsdDtoMyZndDtNb2RvIGRlIHVzbyZsdDsvaDMmZ3Q7Jmx0O29sJmd0OyZsdDtsaSZndDtSZWNvZ2UgdHUgY2FiZWxsby4mbHQ7L2xpJmd0OyZsdDtsaSZndDtMaW1waWEgdHUgcGllbC4mbHQ7L2xpJmd0OyZsdDtsaSZndDtBcGxpY2EgbGEgY2FudGlkYWQgYWRlY3VhZGEgZGUgcHJvZHVjdG8gZW4gbGEgeWVtYSBkZSB0dXMgZGVkb3MuJmx0Oy9saSZndDsmbHQ7bGkmZ3Q7RXNwJmFtcDthYWN1dGU7cmNlbGEgc29icmUgdHUgY2FyYSBjb24gbW92aW1pZW50b3MgY2lyY3VsYXJlcy4mbHQ7L2xpJmd0OyZsdDsvb2wmZ3Q7Jmx0O2JyIC8mZ3Q7Jmx0O2gzJmd0O0ZpY2hhIHQmYW1wO2VhY3V0ZTtjbmljYSZsdDsvaDMmZ3Q7Jmx0O3VsJmd0OyZsdDtsaSZndDtNYXJjYTogTmV1dHJvZ2VuYS4mbHQ7L2xpJmd0OyZsdDtsaSZndDtUaXBvOiBIaWRyYXRhbnRlcy4mbHQ7L2xpJmd0OyZsdDtsaSZndDtSZWdpc3RybyBJbnZpbWE6IE5TT0M3MTYzMy0xNkNPLiZsdDsvbGkmZ3Q7Jmx0O2xpJmd0O0NhbnRpZGFkOiA1MCBnci4mbHQ7L2xpJmd0OyZsdDtsaSZndDtQYSZhbXA7aWFjdXRlO3MgZGUgcHJvZHVjY2kmYW1wO29hY3V0ZTtuOiBCcmFzaWwuJmx0Oy9saSZndDsmbHQ7bGkmZ3Q7VG9kbyB0aXBvIGRlIHBpZWwuJmx0Oy9saSZndDsmbHQ7L3VsJmd0Ow==";
  public static final String HTML_TEST_PRODUCT_DETAIL2 = "PGh0bWwgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGh0bWwiPg0KPGhlYWQ+DQo8bWV0YSBodHRwLWVxdWl2PSJDb250ZW50LVR5cGUiIGNvbnRlbnQ9InRleHQvaHRtbDsgY2hhcnNldD11dGYtOCIgLz4NCjx0aXRsZT5JbmxpbmUgY3NzPC90aXRsZT4NCiANCjwvaGVhZD4NCjxib2R5IGJnY29sb3I9IiNDQ0ZGNjYiPg0KPGgxIHN0eWxlPSJ0ZXh0LWRlY29yYXRpb246dW5kZXJsaW5lO2NvbG9yOiMzMEY7ImFsaWduPSJjZW50ZXIiPkV4YW1wbGUgZm9yIElubGluZSBDU1M8L2gxPg0KPHAgc3R5bGU9ImZvbnQtZmFtaWx5OkFyaWFsLCBIZWx2ZXRpY2EsIHNhbnMtc2VyaWY7IGFsaWduOmxlZnQ7IGNvbG9yOiNGMDA7Ij5DYXNjYWRpbmcgU3R5bGUgU2hlZXQgaXMgYSBzdHlsZSBsYW5ndWFnZSB0aGF0IGRlZmluZXMgbGF5b3V0IG9mIEhUTUwgZG9jdW1lbnRzLkNTUyBwcm9wZXJ0aWVzIHN1Y2ggYXMgYmFja2dyb3VuZCwgYm9yZGVyLCBmb250LCBmbG9hdCwgZGlzcGxheSwgbWFyZ2luLCBvcGFjaXR5LCBwYWRkaW5nLCB0ZXh0LWFsaWduLCB2ZXJ0aWNhbC1hbGlnbiwgcG9zaXRpb24sIGNvbG9yIGV0Yy48L3A+DQo8aDIgYWxpZ249ImxlZnQiIHN0eWxlPSJjb2xvcjojQzBDO3RleHQtZGVjb3JhdGlvbjp1bmRlcmxpbmU7Ij5JbWFnZSBhZmZlY3RlZCB3aXRoIHN0eWxlczwvaDI+DQo8aW1nIHNyYz0iL2ltYWdlcy9pbmxpbmUtZXhhbXBsZS5wbmciIHN0eWxlPSJib3JkZXI6M3B4IHNvbGlkICMwM0Y7IHdpZHRoOjQwMHB4OyBoZWlnaHQ6MzAwcHg7DQogICAgIG1hcmdpbi1sZWZ0OjEwcHg7Ii8+DQogDQo8L2JvZHk+DQo8L2h0bWw+";
  public static final String HTML_TEST_PRODUCT_DETAIL3 = "PGgyPjxlbT5XZWxjb21lIFRvIFRoZSBCZXN0IE9ubGluZSBIVE1MIFdlYiBFZGl0b3IhPC9lbT48L2gyPg0KPHAgc3R5bGU9ImZvbnQtc2l6ZTogMS41ZW07Ij5Zb3UgY2FuIDxzdHJvbmcgc3R5bGU9ImJhY2tncm91bmQtY29sb3I6ICMzMTczOTk7IHBhZGRpbmc6IDAgNXB4OyBjb2xvcjogI2ZmZjsiPnR5cGUgeW91ciB0ZXh0PC9zdHJvbmc+IGRpcmVjdGx5IGluIHRoZSBlZGl0b3Igb3IgcGFzdGUgaXQgZnJvbSBhIFdvcmQgRG9jLCBQREYsIEV4Y2VsIGV0Yy48L3A+DQo8cCBzdHlsZT0iZm9udC1zaXplOiAxLjVlbTsiPlRoZSA8c3Ryb25nPnZpc3VhbCBlZGl0b3I8L3N0cm9uZz4gb24gdGhlIHJpZ2h0IGFuZCB0aGUgPHN0cm9uZz5zb3VyY2UgZWRpdG9yPC9zdHJvbmc+IG9uIHRoZSBsZWZ0IGFyZSBsaW5rZWQgdG9nZXRoZXIgYW5kIHRoZSBjaGFuZ2VzIGFyZSByZWZsZWN0ZWQgaW4gdGhlIG90aGVyIG9uZSBhcyB5b3UgdHlwZSEgPGltZyBzcmM9Imh0dHBzOi8vaHRtbDUtZWRpdG9yLm5ldC9pbWFnZXMvc21pbGV5LnBuZyIgYWx0PSJzbWlsZXkiIC8+PC9wPg0KPHRhYmxlIGNsYXNzPSJlZGl0b3JEZW1vVGFibGUiPg0KPHRib2R5Pg0KPHRyPg0KPHRkPjxzdHJvbmc+TmFtZTwvc3Ryb25nPjwvdGQ+DQo8dGQ+PHN0cm9uZz5DaXR5PC9zdHJvbmc+PC90ZD4NCjx0ZD48c3Ryb25nPkFnZTwvc3Ryb25nPjwvdGQ+DQo8L3RyPg0KPHRyPg0KPHRkPkpvaG48L3RkPg0KPHRkPkNoaWNhZ288L3RkPg0KPHRkPjIzPC90ZD4NCjwvdHI+DQo8dHI+DQo8dGQ+THVjeTwvdGQ+DQo8dGQ+V2lzY29uc2luPC90ZD4NCjx0ZD4xOTwvdGQ+DQo8L3RyPg0KPHRyPg0KPHRkPkFtYW5kYTwvdGQ+DQo8dGQ+TWFkaXNvbjwvdGQ+DQo8dGQ+MjI8L3RkPg0KPC90cj4NCjwvdGJvZHk+DQo8L3RhYmxlPg0KPHA+VGhpcyBpcyBhIHRhYmxlIHlvdSBjYW4gZXhwZXJpbWVudCB3aXRoLjwvcD4NCjxwPkVzdGEgZXMgdW5hIGltYWdlbjwvcD4NCjxwPjxpbWcgc3JjPSJodHRwczovL2kuYmxvZ3MuZXMvMmI2M2Y4L2FuZHJvaWR6ZS80NTBfMTAwMC5qcGciIGFsdD0iIiB3aWR0aD0iNDUwIiBoZWlnaHQ9IjI0NCIgLz48L3A+DQo8cD4mbmJzcDs8L3A+DQo8cD5JT1M8L3A+DQo8cD48aW1nIHNyYz0iaHR0cHM6Ly8xejczcTEzaDVnejkzMnBkc3o0MnUwMHEtd3BlbmdpbmUubmV0ZG5hLXNzbC5jb20vd3AtY29udGVudC91cGxvYWRzLzIwMTcvMDUvc3dpZnQtb3JnLS5qcGciIHdpZHRoPSI0MDAiIGhlaWdodD0iMjA5IiAvPjwvcD4=";

  //Talon One
  public static final String ERROR_CUSTOMER_SESSION = "Para validar los descuentos, es requerido enviar la informaci"+O_SPECIAL+"n de los Productos";
  public static final String NAME_SAMPLING="SAMPLING";
  public static final String EMAIL_COMBO="combos_talon@farmatodo.com";
  public static final String OPEN_STATUS_TALON_ONE="OPEN";
  public static final String COMBO_VIRTUAL="COMBOS VIRTUALES";

  public static final String BAG_SKU="201900072";
  public static final String BAG_CACHE_DELETION_INDEX="BAG_CACHE_DELETION_INDEX";
  public static final String CORE_LIB_TALON_CACHE_KEY="talonRequestCoreLib";

  public static final Integer DEFAULT_PAYMENT_CARD_ID = 0;
  public static final String ALREADY_EXIST_COUPON="Ya existe un cupón aplicado, eliminelo y vuelva a validar.";
  public static final String DONT_COUPON_APPLIED="Cup" + O_SPECIAL + "n no aplicado. Por favor validar con soporte.";
  public static final String KEY_COUPON_CACHE="_COUPON";
  public static final String WORD_REJECTED="rechazado";
  public static final String WORD_ERROR="error";
  public static final String VALID_COUPON_ITEMS_EMPTY="Para validar el cupón agregue productos al carrito";
  public static final Integer TIME_EXPIRE_IN_SECONDS = 86400;
  public static final Integer TIME_EXPIRE_RES_FOR_ADDRESS = 600;
  public static final Integer INDEX_REDIS_FOURTEEN = 14;
  public static final Integer INDEX_REDIS_KEY_USER_DATA_STORE = 3;
  public static final Integer TIME_REDIS_KEY_USER_DATA_STORE = 86400;
  public static final Integer INDEX_REDIS_RES_FOR_ADDRESS = 13;
  public static final String REDIS_ATTEMPTS_FOR_GOOGLE="ATTEMPTS_GOOGLE";
  public static final Integer INDEX_REDIS_ATTEMPTS_FOR_GOOGLE = 12;
  public static final String KEY_COUPON_AUTOMATIC="_AUTOMATIC_COUPON";
  public static final String KEY_OFFER_PRICE="_PRODUCTS_OFFER_PRICE";
  public static final String KEY_TOTAL_SAVE="_TOTAL_SAVE_PRODUCTS";
  public static final String KEY_DISCOUNT_TALON="_TALON";
  public static final String KEY_DISCOUNT_PRIME="_PRIME";
  public static final String KEY_DISCOUNT_RPM="_RPM";
  public static final String FARMA_CREDITS_USED_CACHE_KEY_CUSTOMER = "FARMACREDITS_USED_CUSTOMER_";
  public static final String KEY_REQUEST_TALON="request";
  public static final String KEY_RESPONSE_TALON="response";
  public static final String KEY_CUSTOMER_PRIME_TALON_ONE="CUSTOMER_PRIME_TALON_ONE";

  public static final String ITEM_ALGOLIA_WITHOUT_SUPPLIER = "Without supplier";

  public static final String REJECTION_REASON_COUPON_TALON = "REJECTION_REASON_COUPON_TALONONE_CONFIG";

  public static final String PATTERN_COMPLEX_OFFER = "#([^#]*)#";

  public static final String NEARBY_STORES_GET_REGEX  = "^\\d+(,\\d+)*$";

  public static final String ALGOLIA_TALON_ONE_BAG_ITEM= "TALON_ONE_BAG_ITEM";
  public static final String TALON_ONE_EXTENDED_BAG_ITEM = "TALON_ONE_EXTENDED_BAG_ITEM";

  public final static String ERROR_MESSAGE_CANNOT_CANCEL_MARKETPLACE_ORDERS = "Lo sentimos, no es posible cancelar tu pedido en línea. Para solicitar una cancelación, por favor comunícate con el call center.";
  public final static String ERROR_CODE_CANNOT_CANCEL_PROVIDER_ORDERS = "FTD-BACKEND-ERROR-OO1";
  public final static long PROVIDER_ID_MARKETPALCE = 1208;

  public static final String GROWTHBOOK_FEATURE_NOTIFICATIONS_ORDER= "notification-orden-config-co";
  public static final String DEFAULT_FEATURE_NOTIFICATIONS_ORDER= "DefaultNotificationOrder.json";
  public static final Double SAMPLING_PRICE = 1.0D;
}
