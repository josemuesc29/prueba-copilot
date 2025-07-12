package com.imaginamos.farmatodo.backend.customer;

import com.algolia.search.exceptions.AlgoliaException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.server.spi.config.*;
import com.google.api.server.spi.response.*;
import com.google.appengine.api.datastore.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.imaginamos.farmatodo.backend.OfyService;
import com.imaginamos.farmatodo.backend.Prime.PrimeUtil;
import com.imaginamos.farmatodo.backend.braze.Braze;
import com.imaginamos.farmatodo.backend.cache.CachedDataManager;
import com.imaginamos.farmatodo.backend.customer.async.ExternalDataAsync;
import com.imaginamos.farmatodo.backend.customer.async.models.DatasourcesIds;
import com.imaginamos.farmatodo.backend.customerCoupon.application.CustomerCouponManagerHandler;
import com.imaginamos.farmatodo.backend.firebase.api.FirebaseService;
import com.imaginamos.farmatodo.backend.firebase.models.NotifyCodeLogin;
import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderService;
import com.imaginamos.farmatodo.backend.product.ProductsMethods;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.backend.user.Users;
import com.imaginamos.farmatodo.backend.util.*;
import com.imaginamos.farmatodo.model.algolia.*;
import com.imaginamos.farmatodo.model.algolia.delivery.CampaignFree;
import com.imaginamos.farmatodo.model.algolia.delivery.DeliveryFree;
import com.imaginamos.farmatodo.model.algolia.delivery.VariablesFree;
import com.imaginamos.farmatodo.model.algolia.login.AlgoliaEmailConfig;
import com.imaginamos.farmatodo.model.algolia.messageconfig.MsgSmsEnum;
import com.imaginamos.farmatodo.model.algolia.tips.TipConfig;
import com.imaginamos.farmatodo.model.categories.Department;
import com.imaginamos.farmatodo.model.cms.Banner;
import com.imaginamos.farmatodo.model.cms.InfoPrivacy;
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon;
import com.imaginamos.farmatodo.model.customer.SelfCheckout;
import com.imaginamos.farmatodo.model.customer.*;
import com.imaginamos.farmatodo.model.dto.*;
import com.imaginamos.farmatodo.model.favorite.Favorite;
import com.imaginamos.farmatodo.model.favorite.FavoriteRequest;
import com.imaginamos.farmatodo.model.home.*;
import com.imaginamos.farmatodo.model.location.City;
import com.imaginamos.farmatodo.model.microCharge.MicroCharge;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.product.Highlight;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemMostSales;
import com.imaginamos.farmatodo.model.product.ItemQuery;
import com.imaginamos.farmatodo.model.provider.ElementProvider;
import com.imaginamos.farmatodo.model.provider.LandingPagesRequest;
import com.imaginamos.farmatodo.model.provider.ProviderResponse;
import com.imaginamos.farmatodo.model.provider.ProviderSections;
import com.imaginamos.farmatodo.model.store.Store;
import com.imaginamos.farmatodo.model.user.*;
import com.imaginamos.farmatodo.model.util.Property;
import com.imaginamos.farmatodo.model.util.*;
import com.imaginamos.farmatodo.networking.algolia.APIAlgolia;
import com.imaginamos.farmatodo.networking.growthbook.GrowthBookConfigLoader;
import com.imaginamos.farmatodo.networking.kustomer.KustomerService;
import com.imaginamos.farmatodo.networking.models.SendSMSCloudFunctionVenReq;
import com.imaginamos.farmatodo.networking.models.addresses.*;
import com.imaginamos.farmatodo.networking.models.algolia.WhatsAapSendMessageConfig;
import com.imaginamos.farmatodo.networking.models.authentication.LoginFirebaseReq;
import com.imaginamos.farmatodo.networking.models.authentication.LoginFirebaseRes;
import com.imaginamos.farmatodo.networking.models.braze.EmailFromTemplate;
import com.imaginamos.farmatodo.networking.models.braze.IdStoreGroupForTemplate;
import com.imaginamos.farmatodo.networking.models.braze.NotificationAndEmailBrazeRequest;
import com.imaginamos.farmatodo.networking.models.braze.NotificationBrazeRequest;
import com.imaginamos.farmatodo.networking.models.mail.SendMailReq;
import com.imaginamos.farmatodo.networking.models.shorturl.ShortUrlReq;
import com.imaginamos.farmatodo.networking.models.shorturl.ShortUrlRes;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import com.imaginamos.farmatodo.networking.services.CloudFunctionsService;
import com.imaginamos.farmatodo.networking.services.CloudFunctionsServiceSMSVen;
import com.imaginamos.farmatodo.networking.talonone.TalonOneService;
import com.imaginamos.farmatodo.networking.talonone.model.TalonAttributes;
import com.imaginamos.farmatodo.networking.util.Util;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import migration.algolia.domain.models.DepartmentColor;
import migration.algolia.domain.services.AlgoliaRecommendManagerHandler;
import migration.algolia.infrastructure.services.AlgoliaRecommendManager;
import org.apache.http.client.utils.URIBuilder;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import retrofit2.Response;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.imaginamos.farmatodo.backend.OfyService.ofy;
import static com.imaginamos.farmatodo.backend.photoSlurp.PhotoSlurpMethods.setPhotoSlurpData;
import static com.imaginamos.farmatodo.backend.photoSlurp.PhotoSlurpMethods.setPhotoSlurpGridData;
import static com.imaginamos.farmatodo.model.util.Constants.LATITUDE_STORE;
import static com.imaginamos.farmatodo.model.util.Constants.LONGITUDE_STORE;
import static com.imaginamos.farmatodo.networking.algolia.APIAlgolia.getWhatsappConfigMessage;
import static java.util.Comparator.nullsLast;


/**
 * Created by mileniopc on 10/25/16.
 * Property of Imaginamos.
 */

@Api(name = "customerEndpoint",
    version = "v1",
    apiKeyRequired = AnnotationBoolean.TRUE,
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
    description = "Stores, deletes, edits and queries for customer information, including customer identification, " +
        "email, password and token. ")
public class CustomerEndpoint {

    private static final Logger log = Logger.getLogger(Customer.class.getName());

    private Customers customers;
    private Users users;
    private Authenticate authenticate;
    private SupportMethods supportMethods;
    private ProductsMethods productsMethods;
    private final String ORDER_FINALIZED = "order_finalized";
    private FTDUtilities ftdUtilities;
    private MsgUtilAlgolia msgUtilities;
    private ExternalDataAsync externalDataAsync;

    private AlgoliaRecommendManager algoliaRecommendManager;

    private CustomerCouponManagerHandler customerCouponManager;

    private PrimeUtil primeUtil;

    public CustomerEndpoint() {
        customers = new Customers();
        users = new Users();
        authenticate = new Authenticate();
        supportMethods = new SupportMethods();
        productsMethods = new ProductsMethods();
        ftdUtilities = new FTDUtilities();
        algoliaRecommendManager = new AlgoliaRecommendManagerHandler();
        externalDataAsync = new ExternalDataAsync();
        customerCouponManager = new CustomerCouponManagerHandler();
        primeUtil = new PrimeUtil();
    }

    /**
     * Creating Customer. Insertion or association of a user to register through the platform, in their database.
     * In the process, a security token for Firebase is returned and a token for petitions through the platform.
     *
     * @param keyClient client's token
     * @param anonymous Anonymous segment+
     * @param customer  Object of class 'Customer' that contain data to store or associate of a new user (customer).
     * @return customer2 object of class CustomerJSOn that contain the created customer information
     * @throws Exception father class of all exceptions
     */


    @ApiMethod(name = "createCustomer", path = "/customerEndpoint/createCustomer", httpMethod = ApiMethod.HttpMethod.POST)
    public CustomerJSON createCustomer(@Named("keyClient") final String keyClient,
                                       @Named("anonymous") final boolean anonymous,
                                       final Customer customer,
                                       final HttpServletRequest request)
            throws Exception {

        if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        if (customer == null)
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);

        if (customer.isDataManagement() == null || customer.isTermsAndConditions() == null || !customer.isDataManagement() || !customer.isTermsAndConditions())
            throw new UnauthorizedException(Constants.TERMS_ACCEPTANCE);

        String idFacebook = null;
        GoogleAuth idGoogle = new GoogleAuth();
        idGoogle.setGoogleId(null);

        if (customer.getTokenFacebook() != null && customer.getTokenFacebook() != "") {
            FacebookClient.AccessToken accessToken = new DefaultFacebookClient().obtainAppAccessToken(Constants.APP_ID_FACEBOOK, Constants.APP_SECRET_FACEBOOK);
            FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken(), Version.VERSION_2_3);
            FacebookClient.DebugTokenInfo facebookClient1 = facebookClient.debugToken(customer.getTokenFacebook());
            idFacebook = facebookClient1.getUserId();
        }

        if (customer.getTokenGoogle() != null && !customer.getTokenGoogle().trim().isEmpty()) {
            idGoogle = googleAuth(customer.getTokenGoogle());
        }

        if (customer.getUidFirebase() == null || customer.getUidFirebase().isEmpty()) {
            if (!anonymous && idFacebook == null && idGoogle == null && (customer.getPassword() == null || customer.getPassword().isEmpty()))
                throw new BadRequestException(Constants.PASSWORD_INITIALIZATION);
        }


        if (customer.getPassword() != null && !customer.getPassword().isEmpty() && !anonymous) {
            final boolean isValidPassword = validatePassword(customer.getPassword());
            if (isValidPassword == false) {
                throw new ConflictException(Constants.PASSWORD_VALIDATE);
            }
        }

        customer.setLatitude(LATITUDE_STORE);
        customer.setLongitude(LONGITUDE_STORE);

        final String finalCityName = Constants.DEFAULT_CITY;
        CustomerJSON customerJSON = null;

        if ((Objects.isNull(customer.getEmail()) || customer.getEmail().isEmpty()) && !anonymous) {
//            log.info("Se crea correo aleatorio.");
            customer.setEmail(customer.getFirstName() + UUID.randomUUID().toString() + "@farmatodo.com");
        }

        if (Objects.nonNull(customer.getEmail())) {
            final String finalEmail = customer.getEmail().toLowerCase();
            customer.setEmail(finalEmail);

            if (!Util.isValidEmailAddress(customer.getEmail()) && !anonymous) {
                throw new ConflictException(Constants.MESSAGE_MAIL_NOT_VALID);
            }
        }

        Customer customerfinal = customer;

        if (Objects.nonNull(idFacebook))
            customerfinal.setIdFacebook(idFacebook);

        if (Objects.nonNull(idGoogle))
            customerfinal.setIdGoogle(idGoogle.getGoogleId());

        if (Objects.isNull(customer.getPassword()))
            customer.setPassword("farmatodo");

        //Optional<String> optionalUidFirebase = Optional.empty();

        if (customer.getUidFirebase() != null) {
//            log.info("firebase uid register -> " + customer.getUidFirebase());
            customerfinal.setUidFirebase(customer.getUidFirebase());
//            log.info("firebase uid register customerFinal-> " + customerfinal.getUidFirebase());
        }

        CustomerDataResponse data = new CustomerDataResponse();

        if (!anonymous) {
            final String phoneNumber = customer.getPhone().trim();
            if (customer.getDocumentNumber() != null){
                final String documentNumber = customer.getDocumentNumber().trim();
                customerfinal.setDocumentNumber(documentNumber);
            }else {
                log.info("No se envia Documento Para los usuarios de Apple");
            }
            customerfinal.setPhone(phoneNumber);
//            log.info("request backend3 register -> " + customerfinal.toStringJson());
            data = ApiGatewayService.get().createCustomer(customerfinal, TraceUtil.getXCloudTraceId(request));
//            log.info("CustomerData = " + data.toString());
        }

        if (Objects.nonNull(data.getId())) {
            customerJSON = new CustomerJSON(data, data.getId().intValue());
        }

        String documentBraze="";
        if (Objects.nonNull(data.getDocumentNumber())) {
            documentBraze=String.valueOf(data.getDocumentNumber());
            //log.info("document Braze createCustomer= " + documentBraze);
        }


        final Optional<String> optionalBrazeUUID = ApiGatewayService.get().getUUIDFromBrazeCreateUser(data.getEmail(),documentBraze, data.getPhone());

        if (optionalBrazeUUID.isPresent()) {
            customerJSON.setAnalyticsUUID(optionalBrazeUUID.get());
            TalonOneService talonOneService = new TalonOneService();
            talonOneService.updateCustomer(customerJSON);
        }

        final CustomerJSON customerJsonFinal = customerJSON;

        final long finalIdStoreGroup = anonymous ? 26 :
                this.getIdStoreGroupLatLon(customer.getLatitude(), customer.getLongitude());

        final String finalIdGoogle = idGoogle.getGoogleId();
        final String finalIdFacebook = idFacebook;

        //peticion para Kustomer
        try {
            KustomerService kustomerService = new KustomerService();
            CustomerRequestKustomer customerRequestKustomer = CustomerRequestKustomer.buildFromCustomer(customerJSON, customerfinal);
            kustomerService.sendCustomerPublisher(customerRequestKustomer);
        } catch (Exception e) {
            log.warning("Error al enviar el cliente a CREAR - publiser Kustomer -> " + e.getMessage());
        }

        try {
//            log.info("Se envia el mismo usuario");
            return getCustomerJSONNewUser(anonymous, customer, finalCityName, customerJsonFinal, (int) finalIdStoreGroup, finalIdGoogle, finalIdFacebook, customer.getUidFirebase());
        } catch (Exception e) {
            log.severe("Error al crear el cliente -> " + e.getMessage());
        }

        return customerJSON;
    }

    private CustomerJSON getCustomerJSONNewUser(@Named("anonymous") boolean anonymous, Customer customer, String finalCityName, CustomerJSON customerJsonFinal, int finalIdStoreGroup, String finalIdGoogle, String finalIdFacebook, String uidFirebase) {
        return ofy().transact(() ->
        {
            String passEnc = encrypt(customer.getPassword());

            User user = new User();
            user.setIdUser(UUID.randomUUID().toString());
            user.setRole("Customer");
            Key<User> userKey = Key.create(User.class, user.getIdUser());
            user.setIdUserWebSafe(userKey.toWebSafeString());

            Token tokenTransport = generateToken();
            Token tokenClient = new Token();
            tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
            tokenClient.setToken(tokenTransport.getToken());
            encryptToken(tokenClient);
            tokenClient.setTokenId(UUID.randomUUID().toString());
            tokenClient.setOwner(Ref.create(userKey));
            tokenClient.setTokenExpDate(tokenTransport.getTokenExp());
            Key<Token> keyToken = ofy().save().entity(tokenClient).now();
            tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
            user.setIdUserWebSafe(userKey.toWebSafeString());

            if (customer.getDeviceId() != null && customer.getFirebaseTokenDevice() != null) {
                createDevice(customer.getFirebaseTokenDevice(), customer.getDeviceId(), userKey);
            }

            CustomerJSON customer2;
            if (customerJsonFinal == null) {
                customer2 = new CustomerJSON();
                user.setId(0);
                customer2.setIdCustomerWebSafe(user.getIdUserWebSafe());
            } else {
                customer2 = customerJsonFinal;
                user.setId(customerJsonFinal.getId());
                customer2.setIdCustomerWebSafe(user.getIdUserWebSafe());
            }

            customer2.setLatitude(customer.getLatitude());
            customer2.setLongitude(customer.getLongitude());
            customer2.setCity(finalCityName);
            customer2.setToken(tokenTransport);
            HashMap<String, Object> additionalClaims = new HashMap<>();

            if (!anonymous) {
                if (customer.getTokenFacebook() == null && customer.getTokenGoogle() == null) {

                    if (customer.getEmail() == null)
                        throw new IllegalArgumentException(Constants.EMAIL_EXISTS);

                    if (Objects.nonNull(customerJsonFinal.getRegisteredBy())) {
                        customer2.setRegisteredBy(customerJsonFinal.getRegisteredBy());
                    }
                    /*if (customer.getRegisteredByCall().equals(Constants.REGISTEREDBYCALL)){
                        customer2.setRegisteredBy(Constants.REGISTEREDBYCALL);
                    }*/

                    if (!customer.getEmail().contains("@"))
                        throw new IllegalArgumentException(Constants.EMAIL_NOT_CORRECT);

                    String[] partsEmail = customer.getEmail().split("@");
                    String[] partsDomain = partsEmail[1].split("\\.");

                    if (partsDomain.length < 2)
                        throw new IllegalArgumentException(Constants.EMAIL_NOT_CORRECT);

                    savedCredentialsDS(userKey, customer.getEmail().toLowerCase());

                    createPasswordDS(passEnc, userKey);

                } else if (customer.getTokenGoogle() == null) {
                    //log.warning(finalIdFacebook);
                    user.setIdFacebook(finalIdFacebook);
                } else {
                    //log.warning(finalIdGoogle);
                    user.setIdGoogle(finalIdGoogle);
                }
            } else {
                List<Address> addressList = new ArrayList<>();
                Address address = new Address();
                addressList.add(address);
                customer2.setAddresses(addressList);
            }

            // uid firebase.

            if (uidFirebase != null && !uidFirebase.isEmpty()) {
                user.setUidFirebase(uidFirebase);
            }


            ofy().save().entity(user).now();
            if (customer2.getAddresses() != null && !customer2.getAddresses().isEmpty()) {
                for (Address address : customer2.getAddresses()) {
                    address.setLatitude(4.68197251);
                    address.setLongitude(-74.04383515);
                }
            }
            customer2.setIdStoreGroup(finalIdStoreGroup);
            log.info("Response CreateCustomer -> {" + customer2.toString() + "}");
            return customer2;
        });
    }

    private void savedCredentialsDS(Key<User> userKey, String emailUser) {
        Credential credential = new Credential();

        List<Credential> credentialList = ofy().load().type(Credential.class)
                .filter("email", emailUser)
                .filter("status", true)
                .ancestor(userKey).list();

        credentialList.stream().forEach(credentialAux -> {
            credentialAux.setStatus(false);
        });
        ofy().save().entities(credentialList).now();

//        log.info("Registrando credenciales en DS");
        credential.setIdCredential(UUID.randomUUID().toString());
        credential.setConfirmed(true);
        credential.setCreateAt(new Date());
        credential.setEmail(emailUser);
        credential.setLastLogin(new Date());
        credential.setOwner(Ref.create(userKey));
        credential.setStatus(true);
        Key<Credential> credentialKey = ofy().save().entity(credential).now();
        credential.setIdCredentialWebSafe(credentialKey.toWebSafeString());
    }

    private void createPasswordDS(String passEnc, Key<User> userKey) {
        List<UserPass> userPasswordList = ofy().load().type(UserPass.class).filter("active", true).ancestor(userKey).list();


        UserPass userPass = new UserPass();
        userPass.setIdUserPass(UUID.randomUUID().toString());
        userPass.setOwner(Ref.create(userKey));
        userPass.setCreateAt(new Date());
        userPass.setActive(true);
        userPass.setPassword(passEnc);
        Key<UserPass> userPassKey = ofy().save().entity(userPass).now();
        userPass.setIdUserPassWebSafe(userPassKey.toWebSafeString());

        if (Objects.nonNull(userPasswordList) && !userPasswordList.isEmpty()) {
            //log.warning("Inactiva contraseñas antiguas count():" + userPasswordList.size());
            userPasswordList.stream().forEach(userPassword1 -> {
                userPassword1.setActive(false);
            });
            ofy().save().entities(userPasswordList).now();
        }
    }

    @ApiMethod(name = "createCustomerCallCenter", path = "/customerEndpoint/createCustomerCallCenter", httpMethod = ApiMethod.HttpMethod.POST)
    public CustomerJSON createCustomerCallCenter(@Named("keyClient") final String keyClient,
                                                 final Customer customer,
                                                 final HttpServletRequest request)
            throws Exception {
        if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        if (customer == null)
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);

        String cityName = "";
        String idFacebook = null;
        String idGoogle = null;

        customer.setLatitude(LATITUDE_STORE);
        customer.setLongitude(LONGITUDE_STORE);

        City city = ofy().load().type(City.class).filter("name", cityName).first().now();
        if (cityName.equals("") || city == null)
            cityName = "Bogota";

        final String finalCityName = cityName;
        final String documentNumber = customer.getDocumentNumber().trim();
        final String phoneNumber = customer.getPhone().trim();

        final String password = "farmatodo";

        if (Objects.isNull(customer.getEmail()))
            customer.setEmail(customer.getFirstName() + UUID.randomUUID().toString() + "@farmatodo.com");

        Customer customerCall = customer;
        customerCall.setDocumentNumber(documentNumber);
        customerCall.setPhone(phoneNumber);
        customerCall.setPassword(password.trim());
        CustomerDataResponse data = ApiGatewayService.get().createCustomer(customerCall, TraceUtil.getXCloudTraceId(request));
//        log.info("CustomerDataResponse = " + data.toString());
        final String idFinal = String.valueOf(data.getId());

        CustomerJSON customerJSON = new CustomerJSON(data, Integer.valueOf(idFinal));
        final CustomerJSON customerJsonFinal = customerJSON;

        final long finalIdStoreGroup = this.getIdStoreGroupLatLon(customer.getLatitude(), customer.getLongitude());
        final String finalIdGoogle = idGoogle;
        final String finalIdFacebook = idFacebook;

        try {
            return ofy().transact(() ->
            {
                final String pass = "farmatodo";
                String passEnc = encrypt(pass);

                User user = new User();
                user.setIdUser(UUID.randomUUID().toString());
                user.setRole("Customer");
                Key<User> userKey = Key.create(User.class, user.getIdUser());
                user.setIdUserWebSafe(userKey.toWebSafeString());

                Token tokenTransport = generateToken();
                Token tokenClient = new Token();
                tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
                tokenClient.setToken(tokenTransport.getToken());
                encryptToken(tokenClient);
                tokenClient.setTokenId(UUID.randomUUID().toString());
                tokenClient.setOwner(Ref.create(userKey));
                tokenClient.setTokenExpDate(tokenTransport.getTokenExp());
                Key<Token> keyToken = ofy().save().entity(tokenClient).now();
                tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
                user.setIdUserWebSafe(userKey.toWebSafeString());

                if (customer.getDeviceId() != null && customer.getFirebaseTokenDevice() != null) {
                    //log.warning("DEVICE");
                    createDevice(customer.getFirebaseTokenDevice(), customer.getDeviceId(), userKey);
                }

                CustomerJSON customer2;
                if (customerJsonFinal == null) {
                    customer2 = new CustomerJSON();
                    user.setId(0);
                    customer2.setIdCustomerWebSafe(user.getIdUserWebSafe());
                } else {
                    customer2 = customerJsonFinal;
                    user.setId(customerJsonFinal.getId());
                    customer2.setIdCustomerWebSafe(user.getIdUserWebSafe());

                }
                customer2.setLatitude(customer.getLatitude());
                customer2.setLongitude(customer.getLongitude());
                customer2.setCity(finalCityName);
                customer2.setToken(tokenTransport);

                if (customer.getTokenFacebook() == null && customer.getTokenGoogle() == null) {
                    if (customer.getEmail() == null)
                        throw new IllegalArgumentException(Constants.EMAIL_EXISTS);

                    if (!customer.getEmail().contains("@"))
                        throw new IllegalArgumentException(Constants.EMAIL_NOT_CORRECT);

                    String[] partsEmail = customer.getEmail().split("@");
                    String[] partsDomain = partsEmail[1].split("\\.");

                    if (partsDomain.length < 2)
                        throw new IllegalArgumentException(Constants.EMAIL_NOT_CORRECT);

                    savedCredentialsDS(userKey, customer.getEmail().toLowerCase());

                    createPasswordDS(passEnc, userKey);
                } else if (customer.getTokenGoogle() == null) {
                    //log.warning(finalIdFacebook);
                    user.setIdFacebook(finalIdFacebook);
                } else {
                    //log.warning(finalIdGoogle);
                    user.setIdGoogle(finalIdGoogle);
                }
                ofy().save().entity(user).now();
                if (customer2.getAddresses() != null && !customer2.getAddresses().isEmpty()) {
                    for (int i = 0; i < customer2.getAddresses().size(); i++) {
                        customer2.getAddresses().get(i).setLatitude(4.68197251);
                        customer2.getAddresses().get(i).setLongitude(-74.04383515);
                    }
                }

                String documentBraze="";
                if (Objects.nonNull(data.getDocumentNumber())) {
                    documentBraze=String.valueOf(data.getDocumentNumber());
                    //log.info("document Braze createCustomer= " + documentBraze);
                }

                final Optional<String> optionalBrazeUUID = ApiGatewayService.get().getUUIDFromBrazeCreateUser(data.getEmail(),documentBraze, data.getPhone());

                if (optionalBrazeUUID.isPresent()) {
                    customerJSON.setAnalyticsUUID(optionalBrazeUUID.get());
                    TalonOneService talonOneService = new TalonOneService();
                    talonOneService.updateCustomer(customerJSON);
                }

                customer2.setIdStoreGroup((int) finalIdStoreGroup);
                return customer2;
            });
        } catch (Exception e) {
            log.severe("Error al crear el cliente -> " + e.getMessage());
        }
        return customerJSON;
    }

    private CustomerJSON createCustomer(final Customer customer, CustomerJSON customerJsonFinal, Boolean anonymous, String finalCityName, String finalIdFacebook, String finalIdGoogle, Integer finalIdStoreGroup) {
        try {
            return ofy().transact(() ->
            {
                String passEnc = encrypt(customer.getPassword());

                User user = new User();
                user.setIdUser(UUID.randomUUID().toString());
                user.setRole("Customer");
                Key<User> userKey = Key.create(User.class, user.getIdUser());
                user.setIdUserWebSafe(userKey.toWebSafeString());

                Token tokenTransport = generateToken();
                Token tokenClient = new Token();
                tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
                tokenClient.setToken(tokenTransport.getToken());
                encryptToken(tokenClient);
                tokenClient.setTokenId(UUID.randomUUID().toString());
                tokenClient.setOwner(Ref.create(userKey));
                tokenClient.setTokenExpDate(tokenTransport.getTokenExp());
                Key<Token> keyToken = ofy().save().entity(tokenClient).now();
                tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
                user.setIdUserWebSafe(userKey.toWebSafeString());

                if (customer.getDeviceId() != null && customer.getFirebaseTokenDevice() != null) {
                    //log.warning("DEVICE");
                    createDevice(customer.getFirebaseTokenDevice(), customer.getDeviceId(), userKey);
                }

                CustomerJSON customer2;
                if (customerJsonFinal == null) {
                    customer2 = new CustomerJSON();
                    user.setId(0);
                    customer2.setIdCustomerWebSafe(user.getIdUserWebSafe());
                } else {
                    customer2 = customerJsonFinal;
                    user.setId(customerJsonFinal.getId());
                    customer2.setIdCustomerWebSafe(user.getIdUserWebSafe());

                }
                customer2.setLatitude(customer.getLatitude());
                customer2.setLongitude(customer.getLongitude());
                customer2.setCity(finalCityName);
                customer2.setToken(tokenTransport);
                HashMap<String, Object> additionalClaims = new HashMap<>();
                //String tokenFirebase = FirebaseAuth.getInstance().createCustomToken(user.getIdUser(), additionalClaims);
                //user.setTokenFirebase(tokenFirebase);

                if (!anonymous) {
                    if (customer.getTokenFacebook() == null && customer.getTokenGoogle() == null) {
                        if (customer.getEmail() == null)
                            throw new IllegalArgumentException(Constants.EMAIL_EXISTS);

                        if (!customer.getEmail().contains("@"))
                            throw new IllegalArgumentException(Constants.EMAIL_NOT_CORRECT);

                        String[] partsEmail = customer.getEmail().split("@");
                        String[] partsDomain = partsEmail[1].split("\\.");

                        if (partsDomain.length < 2)
                            throw new IllegalArgumentException(Constants.EMAIL_NOT_CORRECT);

                        savedCredentialsDS(userKey, customer.getEmail().toLowerCase());

                        createPasswordDS(passEnc, userKey);
                    } else if (customer.getTokenGoogle() == null) {
                        //log.warning(finalIdFacebook);
                        user.setIdFacebook(finalIdFacebook);
                    } else {
                        //log.warning(finalIdGoogle);
                        user.setIdGoogle(finalIdGoogle);
                    }
                } else {
                    List<Address> addressList = new ArrayList<>();
                    Address address = new Address();
                    addressList.add(address);
                    customer2.setAddresses(addressList);
                }
                ofy().save().entity(user).now();
                if (customer2.getAddresses() != null && !customer2.getAddresses().isEmpty()) {
                    for (Address address : customer2.getAddresses()) {
                        address.setLatitude(4.68197251);
                        address.setLongitude(-74.04383515);
                    }
                }
                customer2.setIdStoreGroup(finalIdStoreGroup);
                return customer2;
            });
        } catch (Exception e) {
            log.severe("Error al crear el cliente -> " + e.getMessage());
        }
        return customerJsonFinal;
    }

    private CustomerJSON createCustomerSelf(final Customer customer, CustomerJSON customerJsonFinal, Boolean anonymous, String finalCityName, String finalIdFacebook, String finalIdGoogle, Integer finalIdStoreGroup) {
        try {
            User userValidate = getUser(customerJsonFinal.getId());
            return ofy().transact(() ->
            {
                User user = new User();
                Key<User> userKey = null;
                if (userValidate == null) {
                    user.setIdUser(UUID.randomUUID().toString());
                    user.setRole("Customer");
                } else {
                    user = userValidate;
                }

                userKey = Key.create(User.class, user.getIdUser());
                user.setIdUserWebSafe(userKey.toWebSafeString());

                Token tokenTransport = generateToken();
                Token tokenClient = new Token();
                tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
                tokenClient.setToken(tokenTransport.getToken());
                encryptToken(tokenClient);
                tokenClient.setTokenId(UUID.randomUUID().toString());
                tokenClient.setOwner(Ref.create(userKey));
                tokenClient.setTokenExpDate(tokenTransport.getTokenExp());
                Key<Token> keyToken = ofy().save().entity(tokenClient).now();
                tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
                user.setIdUserWebSafe(userKey.toWebSafeString());

                savedCredentialsDS(userKey, customer.getEmail().toLowerCase());


                CustomerJSON customerJsonAux;
                if (customerJsonFinal == null) {
                    customerJsonAux = new CustomerJSON();
                    user.setId(0);
                    customerJsonAux.setIdCustomerWebSafe(user.getIdUserWebSafe());
                } else {
                    customerJsonAux = customerJsonFinal;
                    user.setId(customerJsonFinal.getId());
                    customerJsonAux.setIdCustomerWebSafe(user.getIdUserWebSafe());

                }
                customerJsonAux.setLatitude(customer.getLatitude());
                customerJsonAux.setLongitude(customer.getLongitude());
                customerJsonAux.setCity(finalCityName);
                customerJsonAux.setToken(tokenTransport);

                ofy().save().entity(user).now();
                if (customerJsonAux.getAddresses() != null && !customerJsonAux.getAddresses().isEmpty()) {
                    for (Address address : customerJsonAux.getAddresses()) {
                        address.setLatitude(LATITUDE_STORE);
                        address.setLongitude(LONGITUDE_STORE);
                    }
                }
                customerJsonAux.setIdStoreGroup(finalIdStoreGroup);
                return customerJsonAux;
            });
        } catch (Exception e) {
            log.severe("Error al crear el cliente -> " + e.getMessage());
        }
        return customerJsonFinal;
    }

    /**
     * Creating Customer. Insertion or association of a user to register through the platform, in their database.
     * In the process, a security token for Firebase is returned and a token for petitions through the platform.
     *
     * @param customer Object of class 'Customer' that contain data to store or associate of a new user (customer).
     */

    @ApiMethod(name = "registerForAnonymous", path = "/customerEndpoint/registerForAnonymous", httpMethod = ApiMethod.HttpMethod.POST)
    public CustomerJSON registerForAnonymous(final Customer customer, final HttpServletRequest request)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException, NotFoundException {
        if (Objects.isNull(customer))
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);

        if (Objects.isNull(customer)) {
//            log.info("method: updateFavorites() --> BadRequest [request is required]");
            throw new BadRequestException("BadRequest [request is null]");
        } else if (Objects.isNull(customer.getToken()) || Objects.isNull(customer.getToken().getToken()) || customer.getToken().getToken().isEmpty()) {
//            log.info("method: updateFavorites() --> BadRequest [token is required]");
            throw new BadRequestException("BadRequest [token is required]");
        } else if (Objects.isNull(customer.getToken()) || Objects.isNull(customer.getToken().getTokenIdWebSafe()) || customer.getToken().getTokenIdWebSafe().isEmpty()) {
//            log.info("method: updateFavorites() --> BadRequest [tokenIdWebSafe is required]");
            throw new BadRequestException("BadRequest [tokenIdWebSafe is required]");
        } else if (Objects.isNull(customer.getIdCustomerWebSafe()) || customer.getIdCustomerWebSafe().isEmpty()) {
//            log.info("method: updateFavorites() --> BadRequest [IdCustomerWebSafe is required]");
            throw new BadRequestException("BadRequest [IdCustomerWebSafe is required]");
        } else if (!authenticate.isValidToken(customer.getToken().getToken(), customer.getToken().getTokenIdWebSafe())) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }
        final Key<User> userKey = Key.create(customer.getIdCustomerWebSafe());
        final User user = users.findUserByKey(userKey);


        if (!customer.isDataManagement() || !customer.isTermsAndConditions())
            throw new UnauthorizedException(Constants.TERMS_ACCEPTANCE);
        String cityName = "";

        String idFacebook = null;
        GoogleAuth idGoogle = null;

        if (customer.getTokenFacebook() != null) {
            //log.warning("Face");
            FacebookClient.AccessToken accessToken = new DefaultFacebookClient().obtainAppAccessToken(Constants.APP_ID_FACEBOOK, Constants.APP_SECRET_FACEBOOK);
            FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken(), Version.VERSION_2_3);
            FacebookClient.DebugTokenInfo facebookClient1 = facebookClient.debugToken(customer.getTokenFacebook());
            idFacebook = facebookClient1.getUserId();
            //log.warning(idFacebook);

        }

        if (customer.getTokenGoogle() != null && !customer.getTokenGoogle().trim().isEmpty()) {
            //log.warning("Google");
            idGoogle = googleAuth(customer.getTokenGoogle());
        }

        if (idFacebook == null && idGoogle == null && (customer.getPassword() == null || customer.getPassword().isEmpty()))
            throw new BadRequestException(Constants.PASSWORD_INITIALIZATION);

        double lat = ofy().load().type(City.class).filter("id", "BOG").first().now().getLatitude();
        double lon = ofy().load().type(City.class).filter("id", "BOG").first().now().getLongitude();
        customer.setLatitude(lat);
        customer.setLongitude(lon);

        City city = ofy().load().type(City.class).filter("name", cityName).first().now();

        if (cityName.equals("") || city == null)
            cityName = "Bogota";

        final String finalCityName = cityName;

        CustomerJSON customerDataResponse = null;
        try {
            log.info("CreateCustomer: " + customer);
            customer.setIdGoogle(idGoogle.getGoogleId());
            customer.setIdFacebook(idFacebook);
            customerDataResponse = ApiGatewayService.get().createBasicCustomerV3(customer, TraceUtil.getXCloudTraceId(request));
        } catch (Exception ex) {
            throw new IllegalArgumentException("No fue posible crear el usuario. Error: " + ex.getMessage(), ex);
        }
        final CustomerJSON customerJsonFinal = customerDataResponse;

        final long finalIdStoreGroup = this.getIdStoreGroupLatLon(customer.getLatitude(), customer.getLongitude());
        final String finalIdGoogle = idGoogle.getGoogleId();
        final String finalIdFacebook = idFacebook;
        return ofy().transact(() -> {
            String passEnc = encrypt(customer.getPassword());
            user.setRole("Customer");
            user.setIdUserWebSafe(userKey.toWebSafeString());
            Token tokenTransport = generateToken();
            Token tokenClient = new Token();
            tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
            tokenClient.setToken(tokenTransport.getToken());
            encryptToken(tokenClient);
            tokenClient.setTokenId(UUID.randomUUID().toString());
            tokenClient.setOwner(Ref.create(userKey));
            tokenClient.setTokenExpDate(tokenTransport.getTokenExp());
            Key<Token> keyToken = ofy().save().entity(tokenClient).now();
            tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
            user.setIdUserWebSafe(userKey.toWebSafeString());

            if (customer.getDeviceId() != null && customer.getFirebaseTokenDevice() != null) {
                DeviceRegistry device = new DeviceRegistry();
                device.setIdDeviceRegistry(UUID.randomUUID().toString());
                device.setOwner(Ref.create(userKey));
                device.setFirebaseTokenDevice(customer.getFirebaseTokenDevice());
                device.setDeviceId(customer.getDeviceId());
                device.setAvailable(true);
                ofy().save().entity(device);
            }

            CustomerJSON customer2;
            if (customerJsonFinal == null) {
                customer2 = new CustomerJSON();
                user.setId(0);
                customer2.setIdCustomerWebSafe(user.getIdUserWebSafe());
            } else {
                customer2 = customerJsonFinal;
                user.setId(customerJsonFinal.getId());
                customer2.setIdCustomerWebSafe(user.getIdUserWebSafe());

            }
            customer2.setLatitude(customer.getLatitude());
            customer2.setLongitude(customer.getLongitude());
            customer2.setCity(finalCityName);
            customer2.setToken(tokenTransport);
            HashMap<String, Object> additionalClaims = new HashMap<>();
            String tokenFirebase = FirebaseAuth.getInstance().createCustomToken(user.getIdUser(), additionalClaims);
            user.setTokenFirebase(tokenFirebase);

            if (customer.getTokenFacebook() == null && customer.getTokenGoogle() == null) {
                if (customer.getEmail() == null)
                    throw new IllegalArgumentException(Constants.EMAIL_EXISTS);

                if (!customer.getEmail().contains("@"))
                    throw new IllegalArgumentException(Constants.EMAIL_NOT_CORRECT);

                String[] partsEmail = customer.getEmail().split("@");
                String[] partsDomain = partsEmail[1].split("\\.");

                if (partsDomain.length < 2)
                    throw new IllegalArgumentException(Constants.EMAIL_NOT_CORRECT);

                savedCredentialsDS(userKey, customer.getEmail());

                createPasswordDS(passEnc, userKey);

            } else if (customer.getTokenGoogle() == null) {
                //log.warning(finalIdFacebook);
                user.setIdFacebook(finalIdFacebook);
            } else {
                //log.warning(finalIdGoogle);
                user.setIdGoogle(finalIdGoogle);
            }

            ofy().save().entity(user).now();
            if (customer2.getAddresses() != null && !customer2.getAddresses().isEmpty()) {
                for (Address address : customer2.getAddresses()) {
                    address.setLatitude(4.6730450);
                    address.setLongitude(-74.0583310);
                }
            }
            customer2.setIdStoreGroup((int) finalIdStoreGroup);
            return customer2;
            //return customerJsonFinal;
        });
    }

    /**
     * Updating Customer. Update or association of data user through the platform, in their database.
     * In the process, a security token for Firebase is returned and a token for petitions through the platform.
     *
     * @param customer Object of class 'Customer' that contain data to update or associate of a user exists (customer).
     * @return Object of class 'Customer' that contain data to store of a new banner.
     * @throws Exception
     */
    @SuppressWarnings("ALL")
    @ApiMethod(name = "updateCustomer", path = "/customerEndpoint/updateCustomer", httpMethod = ApiMethod.HttpMethod.PUT)
    public CustomerJSON updateCustomer(final Customer customer, final HttpServletRequest servletRequest) throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException, NotFoundException {
        if (customer == null) {
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        }
        if (!authenticate.isValidToken(customer.getToken().getToken(), customer.getToken().getTokenIdWebSafe())) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }

        Key<User> userKey = Key.create(customer.getIdCustomerWebSafe());
        User user = users.findUserByKey(userKey);

        if (Objects.isNull(user) || (customer.getId() != user.getId()))
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);

        if (user == null || user.getId() <= 0)
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);

        if (customer.getFirstName() != null && customer.getLastName() != null && customer.getGender() != null && customer.getPhone() != null) {
            customers.customerUpdate(new Customer(customer.getId(),customer.getDocumentNumber(), customer.getFirstName(), customer.getLastName(), customer.getGender(), customer.getPhone(), customer.getProfileImageUrl()));
        }
        if (customer.getAddresses() != null) {
            if (!customer.getAddresses().isEmpty()) {
                for (Address address : customer.getAddresses()) {
                    AddAddressRequest request = new AddAddressRequest();
                    request.setIdCustomer(customer.getId());
                    request.setCity(address.getCity());
                    request.setAddress(address.getAddress());
                    request.setLatitude(address.getLatitude());
                    request.setLongitude(address.getLongitude());
                    request.setComments(address.getComments());
                    request.setNickname(address.getNickname());
                    request.setCourierCode(address.getCourierCode());
                    if (address.getDeliveryType() != null) {
                        request.setDeliveryType(address.getDeliveryType());
                    } else {
                        request.setDeliveryType(DeliveryType.EXPRESS);
                    }
                    if (address.getNewAddress())
                        ApiGatewayService.get().createCustomerAddress(request, TraceUtil.getXCloudTraceId(servletRequest));
                    else {
                        request.setIdAddress((long) address.getIdAddress());
                        ApiGatewayService.get().updateCustomerAddress(request);
                    }
                }
            }
        }

        Optional<CustomerJSON> optionalCustomerJSON = this.customers.customerInformation(user, 0, false);

        if (!optionalCustomerJSON.isPresent()){
            throw new ConflictException(Constants.CUSTOMER_NOT_FOUND);
        }

        CustomerJSON customerJSON = optionalCustomerJSON.get();
        if (customerJSON.getAddresses() != null && !customerJSON.getAddresses().isEmpty()) {
            for (Address address : customerJSON.getAddresses()) {
                address.setLatitude(4.6730450);
                address.setLongitude(-74.0583310);
            }
        }
        customerJSON.setBanners(null);
        customerJSON.setHighlightedItems(null);
        customerJSON.setSuggestedProducts(null);
        customerJSON.setPreviousItems(null);
        customerJSON.setPhotos(ApiGatewayService.get().getCustomerPhotos(customer.getId()));

        TalonOneService talonOneService = new TalonOneService();
        talonOneService.updateCustomer(customerJSON);
        return customerJSON;
    }

    /**
     * Find Customer by document number
     *
     * @param keyClient      Key Client
     * @param documentNumber Document number
     * @return Object of class 'Customer' stored.
     */
    @ApiMethod(name = "verifyCustomerV2", path = "/customerEndpoint/verifyCustomerV2", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer verifyCustomerV2(@Named("keyClient") final String keyClient,
                                   @Nullable @Named("documentNumber") final String documentNumber,
                                   @Nullable @Named("email") final String email,
                                   @Nullable @Named("phone") final String phone,
                                   @Nullable @Named("isEdit") final Boolean isEdit,
                                   final HttpServletRequest req)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException, NotFoundException {
        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
        RequestSourceEnum source = null;
        try {
            source = ftdUtilities.getSourceFromRequestHeader(req);
        } catch (Exception e) {
            log.warning("Ocurrio un error verificando el source");
        }


        Answer answer = new Answer();
        String message = "";
        if ((isEdit == null) || (!isEdit)) {
            ValidateCustomerDocumentNumber customer = null;
            if (documentNumber != null){
                try {
                    customer = customers.findCustomerDocumentNumber(Long.valueOf(documentNumber));
                } catch (Exception e) {
                    log.warning("No se pudo consultar el customer -> " + e.getMessage());
                }
            }

            if (customer != null && Objects.isNull(customer.getEmail()))
                customer = null;

            if(Objects.isNull(customer) && Objects.nonNull(email)){
                ValidateCustomerEmail customerResponse = ApiGatewayService.get().getCustomerByEmailLowerCase(email.toLowerCase());
                if (Objects.nonNull(customerResponse)){
                    customer = new ValidateCustomerDocumentNumber(customerResponse.getId(), customerResponse.getDocumentNumber(), customerResponse.getEmail(), customerResponse.getRegisteredBy());
                }
            }

            if (customer != null) {
//                log.info("Customer -->" + customer.toString());
                String[] emailParts = customer.getEmail().split("@");
                int lenght = (emailParts[0].length() / 2) + 1;
                String maskedEmail = customer.getEmail().replaceAll("(?<=.{" + lenght + "}).(?=[^@]*?@)", "*");
                answer.setConfirmation(false);
                message += Constants.LOGIN_MESSAGE;
                switch (customer.getRegisteredBy()) {
                    case "EMAIL":
                        message += maskedEmail + "\n\n" + Constants.EMAIL_MESSAGE + Constants.MESSAGE_COMPLETE;
                        break;
                    case "GOOGLE":
                        message += "Gmail con tu correo: " + maskedEmail + "\n\n" + Constants.GMAIL_MESSAGE + Constants.MESSAGE_COMPLETE;
                        break;
                    case "FACEBOOK":
                        message += "Facebook con tu correo: " + maskedEmail + "\n\n" + Constants.FACEBOOK_MESSAGE + Constants.MESSAGE_COMPLETE;
                        break;
                    case "APPLE":
                        message += "Apple con tu AppleId: " + maskedEmail + "\n\n" + Constants.APPLE_MESSAGE + Constants.MESSAGE_COMPLETE;
                        break;
                }
                answer.setMessage(message);
            } else {

                Boolean isCallCenter = false;
                try {
                    isCallCenter = isCalLCenter(phone, email);
                }catch (Exception e) {
                    log.warning("Ocurrio un problema verificando si el cliente es de call center.");
                }

                /**
                 * Validación del servicio para usuarios de call center
                 */
                if (isCallCenter) {
                    answer.setMessage("Este correo electronico ya se encuentra registrado en nuestra base de datos");
                    answer.setConfirmation(false);
                    return answer;
                }

                Boolean emailValidation = false;
                if (email != null){
                    if (Objects.nonNull(source) && source.equals(RequestSourceEnum.CALLCENTER)){
                        emailValidation = customers.findCustomerByEmail(email, true);
                    } else {
                        emailValidation = customers.findCustomerByEmail(email, false);
                    }
                }
                try {
                    if (emailValidation != null && emailValidation) {
                        answer.setMessage("Este correo electronico ya se encuentra registrado en nuestra base de datos");
                        answer.setConfirmation(false);
                    } else {
                        Boolean phoneValidation = false;
                        if (phone != null && !isCallCenter){
                            phoneValidation = customers.findCustomerByPhone(phone);
                        }
                        if (phoneValidation && !isCallCenter) {
                            answer.setMessage("El número celular esta asignado a otro usuario");
                            answer.setConfirmation(false);
                        } else {
                            answer.setConfirmation(true);
                        }
                    }
                } catch (Exception e) {
                    answer.setMessage("No se pudo validar el email, por favor intente nuevamente.");
                    log.warning("Error validando el email " + e.getMessage());
                }
            }
        } else {

            Boolean isCallCenter = false;
            try {
                isCallCenter = isCalLCenter(phone, email);
            }catch (Exception e) {
                log.warning("Ocurrio un problema verificando si el cliente es de call center.");
            }

            Boolean phoneValidation = customers.findCustomerByPhone(phone);
            if (phoneValidation && !isCallCenter) {
                answer.setMessage("El número celular esta asignado a otro usuario");
                answer.setConfirmation(false);
            } else {
                answer.setConfirmation(true);
            }
        }
        return answer;
    }

    @ApiMethod(name = "verifyCustomer", path = "/customerEndpoint/verifyCustomer", httpMethod = ApiMethod.HttpMethod.GET)
    public Customer verifyCustomer(@Named("keyClient") final String keyClient,
                                   @Named("documentNumber") final String documentNumber,
                                   HttpServletRequest request)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {
        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        return customers.findCustomerByDocumentNumber(documentNumber);
    }

    /**
     * Generate validation Token for the user
     *
     * @param keyClient Key Client
     * @param customer  Object of class 'Customer' that contain data to update or associate of a user exists (customer).
     * @return Answer.
     */
    @Deprecated
    @SuppressWarnings("ALL")
    @ApiMethod(name = "getValidationToken", path = "/customerEndpoint/getValidationToken", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer getValidationToken(@Named("keyClient") final String keyClient,
                                     final Customer customer)
            throws UnauthorizedException, BadRequestException, InternalServerErrorException,
            ConflictException, IOException, NotFoundException {
        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
        if (customer == null)
            throw new BadRequestException(Constants.CUSTOMER_INITIALIZATION);

        JSONObject customerJson = new JSONObject();
        customerJson.put("customerId", customer.getId());
        //log.warning(customerJson.toString());
        Answer answer = new Answer();
        answer.setConfirmation(false);
        return answer;
    }

    @SuppressWarnings("ALL")
    @ApiMethod(name = "getValidationTokenPhone", path = "/customerEndpoint/getValidationTokenPhone", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer getValidationTokenPhone(@Named("keyClient") final String keyClient, @Named("call") @Nullable String byCall, final Customer customer) {

        // Generacion del codigo
        final String code = Integer.toString((int) (Math.random() * 9000) + 1000);

//        log.info(byCall);

        try {

            if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
                throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
            if (customer == null)
                throw new BadRequestException(Constants.CUSTOMER_INITIALIZATION);
            if( Objects.nonNull(byCall) && !byCall.isEmpty() && (byCall.equals("true") || byCall.equals("TRUE")) ) {

                SendSMSCloudFunctionReq req = new SendSMSCloudFunctionReq();
                req.setTo(customer.getPhone());
                req.setBody(code);
                CloudFunctionsService.get().postSendCodeByCall(req);

            } else {
                final String finalMessage = msgUtilities.obtainMsgAlgolia(MsgSmsEnum.MESSAGE_VALIDATION_TOKEN_PHONE).replace("{CODE}", code);
                final SendSMSCloudFunctionReq request = new SendSMSCloudFunctionReq(customer.getPhone(), finalMessage);
                CloudFunctionsService.get().postSendSms(request);

                // Envio Whatsapp
                try {
                    SendWhatsappCloudFunctionReq requestW = new SendWhatsappCloudFunctionReq(customer.getPhone(), finalMessage);
                    CloudFunctionsService.get().postSendWhatsappV2(requestW);
                } catch (Exception e) {
                    log.warning("El api de whatsappV2 tiene problemas. Mensaje: " + (e != null ? e.getMessage() : ""));
                }
            }

            // Envio SMS

        } catch (Exception e) {
            log.warning("ERROR enviando SMS o Whatsapp. Mensaje: "+ e.getMessage());
        } finally {
            Answer answer = new Answer();
            answer.setConfirmation(true);
            answer.setTokenFarmatodo(code);
            return answer;
        }
    }

    /**
     * Merge cart anonymous with user after login
     *
     * @param mergeAnonymousCartReq
     * @return
     * @throws ConflictException
     */
    @SuppressWarnings("Duplicates")
    @ApiMethod(name = "mergeShoppingCartAnonymous", path = "/customerEndpoint/mergeShoppingCartAnonymous", httpMethod = ApiMethod.HttpMethod.POST)
    public MergeAnonymousCartRes mergeShoppingCartAnonymous(final MergeAnonymousCartReq mergeAnonymousCartReq) throws ConflictException, BadRequestException, AlgoliaException {
        if (!mergeAnonymousCartReq.isValid()) {
            throw new ConflictException("Request Invalid");
        }
        if (!authenticate.isValidToken(mergeAnonymousCartReq.getToken(), mergeAnonymousCartReq.getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);

        MergeAnonymousCartRes response = new MergeAnonymousCartRes();

        Key<User> customerKey = Key.create(mergeAnonymousCartReq.getIdCustomerWebSafeAnonymous());
        DeliveryOrder deliveryOrderAnonymous = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();

        if (Objects.isNull(deliveryOrderAnonymous)) {
            response.setConfirmation(false);
            response.setMessage("Anonymous Cart is Empty");
            return response;
        }

        List<DeliveryOrderItem> anonymousDeliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderAnonymous).list();
        List<DeliveryOrderProvider> anonymousDeliveryOrderProviderList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderAnonymous).list();

        boolean anonymousCartIsValid = validateShoppingCart(deliveryOrderAnonymous, anonymousDeliveryOrderItemList, anonymousDeliveryOrderProviderList);

        if (!anonymousCartIsValid) {
            throw new ConflictException("Carrito Anonimo Vacio o con error.");
        }

//        log.info("Carrito anonimo:");

        anonymousDeliveryOrderItemList.forEach(item -> {
            log.info("item -> " + item.getId() + " quantity , -> " + item.getQuantitySold());
        });

        // real customer

        int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(mergeAnonymousCartReq.getIdStoreGroup());
        Key<Customer> realCustomerKey = Key.create(mergeAnonymousCartReq.getIdCustomerWebSafe());
        DeliveryOrder realDeliveryOrderBase = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(realCustomerKey)).first().now();
        // final DeliveryOrder realDeliveryOrder = Objects.nonNull(realDeliveryOrderBase) ? realDeliveryOrderBase : new DeliveryOrder();
        final DeliveryOrder realDeliveryOrder;
        if (Objects.isNull(realDeliveryOrderBase)) {
            String deliveryType = mergeAnonymousCartReq.getDeliveryType();
            //log.warning("Se crea un carrito de compras.");
            realDeliveryOrder = new DeliveryOrder();
            realDeliveryOrder.setIdDeliveryOrder(UUID.randomUUID().toString());
            realDeliveryOrder.setIdCustomer(Ref.create(realCustomerKey));
            realDeliveryOrder.setCurrentStatus(1);
            realDeliveryOrder.setCreateDate(new Date());

            switch (deliveryType) {
                case "EXPRESS":
                    realDeliveryOrder.setDeliveryType(DeliveryType.EXPRESS);
                    break;
                case "NATIONAL":
                    realDeliveryOrder.setDeliveryType(DeliveryType.NATIONAL);
                    break;
                case "ENVIALOYA":
                    realDeliveryOrder.setDeliveryType(DeliveryType.ENVIALOYA);
                    break;
                case "SCANANDGO":
                    realDeliveryOrder.setDeliveryType(DeliveryType.SCANANDGO);
                    break;
            }

            ofy().save().entity(realDeliveryOrder).now();
        } else {
            realDeliveryOrder = realDeliveryOrderBase;
//            log.info("DeliveryOrderBase is NOT null");
        }

        addItemsToRealCart(realDeliveryOrder, realCustomerKey, anonymousDeliveryOrderItemList, anonymousDeliveryOrderProviderList, idStoreGroup);

        response.setConfirmation(true);
        response.setMessage("ShoppingCart Merged");
        return response;
    }

    @SuppressWarnings("Duplicates")
    private List<DeliveryOrderItem> addItemsToRealCart(DeliveryOrder deliveryOrder,
                                                       Key<Customer> customerKey,
                                                       List<DeliveryOrderItem> itemsToCart,
                                                       List<DeliveryOrderProvider> itemsProviderCart,
                                                       int idStoreGroup) throws ConflictException {

        UpdateDeliveryOrderRequest updateDeliveryOrderRequest = new UpdateDeliveryOrderRequest();

        List<UpdateDeliveryOrderRequest.Item> itemList = new ArrayList<>();

        if (!itemsToCart.isEmpty()) {
            itemsToCart.forEach(it -> {
                UpdateDeliveryOrderRequest.Item itemAux = new UpdateDeliveryOrderRequest.Item();

                itemAux.setItemId((int) it.getId());
                itemAux.setQuantityRequested(it.getQuantitySold());

                itemList.add(itemAux);

            });
        }

        if (!itemsProviderCart.isEmpty()) {
            itemsProviderCart.forEach(it -> {
                UpdateDeliveryOrderRequest.Item itemAux = new UpdateDeliveryOrderRequest.Item();
                itemAux.setItemId((int) it.getId());
                itemAux.setQuantityRequested(it.getQuantityItem());
//                log.info("item to add -> " + it.getId());
                itemList.add(itemAux);
            });
        }


        updateDeliveryOrderRequest.setItems(itemList);
//        log.info(" item list to add to cart ");
        updateDeliveryOrderRequest.getItems().forEach(it -> {
            log.info("item in list -> " + it.toString());
        });


        if (updateDeliveryOrderRequest.getItems().isEmpty()) {
            throw new ConflictException("No hay items para agregar");
        }

//        log.info(" se procede a agregar al carrito.");
        List<DeliveryOrderItem> deliveryItems = updateDeliveryOrderRequest.getItems().stream()
                .map(item -> {
                    try {
                        return productsMethods.buildDeliveryOrderItem(deliveryOrder, customerKey, item, true, idStoreGroup);
                    } catch (ConflictException e) {
                        log.warning("Error al generar los items a agregar. " + e.getMessage() + " - " + e.fillInStackTrace());
                        return null;
                    }
                }).collect(Collectors.toList());

        if (!deliveryItems.isEmpty()) {
            deliveryItems = deliveryItems.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
//            log.info("items to save: ");
            deliveryItems.forEach(it -> {
                log.info(it.toString());
            });

            List<DeliveryOrderItem> finalDeliveryItems = deliveryItems;
            return ofy().transact(() -> {
                ofy().save().entities(finalDeliveryItems);
                return finalDeliveryItems;
            });
        } else {
            throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
        }
    }


    private boolean validateShoppingCart(DeliveryOrder deliveryOrderAnonymous, List<DeliveryOrderItem> anonymousDeliveryOrderItemList, List<DeliveryOrderProvider> anonymousDeliveryOrderProviderList) {


        if (Objects.isNull(anonymousDeliveryOrderItemList) && Objects.isNull(anonymousDeliveryOrderProviderList))
            return false;

        if (anonymousDeliveryOrderItemList.isEmpty() && anonymousDeliveryOrderProviderList.isEmpty()) return false;

        return true;
    }

    /**
     * Validate Token sended to user
     *
     * @param keyClient Key Client
     * @param customer  Object of class 'Customer' that contain data to update or associate of a user exists (customer).
     * @return CustomerJson.
     */
    @Deprecated
    @ApiMethod(name = "validateToken", path = "/customerEndpoint/validateToken", httpMethod = ApiMethod.HttpMethod.POST)
    public CustomerJSON validateToken(@Named("keyClient") final String keyClient,
                                      final Customer customer)
            throws BadRequestException,
            IOException, InternalServerErrorException,
            UnauthorizedException {
        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
        if (customer == null)
            throw new BadRequestException(Constants.CUSTOMER_INITIALIZATION);
        return null;
    }

    /**
     * Get Firebase token created for a user
     *
     * @param token             token of to the user.
     * @param tokenIdWebSafe    tokenIdWebSafe of the user.
     * @param idCustomerWebSafe id web safe of the user.
     * @return user of the class User.
     * @throws ConflictException   when the tokens are empty.
     * @throws BadRequestException When the tokens are not correct.
     */
    @ApiMethod(name = "getFirebaseToken", path = "/customerEndpoint/getFirebaseToken", httpMethod = ApiMethod.HttpMethod.GET)
    public User getFirebaseToken(@Named("token") final String token,
                                 @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                 @Named("idCustomerWebSafe") final String idCustomerWebSafe)
            throws ConflictException, BadRequestException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        /* Solicitar informacion de usuario a farmatodo */
        String uid = user.getIdUser();
        String tokenFirebase = FirebaseAuth.getInstance().createCustomToken(uid);

        user.setTokenFirebase(tokenFirebase);
        ofy().save().entity(user).now();
        user.setIdUserWebSafe(userKey.toWebSafeString());
        return user;
    }

    /**
     * Login in of a user of type 'client'. Validate that credentials are correct (email and password)
     *
     * @param keyClient    client's secure key
     * @param emailAddress User's email to login
     * @param password     User's password to login
     * @return Object of class 'User' stored.
     */
    @Deprecated //use loginPost
    @ApiMethod(name = "login", path = "/customerEndpoint/login", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerJSON login(@Named("keyClient") final String keyClient,
                              @Named("emailAddress") final String emailAddress,
                              @Named("password") final String password,
                              @Named("latitude") final double latitude,
                              @Named("longitude") final double longitude,
                              @Nullable @Named("deviceId") final String deviceId,
                              @Nullable @Named("firebaseTokenDevice") final String firebaseTokenDevice,
                              final HttpServletRequest request)
            throws Exception {

        throw new ForbiddenException("Access Prohibited");
        //return loginMethod(keyClient, emailAddress, password, latitude, longitude, deviceId, firebaseTokenDevice, TraceUtil.getXCloudTraceId(request));
    }

    @ApiMethod(name = "loginPost", path = "/customerEndpoint/loginPost", httpMethod = ApiMethod.HttpMethod.POST)
    public CustomerJSON loginPost(final LoginRequest loginRequest, final HttpServletRequest request)
            throws  Exception {
        return loginMethod(loginRequest.getKeyClient(), loginRequest.getEmailAddress(), loginRequest.getPassword(),
                loginRequest.getLatitude(), loginRequest.getLongitude(), loginRequest.getDeviceId(), loginRequest.getFirebaseTokenDevice(), TraceUtil.getXCloudTraceId(request));
    }

    private CustomerJSON loginMethod(final String keyClient, final String emailAddress,
                                     String password, final double latitude,
                                     final double longitude, final String deviceId,
                                     final String firebaseTokenDevice,
                                     final String traceId)
            throws Exception {

        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        if (emailAddress == null || emailAddress.isEmpty()) {
            throw new BadRequestException(Constants.EMAIL_NOT_CORRECT);
        }
        Credential credential = getCredencial(emailAddress.toLowerCase());
        if (Objects.isNull(credential)) {
            CustomerJSON customerJsonValidate = validateCustomerOracle(emailAddress.toLowerCase(), password, traceId, deviceId, firebaseTokenDevice, latitude, longitude);
            if(customerJsonValidate != null){
                getCredencial(emailAddress.toLowerCase());
                return customerJsonValidate;
            }
        }

        User user;
        if (credential != null)
            user = users.findUserByKey(credential.getOwner().getKey());
        else
            throw new BadRequestException(Constants.EMAIL_EXISTS);

        if (!user.getRole().equals("Customer"))
            throw new ConflictException(Constants.USER_TYPE_ERROR);

        UserPass userPass = users.findPassByAncestor(credential.getOwner().getKey());
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        if (Objects.isNull(userPass)) {
            CustomerJSON customerJsonPass = validateCustomerPass(emailAddress.toLowerCase(), password, traceId, deviceId, firebaseTokenDevice, latitude, longitude);
            if (Objects.nonNull(customerJsonPass)) {
                getUser(customerJsonPass.getId());
                getUserAndDelete(customerJsonPass.getId());
                return customerJsonPass;
            }
        }

        if (!passwordEncryptor.checkPassword(password, userPass.getPassword())) {
            throw new UnauthorizedException(APIAlgolia.getMessagePasswordIncorrect());
        }

        CustomerJSON customerJSON = this.loginFarmatodo(emailAddress.toLowerCase(), password, null, null, traceId);

//        log.info("Backend 3 customerJSON =" + (Objects.nonNull(customerJSON) ? " userId:" + customerJSON.getId() + " Email:" + customerJSON.getEmail() : "Not Found"));
        //log.warning("Backend 3 customerJSON =" +customerJSON);

        if (deviceId != null && firebaseTokenDevice != null) {
            Key<User> userKey = Key.create(User.class, user.getIdUser());
            createDevice(firebaseTokenDevice, deviceId, userKey);
        }

        Optional<CustomerCallCenterAlgolia> optionalCustomerCallCenterAlgolia = APIAlgolia.getUsersCallCenter();
        CustomerCallCenterAlgolia usersCall = optionalCustomerCallCenterAlgolia.get();
//        log.info("Usuarios callCenter: " + usersCall.getUsers());
        usersCall.getUsers().parallelStream()
                .filter(userCall -> userCall.getEmail().equals(emailAddress))
                .forEach(userCall -> {
                    customerJSON.setUserCall(Boolean.TRUE);
                });

        // Succeeded in login security
        Token tokenTransport = generateToken();

        Token tokenClient = new Token();
        tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
        tokenClient.setToken(tokenTransport.getToken());
        encryptToken(tokenClient);
        tokenClient.setTokenId(UUID.randomUUID().toString());
        tokenClient.setOwner(Ref.create(credential.getOwner().getKey()));
        tokenClient.setTokenExpDate(tokenTransport.getTokenExp());

        Key<Token> keyToken = ofy().save().entity(tokenClient).now();
        user.setIdUserWebSafe(credential.getOwner().getKey().toWebSafeString());
        tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
        user.setToken(tokenTransport);

        user.setLastLogin(new Date().getTime());
        credential.setLastLogin(new Date());
        ofy().save().entities(credential, user);
        //final int finalIdStoreGroup = (int) this.getIdStoreGroupLatLon(latitude, longitude);
        //log.info(finalIdStoreGroup + "");
        //CustomerJSON customerJSON = this.customerInformation(user, finalIdStoreGroup);
        customerJSON.setToken(tokenTransport);
        customerJSON.setIdCustomerWebSafe(user.getIdUserWebSafe());
        customerJSON.setBanners(null);
        customerJSON.setSuggestedProducts(null);
        customerJSON.setPreviousItems(null);
        customerJSON.setHighlightedItems(null);
        if (customerJSON.getAddresses() != null && !customerJSON.getAddresses().isEmpty()) {
            for (Address address : customerJSON.getAddresses()) {
                address.setLatitude(4.6730450);
                address.setLongitude(-74.0583310);
            }
        }
        int shoppingCartNumber = 0;
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(user).first().now();
        if (deliveryOrder != null) {
            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
            if (deliveryOrderItemList != null) {
                for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                    shoppingCartNumber += deliveryOrderItem.getQuantitySold();
                }

                //shoppingCartNumber = deliveryOrderItemList.size();
                customerJSON.setShopingCartNumber(shoppingCartNumber);
            }
        }
        //customerJSON.setIdStoreGroup(finalIdStoreGroup);
        Store store = this.getIdStoreGroupLatLonDefaultStores(latitude, longitude);

        City city = ofy().load().type(City.class).filter("id", store.getCity()).first().now();

        int idStoreGroup = city.getDefaultStore();

        customerJSON.setIdStoreGroup(idStoreGroup);
//        log.info("method: login() -> Success");
        return customerJSON;
    }

    private CustomerJSON validateCustomerOracle(String email, String password, String traceId, String deviceId, String firebaseTokenDevice, double latitude, double longitude) throws Exception {
        Boolean isSamePasswordDataBase = checkPassword(email, password);
        if (isSamePasswordDataBase == null) {
            throw new UnauthorizedException(APIAlgolia.getMessagePasswordIncorrect());
        }

        if (!isSamePasswordDataBase) {
            return null;
        }

        ValidateCustomerOracle result = ApiGatewayService.get().getCustomerOracle(email);
        if (result.getValidation()) {
            return createAndReturnCustomer(email, password, traceId, deviceId, firebaseTokenDevice, latitude, longitude);
        }

        return null;
    }

    private Boolean checkPassword(String email, String password) throws Exception {
        ValidatePasswordDataBase validatePasswordDataBase = new ValidatePasswordDataBase();
        validatePasswordDataBase.setEmail(email);
        validatePasswordDataBase.setPassword(password);
        return ApiGatewayService.get().isSamePasswordDataBase(validatePasswordDataBase);
    }

    private CustomerJSON createAndReturnCustomer(String email, String password, String traceId, String deviceId, String firebaseTokenDevice, double latitude, double longitude) throws ConflictException, UnauthorizedException, BadRequestException, InternalServerErrorException, NotFoundException, IOException {
        Customer customer = new Customer();
        CustomerJSON customerJson = this.loginFarmatodo(email, password, null, null, traceId);
        customer.setEmail(email);
        customer.setPassword(password);
        customer.setDeviceId(deviceId);
        customer.setFirebaseTokenDevice(firebaseTokenDevice);
        customer.setLongitude(longitude);
        customer.setLatitude(latitude);
        return createCustomer(customer, customerJson, false, customerJson.getCity(), null, null, 26);
    }


    private CustomerJSON validateCustomerPass(String email, String password, String traceId, String deviceId, String firebaseTokenDevice, double latitude, double longitude) throws Exception {

        Credential credential = getCredencial(email);
        UserPass userPass = users.findPassByAncestor(credential.getOwner().getKey());
        if (Objects.isNull(userPass)){
            users.deleteCredencialDataStore(credential);
            Customer customer = new Customer();
            CustomerJSON customerJson = this.loginFarmatodo(email, password, null, null, traceId);
            customer.setEmail(email);
            customer.setPassword(password);
            customer.setDeviceId(deviceId);
            customer.setFirebaseTokenDevice(firebaseTokenDevice);
            customer.setLongitude(longitude);
            customer.setLatitude(latitude);
            customer.setTokenFacebook(null);
            customer.setTokenGoogle(null);

            return createCustomer(customer, customerJson, false, customerJson.getCity(), null, null, 26);
    }
       return null;
    }

    @ApiMethod(name = "loginFacebook", path = "/customerEndpoint/loginFacebook", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerJSON loginFacebook(@Named("keyClient") final String keyClient,
                                      @Named("tokenAccess") final String tokenAccess,
                                      @Named("latitude") final double latitude,
                                      @Named("longitude") final double longitude,
                                      @Nullable @Named("deviceId") final String deviceId,
                                      @Nullable @Named("firebaseTokenDevice") final String firebaseTokenDevice,
                                      final HttpServletRequest request)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException, NotFoundException {
        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
        //log.warning("Token " + tokenAccess);
        CustomerJSON customerJSON = null;

//        log.info("LoginFB DeviceId ? -->> " + deviceId);

        // clientes quemados para login de FB.
        if (tokenAccess.equals("108865888284982") || tokenAccess.equals("102099112306270")){

            customerJSON = this.loginFarmatodo(null, null, tokenAccess, null, TraceUtil.getXCloudTraceId(request));
            User user = ofy().load().type(User.class).filter("id", customerJSON.getId()).first().now();
            Key<User> keyUser = Key.create(User.class, user.getIdUser());

            return getCustomerJSONTokensLogin(latitude, longitude, customerJSON, user, keyUser);
        }

        FacebookClient.DebugTokenInfo facebookClient1;
        try {
            FacebookClient.AccessToken accessToken = new DefaultFacebookClient().obtainAppAccessToken(Constants.APP_ID_FACEBOOK, Constants.APP_SECRET_FACEBOOK);
            FacebookClient facebookClient = new DefaultFacebookClient(accessToken.getAccessToken(), Version.VERSION_2_3);
            facebookClient1 = facebookClient.debugToken(tokenAccess);
        } catch (Exception e) {
            log.warning("Error@loginFacebook " + e.getMessage());
            throw new ConflictException(Constants.ERROR_ACCESS_DENIED);
        }

        User user;
        //log.warning(facebookClient1.getUserId());
        //se agrega Objects.requireNonNull para evitar null pointer
        if (!Objects.nonNull(facebookClient1.isValid())
                || !facebookClient1.getExpiresAt().after(new Date())
                || !facebookClient1.getAppId().equals(Constants.APP_ID_FACEBOOK)) {
            throw new ConflictException(Constants.ERROR_ACCESS_DENIED);
        }

        //log.warning("FB_Valido");
        //log.warning(facebookClient1.getUserId());
        user = ofy().load().type(User.class).filter("idFacebook", facebookClient1.getUserId()).first().now();

        if (user == null){
            customerJSON = this.loginFarmatodo(null, null, facebookClient1.getUserId(), null, TraceUtil.getXCloudTraceId(request));
            // Long userId = // ... api CRM ;
            log.warning("No se encuentra el usuario en datastore por facebookid => " + tokenAccess + ", se intenta con ORACLE..");
            user = ofy().load().type(User.class).filter("id", customerJSON.getId()).first().now();
        }

        if (user != null) {

//            log.info("Usuario en FB Encontrado => " + user.toStringJson());

            if (customerJSON == null){
                customerJSON = this.loginFarmatodo(null, null, facebookClient1.getUserId(), null, TraceUtil.getXCloudTraceId(request));
            }

            Key<User> keyUser = Key.create(User.class, user.getIdUser());
            if (deviceId != null && firebaseTokenDevice != null) {
                createDevice(firebaseTokenDevice, deviceId, keyUser);
            }
            return getCustomerJSONTokensLogin(latitude, longitude, customerJSON, user, keyUser);

        } else
            throw new ConflictException(Constants.USER_NOT_FOUND);

        }

    @NotNull
    private CustomerJSON getCustomerJSONTokensLogin(double latitude, double longitude, CustomerJSON customerJSON, User user, Key<User> keyUser) throws UnauthorizedException, BadRequestException, IOException, InternalServerErrorException {
        Token tokenTransport = generateToken();
        Token tokenClient = new Token();
        tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
        tokenClient.setToken(tokenTransport.getToken());
        encryptToken(tokenClient);
        tokenClient.setTokenId(UUID.randomUUID().toString());
        tokenClient.setOwner(Ref.create(keyUser));
        tokenClient.setTokenExpDate(tokenTransport.getTokenExp());

        Key<Token> keyToken = ofy().save().entity(tokenClient).now();
        user.setIdUserWebSafe(keyUser.toWebSafeString());
        tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
        user.setToken(tokenTransport);
        user.setLastLogin(new Date().getTime());

        ofy().save().entity(user);
        //final int finalIdStoreGroup = (int) this.getIdStoreGroupLatLon(latitude, longitude);
        //CustomerJSON customerJSON = this.customerInformation(user, finalIdStoreGroup);
        customerJSON.setToken(tokenTransport);
        customerJSON.setIdCustomerWebSafe(user.getIdUserWebSafe());
        customerJSON.setBanners(null);
        customerJSON.setSuggestedProducts(null);
        customerJSON.setPreviousItems(null);
        customerJSON.setHighlightedItems(null);
        if (customerJSON.getAddresses() != null && !customerJSON.getAddresses().isEmpty()) {
            for (Address address : customerJSON.getAddresses()) {
                address.setLatitude(4.6730450);
                address.setLongitude(-74.0583310);
            }
        }
        int shoppingCartNumber = 0;
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(user).first().now();
        if (deliveryOrder != null) {
            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
            if (deliveryOrderItemList != null) {
                for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                    shoppingCartNumber += deliveryOrderItem.getQuantitySold();
                }
                //shoppingCartNumber = deliveryOrderItemList.size();
                customerJSON.setShopingCartNumber(shoppingCartNumber);
            }
        }
        //customerJSON.setIdStoreGroup(finalIdStoreGroup);
        Store store = this.getIdStoreGroupLatLonDefaultStores(latitude, longitude);

        City city = ofy().load().type(City.class).filter("id", store.getCity()).first().now();

        int idStoreGroup = city.getDefaultStore();

        customerJSON.setIdStoreGroup(idStoreGroup);

        return customerJSON;
    }


    // TODO: CHANGUE TO POST, AND USE HEADERS TO SEND INFO
    // TODO: USER TO ALL LOGINS

    // nota: por ahora solo funciona para login con apple, en el futuro lo ideal es integrar con firebase todos los logins.
    @ApiMethod(name = "loginFirebase", path = "/customerEndpoint/loginFirebase", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerJSON loginFirebase(@Named("keyClient") final String keyClient,
                                      @Named("uidFirebase") final String uidFirebase,
                                      @Nullable @Named("deviceId") final String deviceId,
                                      @Named("latitude") final double latitude,
                                      @Named("longitude") final double longitude,
                                      @Nullable @Named("firebaseTokenDevice") final String firebaseTokenDevice,
                                      final HttpServletRequest request) throws UnauthorizedException, BadRequestException, IOException, InternalServerErrorException, ConflictException {

        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        if (uidFirebase == null || uidFirebase.isEmpty()) {
            throw new BadRequestException(Constants.ERROR_BAD_REQUEST);
        }

        User user;
        CustomerJSON customerJSON;
        //log.warning("uid firebase ->  " + uidFirebase);
        //log.warning("deviceID ->  " + deviceId);

        LoginFirebaseReq loginFirebaseRequest = new LoginFirebaseReq(uidFirebase);

        Optional<LoginFirebaseRes> optionalLoginFirebaseRes = ApiGatewayService.get().loginFirebaseByUid(loginFirebaseRequest, TraceUtil.getXCloudTraceId(request));

        if (!optionalLoginFirebaseRes.isPresent()) {
            throw new UnauthorizedException(Constants.CUSTOMER_NOT_FOUND);
        }

        LoginFirebaseRes firebaseRes = optionalLoginFirebaseRes.get();
        user = ofy().load().type(User.class).filter("uidFirebase", firebaseRes.getUidFirebase()).first().now();

        if (user != null) {

            Optional<CustomerJSON> optionalCustomerJSON = ApiGatewayService.get().getCustomerById(user.getId());

            if (!optionalCustomerJSON.isPresent()){
                throw new ConflictException(Constants.USER_NOT_FOUND);
            }

            customerJSON = optionalCustomerJSON.get();

            if (customerJSON.getEmail() != null && !customerJSON.getEmail().isEmpty()){
                String documentBraze="";
                if (Objects.nonNull(customerJSON.getDocumentNumber())) {
                    documentBraze=String.valueOf(customerJSON.getDocumentNumber());
//                    log.info("document Braze for loginFirebase= " + documentBraze);
                }

                final  Optional<String> optionalBrazeUUID = ApiGatewayService.get().getUUIDFromBrazeCreateUser(customerJSON.getEmail(),documentBraze, null);

                if (optionalBrazeUUID.isPresent()){
                    customerJSON.setAnalyticsUUID(optionalBrazeUUID.get());
                }
            }


            return getCustomerJSON(latitude, longitude, deviceId, firebaseTokenDevice, user, customerJSON);
        } else {
            throw new ConflictException(Constants.USER_NOT_FOUND);
        }

    }

    @ApiMethod(name = "loginGoogle", path = "/customerEndpoint/loginGoogle", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerJSON loginGoogle(@Named("keyClient") final String keyClient,
                                    @Named("tokenAccess") final String tokenAccess,
                                    @Named("latitude") final double latitude,
                                    @Named("longitude") final double longitude,
                                    @Nullable @Named("deviceId") final String deviceId,
                                    @Nullable @Named("firebaseTokenDevice") final String firebaseTokenDevice,
                                    final HttpServletRequest request)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException, NotFoundException {
        if (Objects.isNull(keyClient) || !keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
        User user;
        CustomerJSON customerJSON;
        GoogleAuth userGoogle = googleAuth(tokenAccess);

        if (userGoogle == null) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }

        user = ofy().load().type(User.class).filter("idGoogle", userGoogle.getGoogleId()).first().now();
        if (user != null) {
            customerJSON = this.loginFarmatodo(userGoogle.getEmail(), null, null, userGoogle.getGoogleId(), TraceUtil.getXCloudTraceId(request));
            return getCustomerJSON(latitude, longitude, deviceId, firebaseTokenDevice, user, customerJSON);
        } else if (validateEmailGoogle(userGoogle)) {
            customerJSON = this.loginFarmatodo(userGoogle.getEmail(), null, null, userGoogle.getGoogleId(), TraceUtil.getXCloudTraceId(request));
            user = ofy().load().type(User.class).filter("idGoogle", userGoogle.getGoogleId()).first().now();
            return getCustomerJSON(latitude, longitude, deviceId, firebaseTokenDevice, user, customerJSON);

        } else {
            throw new ConflictException(Constants.USER_NOT_FOUND);
        }

    }

    @NotNull
    private CustomerJSON getCustomerJSON(@Named("latitude") double latitude, @Named("longitude") double longitude, @Named("deviceId") @Nullable String deviceId, @Named("firebaseTokenDevice") @Nullable String firebaseTokenDevice, User user, CustomerJSON customerJSON) throws UnauthorizedException, BadRequestException, IOException, InternalServerErrorException {
        Key<User> keyUser = Key.create(User.class, user.getIdUser());
        if (deviceId != null && firebaseTokenDevice != null) {
            //log.warning("DEVICE!!!");
            createDevice(firebaseTokenDevice, deviceId, keyUser);
        }
        Token tokenTransport = generateToken();
        Token tokenClient = new Token();
        tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
        tokenClient.setToken(tokenTransport.getToken());
        encryptToken(tokenClient);
        tokenClient.setTokenId(UUID.randomUUID().toString());
        tokenClient.setOwner(Ref.create(keyUser));
        tokenClient.setTokenExpDate(tokenTransport.getTokenExp());

        Key<Token> keyToken = ofy().save().entity(tokenClient).now();
        user.setIdUserWebSafe(keyUser.toWebSafeString());
        tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
        user.setToken(tokenTransport);
        user.setLastLogin(new Date().getTime());

        ofy().save().entity(user);

        //final int finalIdStoreGroup = (int) this.getIdStoreGroupLatLon(latitude, longitude);
        customerJSON.setToken(tokenTransport);
        customerJSON.setIdCustomerWebSafe(user.getIdUserWebSafe());
        customerJSON.setBanners(null);
        customerJSON.setSuggestedProducts(null);
        customerJSON.setPreviousItems(null);
        customerJSON.setHighlightedItems(null);
        if (customerJSON.getAddresses() != null && !customerJSON.getAddresses().isEmpty()) {
            for (Address address : customerJSON.getAddresses()) {
                address.setLatitude(4.6730450);
                address.setLongitude(-74.0583310);
            }
        }
        int shoppingCartNumber = 0;
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(user).first().now();
        if (deliveryOrder != null) {
            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
            if (deliveryOrderItemList != null) {
                for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                    shoppingCartNumber += deliveryOrderItem.getQuantitySold();
                }
                //shoppingCartNumber = deliveryOrderItemList.size();
                customerJSON.setShopingCartNumber(shoppingCartNumber);
            }
        }
        //customerJSON.setIdStoreGroup(finalIdStoreGroup);
        Store store = this.getIdStoreGroupLatLonDefaultStores(latitude, longitude);

        City city = ofy().load().type(City.class).filter("id", store.getCity()).first().now();

        int idStoreGroup = city.getDefaultStore();

        customerJSON.setIdStoreGroup(idStoreGroup);

        return customerJSON;
    }

    private Boolean validateEmailGoogle(GoogleAuth userGoogle) throws IOException {

        CustomerGoogle result = ApiGatewayService.get().validateEmailGoogle(userGoogle);


        if (result.getData().getResult()) {
            User user = new User();
            user.setIdUser(UUID.randomUUID().toString());
            user.setRole("Customer");
            Key<User> userKey = Key.create(User.class, user.getIdUser());
            user.setIdUserWebSafe(userKey.toWebSafeString());
            user.setId(result.getData().getId());
            user.setIdUserWebSafe(userKey.toWebSafeString());
            user.setIdGoogle(userGoogle.getGoogleId());
            ofy().save().entity(user).now();
            return result.getData().getResult();
        }

        return result.getData().getResult();
    }

    @ApiMethod(name = "sendCodeLogin", path = "/customerEndpoint/sendCodeLogin", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer sendCodeLogin(final SendCondeLoginReq request) throws NotFoundException, BadRequestException {

        Answer response = new Answer();
        response.setConfirmation(false);

        if(Objects.isNull(request))
            throw new BadRequestException("Request is null.");
        if(Objects.isNull(request.getIdCustomer()))
            throw new BadRequestException("Request.idCustomer is null.");


        Optional<CustomerJSON> optionalCustomerJSON = ApiGatewayService.get().getCustomerById(request.getIdCustomer().intValue());

        if (!optionalCustomerJSON.isPresent()){
            throw new NotFoundException("No se encontro el usuario.");
        }

        final CustomerJSON customer = optionalCustomerJSON.get();

        if (Objects.isNull(customer.getId()))
            throw new NotFoundException("No se encontro el usuario.");

        int randomNum = (int) (Math.random() * 900000) + 100000;
        final String code = Integer.toString(randomNum);

        FirebaseService.get().notifyNewCodeLoginV2(new NotifyCodeLogin(String.valueOf(request.getIdCustomer()), code));

        int randomNumAux = (int) (Math.random() * 3) + 1;

        String messageAux;

        final MessageAlgoliaCode messageAlgoliaCode = APIAlgolia.getMessageCodeLogin();

        if (randomNumAux == 1) {
            messageAux = messageAlgoliaCode.getMessageOne();
        } else if (randomNumAux == 2) {
            messageAux = messageAlgoliaCode.getMessageTwo();
        } else if (randomNumAux == 3) {
            messageAux = messageAlgoliaCode.getMessageThree();
        } else {
            messageAux = ":code";
        }

        final String message = messageAux.replace(":name", customer.getFirstName()).replace(":code", code);

        if (Objects.nonNull(request.getByWhatsapp()) && request.getByWhatsapp().equals(Boolean.TRUE)) {
            try {
                if (Objects.nonNull(customer.getId())) {
                    //log.info("Envio de codigo por Whatsapp");

                    WhatsAapSendMessageConfig whatsaapSendMessageConfig = getWhatsappConfigMessage();

                    if (Objects.nonNull(whatsaapSendMessageConfig) && Objects.nonNull(whatsaapSendMessageConfig.getTemplateNameCode())
                            && Objects.nonNull(whatsaapSendMessageConfig.getTemplateNamespace())){

                        SendWhatsappCloudFunctionCodeReq req = new SendWhatsappCloudFunctionCodeReq();

                        String phone = customer.getPhone();
                        String countryCode = phone.substring(0, 2);
                        if (countryCode.equals("58") && Objects.equals(String.valueOf(phone.charAt(2)), "0")) {
                            phone = phone.replaceFirst("0", "");
                        }
                        String phonePrefix = "+" + phone;
                        req.setPhone(phonePrefix);
                        req.setCode(code);
                        req.setName(Objects.nonNull(customer.getFirstName()) ? customer.getFirstName() : "");
                        req.setTemplateName(whatsaapSendMessageConfig.getTemplateNameCode());
                        req.setTemplateNamespace(whatsaapSendMessageConfig.getTemplateNamespace());
                        //log.info("req whtasapp: " + req);
                        SendWhatsappCloudFunctionCodeRes responseCloud = CloudFunctionsService.get().sendWhatsappCode(req);

                        log.info("method sendCodeLogin: response cloudF: " + responseCloud.toString());
                    }
                    response.setConfirmation(true);
                    response.setMessage("success");
                }
            } catch (Exception e) {
                log.warning("method sendCodeLogin: Error to send Whatsapp:" + e.getMessage());
                return response;
            }
        }

        if (Objects.nonNull(request.getByCall()) && request.getByCall().equals(Boolean.TRUE)) {
            try {
                if (Objects.nonNull(customer.getId())) {
                    SendSMSCloudFunctionReq req = new SendSMSCloudFunctionReq();
                    req.setTo(customer.getPhone());
                    req.setBody(code);
                    CloudFunctionsService.get().postSendCodeByCall(req);
                    response.setConfirmation(true);
                    response.setMessage("success");
                }
            } catch (IOException e) {
                log.warning("method sendCodeLogin: Error to send SMS:" + e.getMessage());
                return response;
            }
        }

        if (Objects.nonNull(request.getByPhone()) && request.getByPhone().equals(Boolean.TRUE)) {
            try {
                final String finalMessage = message.replace(":hola ", "").replace(":hola, ", "");

                if (Objects.nonNull(customer.getId())) {
                    String phone = customer.getPhone();
                    String countryCode = phone.substring(0, 2);
                    if (countryCode.equals("58") && Objects.equals(String.valueOf(phone.charAt(2)), "0")) {
                        phone = phone.replaceFirst("0", "");
                    }
                    SendSMSCloudFunctionReq req = new SendSMSCloudFunctionReq();
                    req.setTo(phone);
                    req.setBody(finalMessage);
                    CloudFunctionsService.get().postSendSms(req);
                    response.setConfirmation(true);
                    response.setMessage("success");
                }
            } catch (IOException e) {
                log.warning("method sendCodeLogin: Error to send SMS:" + e.getMessage());
                return response;
            }
        }

        if (Objects.nonNull(request.getByEmail()) && request.getByEmail().equals(Boolean.TRUE)) {
            try {
//                log.info("Email -> " + request.getEmail());
                SendMailCodeLoginReq requestToSendMail = new SendMailCodeLoginReq();
                requestToSendMail.setCode(code);
                requestToSendMail.setEmail(customer.getEmail());
//                log.info("Request to Backend3 -> " + requestToSendMail.toString());
                Boolean responseAux = ApiGatewayService.get().sendMailCodeLogin(requestToSendMail);
                response.setConfirmation(responseAux);
                response.setMessage("success");
            } catch (Exception e) {
                log.warning("method sendCodeLogin: Error to send SMS:" + e.getMessage());
                return response;
            }
        }

        return response;
    }

    @ApiMethod(name = "getCustomerLoginV2", path = "/customerEndpoint/getCustomerLoginV2", httpMethod = ApiMethod.HttpMethod.POST)
    public CustomerNewLoginListRes getCustomerLoginV2(final CustomerNewLoginReq request, final HttpServletRequest req) throws IOException, ConflictException, NotFoundException {

        if (Objects.isNull(request))
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);

        List<CustomerDataLoginRes> responseCustomerLogin = new ArrayList<>();

        if (isTheLoginOfACustomerRegisteredByTheCallCenter(request)) {
            return processLoginOfACustomerRegisteredByTheCallCenter(request.getEmail(), request.getPhone());
        }

        List<CustomerNewLoginRes> listCustomers = ApiGatewayService.get().getDataForLogin(request);

        if (Objects.isNull(listCustomers) || listCustomers.isEmpty())
            throw new ConflictException(Constants.USER_NOT_FOUND_TO_PARAMETER);

        listCustomers.forEach(customerNewLoginRes -> {
            if (customerNewLoginRes.getOrigin().equals("EMAIL")) {
                CustomerDataLoginRes customer = new CustomerDataLoginRes();
                customer = buildCustomerDataLoginRes(customerNewLoginRes);
                if (Objects.nonNull(customer.getIdCustomerWebSafe()))
                    responseCustomerLogin.add(customer);
            }

            if (customerNewLoginRes.getOrigin().equals("GOOGLE")) {
                CustomerDataLoginRes customer = new CustomerDataLoginRes();
                customer = getCustomerKeysGoogle(customerNewLoginRes);
                if (Objects.nonNull(customer.getIdCustomerWebSafe()))
                    responseCustomerLogin.add(customer);
            }

            if (customerNewLoginRes.getOrigin().equals("FACEBOOK")) {
                CustomerDataLoginRes customer = new CustomerDataLoginRes();
                customer = getCustomerKeysFacebook(customerNewLoginRes);
                if (Objects.nonNull(customer.getIdCustomerWebSafe()))
                    responseCustomerLogin.add(customer);
            }

            if (customerNewLoginRes.getOrigin().equals("APPLE")) {
                CustomerDataLoginRes customer = new CustomerDataLoginRes();
                customer = getCustomerKeysApple(customerNewLoginRes);
                if (Objects.nonNull(customer.getIdCustomerWebSafe()))
                    responseCustomerLogin.add(customer);
            }

            if (customerNewLoginRes.getOrigin().equals("CALLCENTER")) {
                CustomerDataLoginRes customer = new CustomerDataLoginRes();
                customer = buildCustomerDataLoginRes(customerNewLoginRes);
                if (Objects.nonNull(customer.getIdCustomerWebSafe()))
                    responseCustomerLogin.add(customer);
            }
        });

        clearSecurityData(responseCustomerLogin);

        return new CustomerNewLoginListRes(responseCustomerLogin);
    }

    private static void clearSecurityData(List<CustomerDataLoginRes> responseCustomerLogin) {
        responseCustomerLogin.forEach(CustomerDataLoginRes::clearSecurityData);
    }

    private CustomerNewLoginListRes processLoginOfACustomerRegisteredByTheCallCenter(String email, String phone) throws ConflictException, IOException {
        CustomerResponse customerResponse = ApiGatewayService.get().getCustomerByEmail(email.toLowerCase());

        if (Objects.isNull(customerResponse))
            throw new ConflictException(Constants.USER_NOT_FOUND_TO_EMAIL);

        if (!Objects.equals(phone, customerResponse.getPhone()))
            throw new ConflictException(Constants.USER_NOT_FOUND_TO_PHONE);

        return buildCustomerNewLoginListRes(customerResponse);
    }

    @NotNull
    private CustomerNewLoginListRes buildCustomerNewLoginListRes(CustomerResponse customerResponse) {
        CustomerNewLoginRes customerNewLoginRes = buildCustomerNewLoginRest(customerResponse);
        CustomerDataLoginRes customer = buildCustomerDataLoginRes(customerNewLoginRes);
        CustomerNewLoginListRes customerNewLoginListRes = new CustomerNewLoginListRes(customer);
        clearSecurityData(customerNewLoginListRes.getCustomers());
        return customerNewLoginListRes;
    }

    private CustomerNewLoginRes buildCustomerNewLoginRest(CustomerResponse customerResponse) {
        List<CustomerNewLoginDataRes> customerNewLoginDataResList = new ArrayList<>();

        CustomerNewLoginDataRes customerNewLoginDataRes = new CustomerNewLoginDataRes();
        customerNewLoginDataRes.setType("phone");
        customerNewLoginDataRes.setData(customerResponse.getPhone().replaceAll("\\b(\\d{5})\\d+(\\d{4})", "$1*******$2"));

        CustomerNewLoginDataRes customerNewLoginDataResEmail = new CustomerNewLoginDataRes();
        String[] emailParts = customerResponse.getEmail().split("@");
        int length = (emailParts[0].length() / 2) + 1;
        customerNewLoginDataResEmail.setType("email");
        customerNewLoginDataResEmail.setData(customerResponse.getEmail().replaceAll("(?<=.{" + length + "}).(?=[^@]*?@)", "*"));

        customerNewLoginDataResList.add(customerNewLoginDataRes);
        customerNewLoginDataResList.add(customerNewLoginDataResEmail);

        CustomerNewLoginRes customerNewLoginRes = new CustomerNewLoginRes();
        customerNewLoginRes.setIdCustomer(Long.valueOf(customerResponse.getId()));
        customerNewLoginRes.setEmail(customerResponse.getEmail());
        customerNewLoginRes.setList(customerNewLoginDataResList);
        return customerNewLoginRes;
    }

    private CustomerDataLoginRes buildCustomerDataLoginRes(final CustomerNewLoginRes customerNewLoginRes) {
        try {
            Credential credential = getCredencial(customerNewLoginRes.getEmail().toLowerCase());

            if (Objects.nonNull(credential) || Objects.nonNull(credential.getOwner().getKey())) {
                User user = users.findUserByKey(credential.getOwner().getKey());
                if (Objects.nonNull(user) || Objects.nonNull(user.getId()) || Objects.nonNull(user.getIdUserWebSafe())) {
                    Token tokenTransport = generateToken();
                    Token tokenClient = new Token();
                    tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
                    tokenClient.setToken(tokenTransport.getToken());
                    encryptToken(tokenClient);
                    tokenClient.setTokenId(UUID.randomUUID().toString());
                    tokenClient.setOwner(Ref.create(credential.getOwner().getKey()));
                    tokenClient.setTokenExpDate(tokenTransport.getTokenExp());

                    Key<Token> keyToken = ofy().save().entity(tokenClient).now();

                    user.setIdUserWebSafe(credential.getOwner().getKey().toWebSafeString());
                    tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
                    user.setToken(tokenTransport);

                    user.setLastLogin(new Date().getTime());
                    credential.setLastLogin(new Date());
                    ofy().save().entities(credential, user);

                    customerNewLoginRes.setToken(tokenTransport.getToken());
                    customerNewLoginRes.setTokenIdWebSafe(tokenTransport.getTokenIdWebSafe());
                    customerNewLoginRes.setIdCustomerWebSafe(user.getIdUserWebSafe());
                    return new CustomerDataLoginRes(customerNewLoginRes);
                }
            }
            return new CustomerDataLoginRes();
        } catch (Exception e) {
            log.warning("method buildCustomerDataLoginRes() CustomerId -> #" + customerNewLoginRes.getIdCustomer() + " -> Error -> " + e.getMessage());
            return new CustomerDataLoginRes();
        }
    }

    private static boolean isTheLoginOfACustomerRegisteredByTheCallCenter(CustomerNewLoginReq request) {
        return Objects.nonNull(request.getEmail()) && Objects.nonNull(request.getPhone());
    }

    private boolean webSecurityIsEnabled(){

        Optional<SecurityWebConfig> optionalSecurityWebConfig = APIAlgolia.getSecurityConfig();

        if (optionalSecurityWebConfig.isPresent() && optionalSecurityWebConfig.get().getEnable() != null){

            return optionalSecurityWebConfig.get().getEnable();
        }

        return false;

    }

    private CustomerDataLoginRes getCustomerKeysGoogle(final CustomerNewLoginRes customerNewLoginRes) {
        try {
//            log.info(customerNewLoginRes.toString());
            User user = users.findUserByIdCustomerLastLogin(customerNewLoginRes.getIdCustomer().intValue());
            if (Objects.nonNull(user) || Objects.nonNull(user.getId()) || Objects.nonNull(user.getIdUserWebSafe())) {

                Key<User> keyUser = Key.create(User.class, user.getIdUser());
                Token tokenTransport = generateToken();

                Token tokenClient = new Token();
                tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
                tokenClient.setToken(tokenTransport.getToken());
                encryptToken(tokenClient);
                tokenClient.setTokenId(UUID.randomUUID().toString());
                tokenClient.setOwner(Ref.create(keyUser));
                tokenClient.setTokenExpDate(tokenTransport.getTokenExp());

                Key<Token> keyToken = ofy().save().entity(tokenClient).now();

                user.setIdUserWebSafe(keyUser.toWebSafeString());
                tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
                user.setToken(tokenTransport);

                user.setLastLogin(new Date().getTime());
                ofy().save().entity(user);


                customerNewLoginRes.setToken(tokenTransport.getToken());
                customerNewLoginRes.setTokenIdWebSafe(tokenTransport.getTokenIdWebSafe());
                customerNewLoginRes.setIdCustomerWebSafe(user.getIdUserWebSafe());

                return new CustomerDataLoginRes(customerNewLoginRes);
            }
        } catch (Exception e) {
            log.warning("method getCustomerKeysGoogle() CustomerId -> #" + customerNewLoginRes.getIdCustomer() + " -> Error -> " + e.getMessage());
//            log.warning("method getCustomerLoginV2() CustomerId -> #" + customerNewLoginRes.getIdCustomer() + " -> Error");
            return new CustomerDataLoginRes();
        }
        return new CustomerDataLoginRes();
    }

    private CustomerDataLoginRes getCustomerKeysFacebook(final CustomerNewLoginRes customerNewLoginRes) {
        try {
//            log.info(customerNewLoginRes.toString());
            User user = users.findUserByIdCustomerLastLogin(customerNewLoginRes.getIdCustomer().intValue());
            if (Objects.nonNull(user) || Objects.nonNull(user.getId()) || Objects.nonNull(user.getIdUserWebSafe())) {

                Key<User> keyUser = Key.create(User.class, user.getIdUser());
                Token tokenTransport = generateToken();

                Token tokenClient = new Token();
                tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
                tokenClient.setToken(tokenTransport.getToken());
                encryptToken(tokenClient);
                tokenClient.setTokenId(UUID.randomUUID().toString());
                tokenClient.setOwner(Ref.create(keyUser));
                tokenClient.setTokenExpDate(tokenTransport.getTokenExp());

                Key<Token> keyToken = ofy().save().entity(tokenClient).now();

                user.setIdUserWebSafe(keyUser.toWebSafeString());
                tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
                user.setToken(tokenTransport);

                user.setLastLogin(new Date().getTime());
                ofy().save().entity(user);


                customerNewLoginRes.setToken(tokenTransport.getToken());
                customerNewLoginRes.setTokenIdWebSafe(tokenTransport.getTokenIdWebSafe());
                customerNewLoginRes.setIdCustomerWebSafe(user.getIdUserWebSafe());

                return new CustomerDataLoginRes(customerNewLoginRes);
            }
        } catch (Exception e) {
            log.warning("method getCustomerKeysFacebook() CustomerId -> #" + customerNewLoginRes.getIdCustomer() + " -> Error -> " + e.getMessage());
//            log.warning("method getCustomerKeysFacebook() CustomerId -> #" + customerNewLoginRes.getIdCustomer() + " -> Error");
            return new CustomerDataLoginRes();
        }
        return new CustomerDataLoginRes();
    }

    private CustomerDataLoginRes getCustomerKeysApple(final CustomerNewLoginRes customerNewLoginRes) {
        try {
//            log.info(customerNewLoginRes.toString());
            User user = users.findUserByIdCustomerLastLogin(customerNewLoginRes.getIdCustomer().intValue());
            if (Objects.nonNull(user) || Objects.nonNull(user.getId()) || Objects.nonNull(user.getIdUserWebSafe())) {

                Key<User> keyUser = Key.create(User.class, user.getIdUser());
                Token tokenTransport = generateToken();

                Token tokenClient = new Token();
                tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
                tokenClient.setToken(tokenTransport.getToken());
                encryptToken(tokenClient);
                tokenClient.setTokenId(UUID.randomUUID().toString());
                tokenClient.setOwner(Ref.create(keyUser));
                tokenClient.setTokenExpDate(tokenTransport.getTokenExp());

                Key<Token> keyToken = ofy().save().entity(tokenClient).now();

                user.setIdUserWebSafe(keyUser.toWebSafeString());
                tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
                user.setToken(tokenTransport);

                user.setLastLogin(new Date().getTime());
                ofy().save().entity(user);


                customerNewLoginRes.setToken(tokenTransport.getToken());
                customerNewLoginRes.setTokenIdWebSafe(tokenTransport.getTokenIdWebSafe());
                customerNewLoginRes.setIdCustomerWebSafe(user.getIdUserWebSafe());

                return new CustomerDataLoginRes(customerNewLoginRes);
            }
        } catch (Exception e) {
            log.warning("method getCustomerKeysFacebook() CustomerId -> #" + customerNewLoginRes.getIdCustomer() + " -> Error -> " + e.getMessage());
//            log.warning("method getCustomerKeysFacebook() CustomerId -> #" + customerNewLoginRes.getIdCustomer() + " -> Error");
            return new CustomerDataLoginRes();
        }
        return new CustomerDataLoginRes();
    }

    @ApiMethod(name = "validateCodeLogin", path = "/customerEndpoint/validateCodeLogin", httpMethod = ApiMethod.HttpMethod.POST)
    public ValidateCodeLoginResponse validateCodeLogin(final ValidateCodeLoginReq request) throws ConflictException {
        ValidateCodeLoginResponse response = new ValidateCodeLoginResponse();
        response.setConfirmation(false);
        String msgException = "Has superado los intentos permitidos, intenta de nuevo en 5 minutos.";
        if (Objects.isNull(request)) {
            response.setMessage("request is null");
            return response;
        } else if (Objects.isNull(request.getCode())) {
            response.setMessage("request.code is null");
            return response;
        } else if (Objects.isNull(request.getIdCustomer())) {
            response.setMessage("request.customer is null");
            return response;
        }
//        log.info("Number -> " + (Objects.nonNull(request.getIdCustomer()) ? request.getIdCustomer() : "NoNumber") + " Codigo Enviado -> " + (Objects.nonNull(request.getCode()) ? request.getCode() : "NoNumber") );

        try {
            final String code = request.getCode();
            final String idCustomer = request.getIdCustomer();
            final String codeFirebase = FirebaseService.get().getLoginCodeV2(code, idCustomer);
            if (CachedDataManager.checkTriesLoginAndRegisterInCache("LOGIN-" + idCustomer)) {
                response.setConfirmation(false);
                response.setMessage(msgException);
                log.info(msgException);
                return response;
            }

            if (code.equals(codeFirebase)) {
                response.setConfirmation(true);
                response.setMessage("The code is valid");

                CustomerDataLoginRes customerDataTokens = getCustomerKeysFromId(idCustomer);

                response.setCustomerData(customerDataTokens);

            } else {
                response.setMessage("The code isn't valid");
            }
            return response;
        } catch (Exception e) {
            log.warning("method dataNewLogin() -> Error -> " + e.getMessage());
            throw new ConflictException("Ocurrio un error.");
        }
    }

    private CustomerDataLoginRes getCustomerKeysFromId(String idCustomer) {

        if (idCustomer == null || idCustomer.isEmpty()){
            return new CustomerDataLoginRes();
        }

        User user = users.findUserByIdCustomer(Integer.parseInt(idCustomer));

        if (user == null){
            return new CustomerDataLoginRes();
        }

        Token tokenTransport = generateToken();


        CustomerDataLoginRes customerDataLoginRes = new CustomerDataLoginRes();

        customerDataLoginRes.setIdCustomer(Long.parseLong(idCustomer));
        Key<User> keyUser = Key.create(User.class, user.getIdUser());

        Token tokenClient = new Token();
        tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
        tokenClient.setToken(tokenTransport.getToken());
        encryptToken(tokenClient);
        tokenClient.setTokenId(UUID.randomUUID().toString());
        tokenClient.setOwner(Ref.create(keyUser));
        tokenClient.setTokenExpDate(tokenTransport.getTokenExp());

        Key<Token> keyToken = ofy().save().entity(tokenClient).now();

        user.setIdUserWebSafe(keyUser.toWebSafeString());
        tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
        user.setToken(tokenTransport);

        user.setLastLogin(new Date().getTime());
        ofy().save().entity(user);

        customerDataLoginRes.setToken(tokenTransport.getToken());
        customerDataLoginRes.setTokenIdWebSafe(tokenTransport.getTokenIdWebSafe());
        customerDataLoginRes.setIdCustomerWebSafe(user.getIdUserWebSafe());

        return customerDataLoginRes;
    }


    /**
     * Brings all of the user's information, with the products that must be shown in the home screen.
     *
     * @param token             User's token.
     * @param tokenIdWebSafe    Identification of the User's token.
     * @param idCustomerWebSafe Secure Identificacion of Customer
     * @return CustomerJSON
     */
    @ApiMethod(name = "customer", path = "/customerEndpoint/customer", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerJSON customer(@Named("token") final String token,
                                 @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                 @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                 @Named("latitude") final double latitude,
                                 @Named("longitude") final double longitude,
                                 @Nullable @Named("deliveryType") final String deliveryType)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {
        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new ConflictException(Constants.USER_NOT_FOUND);
        int idStoreGroup = (int) this.getIdStoreGroupLatLon(latitude, longitude);
        if (deliveryType != null) {
            if (DeliveryType.valueOf(deliveryType).equals(DeliveryType.EXPRESS)) {
                idStoreGroup = (int) this.getIdStoreGroupLatLon(latitude, longitude);
            } else if (DeliveryType.valueOf(deliveryType).equals(DeliveryType.NATIONAL)) {
                idStoreGroup = 1000;
            } else if (DeliveryType.valueOf(deliveryType).equals(DeliveryType.ENVIALOYA)) {
                idStoreGroup = 1001;
            }
        } else {
            idStoreGroup = (int) this.getIdStoreGroupLatLon(latitude, longitude);
        }


        Optional<CustomerJSON> optionalCustomerJSON = this.customers.customerInformation(user, idStoreGroup, true);

        if (!optionalCustomerJSON.isPresent()){
            throw new ConflictException(Constants.CUSTOMER_NOT_FOUND);
        }
        CustomerJSON customerJSON = optionalCustomerJSON.get();

        if (customerJSON.getAddresses() != null && !customerJSON.getAddresses().isEmpty()) {
            for (Address address : customerJSON.getAddresses()) {
                City city = ofy().load().type(City.class).filter("deliveryType", "EXPRESS")
                        .filter("id", address.getCity())
                        .first().now();
                double lat = city != null ? city.getLatitude() : 4.6730450;
                double lng = city != null ? city.getLongitude() : -74.0583310;

                address.setLatitude(lat);
                address.setLongitude(lng);
            }
        }

        int shoppingCartNumber = 0;
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(user).first().now();
        if (deliveryOrder != null) {
            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
            if (deliveryOrderItemList != null) {
                for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                    shoppingCartNumber += deliveryOrderItem.getQuantitySold();
                }
                customerJSON.setShopingCartNumber(shoppingCartNumber);
            }
        }
        customerJSON.setIdStoreGroup(idStoreGroup);
        Query.Filter filterActive = new Query.FilterPredicate("isActive",
                Query.FilterOperator.EQUAL, true);
        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        List<DeliveryOrder> deliveryOrderList;
        deliveryOrderList = ofy().load().type(DeliveryOrder.class)
                .ancestor(customerKey).filter("currentStatus", 0)
                .filter(filterActive).list();

        deliveryOrderList.removeIf(deliveryOrderAux -> deliveryOrderAux.getDeliveryType().equals(DeliveryType.SCANANDGO));

        customerJSON.setActiveOrders(deliveryOrderList.size());
        return customerJSON;
    }

    /**
     * get on
     *
     * @param token             User's token.
     * @param tokenIdWebSafe    Identification of the User's token.
     * @param idCustomerWebSafe Secure Identificacion of Customer
     * @return CustomerJSON
     */
    @ApiMethod(name = "customerOnly", path = "/customerEndpoint/customerOnly", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerData customerOnly(@Named("token") final String token,
                                     @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                     @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                     @Named("latitude") final double latitude,
                                     @Named("longitude") final double longitude,
                                     @Nullable @Named("deliveryType") final String deliveryType)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {

        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new ConflictException(Constants.USER_NOT_FOUND);

        int idStoreGroup = 26;

        City city = null;
        try {
            Store store = this.getIdStoreGroupLatLonDefaultStores(latitude, longitude);
            city = ofy().load().type(City.class).filter("id", store.getCity()).first().now();
            idStoreGroup = city.getDefaultStore();
        } catch (Exception ex) {
            log.warning("No fue posible consultar la ciudad en el data Store. Se envía ciudad por defecto.");
        }
//        log.info("idStoreGroup assigned -> " + idStoreGroup);

        if (deliveryType != null) {
            if (DeliveryType.valueOf(deliveryType).equals(DeliveryType.NATIONAL)) {
                idStoreGroup = 1000;
            } else if (DeliveryType.valueOf(deliveryType).equals(DeliveryType.ENVIALOYA)) {
                idStoreGroup = 1001;
            }
        }

        Optional<CustomerData> optionalCustomerData = this.customers.setCustomerInformation(user, idStoreGroup, true);
        if (!optionalCustomerData.isPresent()) {
            throw new ConflictException(Constants.USER_NOT_FOUND);
        }
        CustomerData customerData = optionalCustomerData.get();

        int shoppingCartNumber = 0;
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(user).first().now();
        if (deliveryOrder != null) {
            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
            if (deliveryOrderItemList != null) {
                for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                    shoppingCartNumber += deliveryOrderItem.getQuantitySold();
                }
                customerData.setShopingCartNumber(shoppingCartNumber);
            }
        }
        customerData.setIdStoreGroup(idStoreGroup);
        Query.Filter filterActive = new Query.FilterPredicate("isActive",
                Query.FilterOperator.EQUAL, true);
        Key<Customer> customerKey = Key.create(idCustomerWebSafe);


        List<DeliveryOrder> deliveryOrderList;
        deliveryOrderList = ofy().load().type(DeliveryOrder.class)
                .ancestor(customerKey).filter("currentStatus", 0)
                .filter(filterActive).list();

        deliveryOrderList.removeIf(deliveryOrderAux -> deliveryOrderAux.getDeliveryType().equals(DeliveryType.SCANANDGO));

        customerData.setActiveOrders(deliveryOrderList.size());
        return customerData;
    }


    /**
     * get on
     *
     * @param token             User's token.
     * @param tokenIdWebSafe    Identification of the User's token.
     * @param idCustomerWebSafe Secure Identificacion of Customer
     * @return CustomerJSON
     */
    @ApiMethod(name = "getCustomer", path = "/customerEndpoint/getCustomer", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerOnlyData getCustomer(@Named("token") final String token,
                                        @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                        @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                        @Named("latitude") final double latitude,
                                        @Named("longitude") final double longitude,
                                        @Nullable @Named("deliveryType") final String deliveryType)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {

        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new ConflictException(Constants.USER_NOT_FOUND);

        if (user.getId() == 0)
            throw new ConflictException(Constants.USER_ANONYMOUS_ERROR);

        Store store = this.getIdStoreGroupLatLonDefaultStores(latitude, longitude);

        City city = ofy().load().type(City.class).filter("id", store.getCity()).first().now();

        int idStoreGroup = city.getDefaultStore();

//        log.info("idStoreGroup assigned -> " + idStoreGroup);

        if (deliveryType != null) {
            if (DeliveryType.valueOf(deliveryType).equals(DeliveryType.NATIONAL)) {
                idStoreGroup = 1000;
            } else if (DeliveryType.valueOf(deliveryType).equals(DeliveryType.ENVIALOYA)) {
                idStoreGroup = 1001;
            }
        }
        // CustomerOnlyData customerOnlyData = this.customers.setCustomerOnlyData(user, idStoreGroup, true);
        CustomerOnlyData customerOnlyData = null;
        try {
            customerOnlyData = this.customers.setCustomerOnlyData(user, idStoreGroup, true);
        } catch (SocketTimeoutException e) {
            log.warning("Error@getCustomer: SocketTimeoutException -> " + e.getMessage());
            throw new ConflictException(Constants.USER_NOT_FOUND);
        }

        if (Objects.isNull(customerOnlyData)) {
            log.warning("Error@getCustomer: customerOnlyData is null");
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);
        }

        int shoppingCartNumber = 0;
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(user).first().now();
        if (deliveryOrder != null) {
            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
            if (deliveryOrderItemList != null) {
                for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                    shoppingCartNumber += deliveryOrderItem.getQuantitySold();
                }
                customerOnlyData.setShopingCartNumber(shoppingCartNumber);
            }
        }
        customerOnlyData.setIdStoreGroup(idStoreGroup);
        Query.Filter filterActive = new Query.FilterPredicate("isActive",
                Query.FilterOperator.EQUAL, true);
        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        List<DeliveryOrder> deliveryOrderList;
        deliveryOrderList = ofy().load().type(DeliveryOrder.class)
                .ancestor(customerKey).filter("currentStatus", 0)
                .filter(filterActive).list();

        if (deliveryOrderList != null && !deliveryOrderList.isEmpty() ) {
            deliveryOrderList.removeIf(deliveryOrderAux -> Objects.equals(DeliveryType.SCANANDGO, deliveryOrderAux.getDeliveryType()));
            customerOnlyData.setActiveOrders(deliveryOrderList.size());
        }
        customerOnlyData.setPhotos(ApiGatewayService.get().getCustomerPhotos(user.getId()));


        Ref<User> referenceUser = Ref.create(userKey);
//        log.info("reference user -> " + referenceUser.toString());
        Credential credential = users.findCredentialByKey(referenceUser);


        if (credential != null && credential.getEmail() != null) {
//            log.info("crdential user -> " + credential.toString());
            customerOnlyData.setLastLoginEmail(credential.getEmail());
//            customerOnlyData.setLastLoginDate(credential.getLastLogin());
        } else {
            customerOnlyData.setLastLoginEmail(customerOnlyData.getEmail());
        }

//        SET BRAZE ID
        if (customerOnlyData.getEmail() != null && !customerOnlyData.getEmail().isEmpty()){

            Optional<String> optionalBrazeId = ApiGatewayService.get().getUUIDFromBraze(customerOnlyData.getEmail());

            if (optionalBrazeId.isPresent()){
                customerOnlyData.setAnalyticsUUID(optionalBrazeId.get());
            }

        }


        log.info("customerOnlyData -> " + customerOnlyData);

        if(customerOnlyData.getId() != null) {
            final String codeFirebase = FirebaseService.get().getLoginCodeV2("000", String.valueOf(customerOnlyData.getId()));
            if ( codeFirebase != null ) {
                FirebaseService.get().deleteLoginCodeV2(String.valueOf(customerOnlyData.getId()));
            }
        }

        return customerOnlyData;
    }

    /**
     * Brings all addresses from a user and can filter the addresses by the parameter deliveryType
     *
     * @param token             User's token.
     * @param tokenIdWebSafe    Identification of the User's token.
     * @param idCustomerWebSafe Secure Identificacion of Customer
     * @return CustomerJSON
     */
    @ApiMethod(name = "addresses", path = "/customerEndpoint/addresses", httpMethod = ApiMethod.HttpMethod.GET)
    public AddressesRes addresses(@Named("token") final String token,
                                  @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                  @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                  @Nullable @Named("deliveryType") final String deliveryType)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {
        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null || user.getId() == 0)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);

        List<Address> allAddresses = this.customers.getAddressesByIdCustomer(user);
        List<Address> addressesResList = new ArrayList<>();
        if (allAddresses != null && !allAddresses.isEmpty()) {
            for (Address address : allAddresses) {
                City city = ofy().load().type(City.class).filter("deliveryType", address.getDeliveryType())
                        .filter("id", address.getCity())
                        .first().now();
                if (address.getLatitude() == 0 || address.getLongitude() == 0) {
                    double lat = city != null ? city.getLatitude() : 4.6730450;
                    double lng = city != null ? city.getLongitude() : -74.0583310;
                    address.setLatitude(lat);
                    address.setLongitude(lng);
                }

                // add default store
                if (city != null) address.setDefaultStore(city.getDefaultStore());

                if (deliveryType != null && !deliveryType.isEmpty()
                        && address.getDeliveryType().getDeliveryType().equalsIgnoreCase(deliveryType)) {
                    addressesResList.add(address);
                }
            }
        }

        addressesResList = addressesResList.isEmpty() ? allAddresses : addressesResList;

        AddressesRes addressesRes = new AddressesRes();

        addressesRes.setAddresses(addressesResList);

        return addressesRes;
    }

    /**
     * Return all addresses by customer
     */


    @ApiMethod(name = "address", path = "/customerEndpoint/address", httpMethod = ApiMethod.HttpMethod.GET)
    public AddressesRes getAddress(@Named("token") final String token,
                                   @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                   @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                   @Nullable @Named("deliveryType") final String deliveryType)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {

        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        return customers.getAddressByCustomerWebSafe(idCustomerWebSafe, deliveryType);
    }


    /**
     * create new Address
     */

    @ApiMethod(name = "createAddress", path = "/customerEndpoint/address", httpMethod = ApiMethod.HttpMethod.POST)
    public AnswerAddNewAddress createAddress(@Named("token") final String token,
                                             @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                             @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                             AddAddressRequest addAddressRequest,
                                             final HttpServletRequest request) throws BadRequestException, ConflictException, UnauthorizedException, IOException {

        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);

        if (!addAddressRequest.isValid())
            throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);

        if (DeliveryType.valueOf(addAddressRequest.getDeliveryType().getDeliveryType()).equals(DeliveryType.EXPRESS) &&
                addAddressRequest.getLatitude() == 0 &&
                addAddressRequest.getLongitude() == 0 &&
                addAddressRequest.getAssignedStore() <= 0) {
            throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);
        }
        if (DeliveryType.valueOf(addAddressRequest.getDeliveryType().getDeliveryType()).equals(DeliveryType.NATIONAL) &&
                addAddressRequest.getAssignedStore() != 1000) {
            throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);
        }
        if (DeliveryType.valueOf(addAddressRequest.getDeliveryType().getDeliveryType()).equals(DeliveryType.ENVIALOYA) &&
                addAddressRequest.getLatitude() != 0 &&
                addAddressRequest.getLongitude() != 0 &&
                addAddressRequest.getAssignedStore() != 0) {
            throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);
        }

        if (user.getId() <= 0) {
            throw new UnauthorizedException(Constants.DEFAULT_MESSAGE_ANONYMOUS);
        }

        // FIX EXPRESS
        if (DeliveryType.valueOf(addAddressRequest.getDeliveryType().getDeliveryType()).equals(DeliveryType.EXPRESS) &&
                (addAddressRequest.getLatitude() == 0 || addAddressRequest.getLongitude() == 0)){
            throw new UnauthorizedException(Constants.ADDRESS_ERROR_DELIVERY_TYPE);
        }

        // FIX CHIA

        /*if (DeliveryType.valueOf(addAddressRequest.getDeliveryType().getDeliveryType()).equals(DeliveryType.NATIONAL)
                && addAddressRequest.getCity().equals("CHI")){
            throw new UnauthorizedException(Constants.ADDRESS_ERROR_DELIVERY_TYPE);
        }

        if (DeliveryType.valueOf(addAddressRequest.getDeliveryType().getDeliveryType()).equals(DeliveryType.EXPRESS)
                && addAddressRequest.getCity().equals("CHI") &&
                (addAddressRequest.getLatitude() == 0 || addAddressRequest.getLongitude() == 0)){
            throw new UnauthorizedException(Constants.ADDRESS_ERROR_DELIVERY_TYPE);
        }*/

        addAddressRequest.setIdCustomer(user.getId());

        try {
            AddressesRes addressesRes = ApiGatewayService.get().getAddressesByCustomerId(user.getId());
            if (addressesRes != null) {
                for (Address address : addressesRes.getAddresses()) {
                    if (address.getAddress() != null && address.getComments() != null &&
                            address.getAddress().equals(addAddressRequest.getAddress()) &&
                            address.getComments().equals(addAddressRequest.getComments())) {
                        throw new ConflictException(Constants.ADDRESS_EXIST);
                    }
                }
            }
        } catch (Exception ex) {
            log.warning("No fue posible consultar las direcciones del cliente: " + user.getId());
        }

//        log.info("request to Backend3 -> {" + addAddressRequest.toString() + "}");
        Optional<Address> optionalAddress = ApiGatewayService.get().createCustomerAddress(addAddressRequest, TraceUtil.getXCloudTraceId(request));


        // validate core Response
        if (!optionalAddress.isPresent()) throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);

        Address address = optionalAddress.get();

//        log.info("Address: {" + address.toString() + "}");
//
//        log.info("Backend3 response- > " + address.getIdAddress());
//        log.info("deliveryType -> " + address.getDeliveryType());
//        log.info("City -> " + address.getCity());
        AnswerAddNewAddress answer = new AnswerAddNewAddress();
        answer.setConfirmation(false);

        if (address.getIdAddress() != null && address.getDeliveryType() != null && address.getCity() != null) {

//            log.info("Buscando la ciudad en DS " + address.getDeliveryType());
            City city = ofy().load().type(City.class).filter("deliveryType", address.getDeliveryType())
                    .filter("id", address.getCity())
                    .first().now();

            answer.setConfirmation(true);
            answer.setMessage("the address has been added correctly");
            answer.setAddress(address);

            if (city != null) answer.getAddress().setDefaultStore(city.getDefaultStore());
        } else {
            answer.setMessage(Constants.DEFAULT_MESSAGE);
        }

        return answer;
    }

    /**
     * update address
     */

    @ApiMethod(name = "updateAddress", path = "/customerEndpoint/address", httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer updateAddress(@Named("token") final String token,
                                @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                final UpdateAddressRequest addressRequest) throws ConflictException, UnauthorizedException, BadRequestException, IOException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);

        if (!addressRequest.isValid())
            throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);

        if (DeliveryType.valueOf(addressRequest.getDeliveryType().getDeliveryType()).equals(DeliveryType.EXPRESS) &&
                addressRequest.getLatitude() == 0 &&
                addressRequest.getLongitude() == 0 &&
                addressRequest.getAssignedStore() <= 0) {
            throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);
        }
        if (DeliveryType.valueOf(addressRequest.getDeliveryType().getDeliveryType()).equals(DeliveryType.NATIONAL) &&
                addressRequest.getLatitude() != 0 &&
                addressRequest.getLongitude() != 0 &&
                addressRequest.getAssignedStore() != 0) {
            throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);
        }
        if (DeliveryType.valueOf(addressRequest.getDeliveryType().getDeliveryType()).equals(DeliveryType.ENVIALOYA) &&
                addressRequest.getLatitude() != 0 &&
                addressRequest.getLongitude() != 0 &&
                addressRequest.getAssignedStore() != 0) {
            throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);
        }

        Answer answer = new Answer();
        answer.setConfirmation(false);
        answer.setMessage(Constants.DEFAULT_MESSAGE);
        if (addressRequest.getIdAddress() > 0) {
            final String idCustomer = String.valueOf(user.getId());
//            log.info("Customer -> " + addressRequest.getIdCustomer());
            try {
                ValidateGeneralBool response = ApiGatewayService.get().updateCustomerAddres(addressRequest, idCustomer);
                if (response.getData() == Boolean.TRUE) {
                    answer.setConfirmation(true);
                    answer.setMessage(null);
                }
            } catch (Exception e) {
                log.warning("updateAddress: No se pudo actualizar la dirección del customer addressID " + addressRequest.getIdAddress());
            }
        }
        return answer;

    }


    /**
     * delete address
     */
    @ApiMethod(name = "deleteAddressV2", path = "/customerEndpoint/address", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteAddressv2(@Named("token") final String token,
                                  @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                  @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                  @Named("idAddress") final int idAddress) throws ConflictException, BadRequestException, UnauthorizedException, IOException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);

        Answer answer = new Answer();
        answer.setConfirmation(false);
        answer.setMessage(Constants.DEFAULT_MESSAGE);

        if (idAddress > 0) {
            CustomerAddressResponse res = ApiGatewayService.get().deleteCustomerByAddressId(idAddress);
            if (res != null && res.getCode() != null && res.getCode().equals("OK")) {
                answer.setConfirmation(true);
                answer.setMessage(null);
            }
        }
        return answer;
    }

    @ApiMethod(name = "getCreditCards", path = "/customerEndpoint/creditCard", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerCreditCard getCreditCards(@Named("token") final String token,
                                             @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                             @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                             @Nullable @Named("city") final String city,
                                             final HttpServletRequest req)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException {
        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Optional<RequestSourceEnum> sourceOptional = Optional.empty();
        try {
            RequestSourceEnum source = ftdUtilities.getSourceFromRequestHeader(req);
            sourceOptional = Optional.of(source);
        } catch (Exception e) {
            log.warning("Ocurrio un error verificando el source");
        }
        if(sourceOptional.isEmpty()){
            throw new ConflictException(Constants.ERROR_SOURCE_NULL);
        }

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);
        try {
            return new CustomerCreditCard(ApiGatewayService.get().getAllCreditCard(user.getId(), city, sourceOptional.get().name()));
        } catch (SocketTimeoutException e) {
            throw new ConflictException("SocketTimeoutException@creditCard -> " + e.getMessage());
        } catch (Exception e) {
            throw new ConflictException("Error@creditCard -> " + e.getMessage());
        }
    }


    /**
     * Updating Customer. Update or association of data user through the platform, in their database.
     * In the process, a security token for Firebase is returned and a token for petitions through the platform.
     *
     * @param creditCard Object of class 'CreditCardToken' that contain data to update or associate of a user exists (customer).
     * @return Object of class 'Customer' that contain data to store of a new banner.
     */
    @ApiMethod(name = "createCreditCard", path = "/customerEndpoint/createCreditCard", httpMethod = ApiMethod.HttpMethod.POST)
    public CreditCard createCreditCard(final CreditCardToken creditCard, final HttpServletRequest request)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException, NotFoundException {
        if (Objects.isNull(creditCard) || Objects.isNull(creditCard.getToken()) || Objects.isNull(creditCard.getToken().getTokenIdWebSafe()))
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);

        if (!authenticate.isValidToken(creditCard.getToken().getToken(), creditCard.getToken().getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        return ApiGatewayService.get().createCreditCard(customers.createCreditCardReq(creditCard), TraceUtil.getXCloudTraceId(request));
    }


    @ApiMethod(name = "deleteAddress", path = "/customerEndpoint/deleteAddress", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteAddress(@Named("token") final String token,
                                @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                @Named("id") final int id,
                                @Named("idAddress") final int idAddress)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        User user = users.findUserByIdCustomer(id);
        if (user == null)
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);

        // SS-266 Subscribe and Save.
        //TODO: Re-hacer el servicio en Backend 3.0 para realizar esta validacion...
    /*
    final String urlBackend30 = URLConnections.URL_PREVALIDATE_DEL_ADDRESS_BC_30.replace("{idAddress}", String.valueOf(idAddress));
    log.info("deleteAddress(): urlBackend30=" + urlBackend30);
    final CustomerPrevalidateDeleteBackend30 prevalidate = CoreConnection.getRequest(urlBackend30, CustomerPrevalidateDeleteBackend30.class);
    if(!prevalidate.getData()){
      throw new ConflictException(prevalidate.getMessage());
    }*/
        /*
        String urlString = URLConnections.URL_CUSTOMER_ADDRESS + idAddress;
        CoreConnection.deleteRequest(urlString);
        Answer answer = new Answer();
        answer.setConfirmation(true);*/

        Answer answer = new Answer();
        answer.setConfirmation(false);
        answer.setMessage(Constants.DEFAULT_MESSAGE);

        if (idAddress > 0) {
            CustomerAddressResponse res = ApiGatewayService.get().deleteCustomerByAddressId(idAddress);
            if (Objects.nonNull(res) && res.getCode() != null && res.getCode().equals("200")) {
                answer.setConfirmation(true);
                answer.setMessage(null);
            }
        }
        return answer;
    }

    @ApiMethod(name = "deleteCreditCard", path = "/customerEndpoint/deleteCreditCard", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteCreditCard(@Named("token") final String token,
                                   @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                   @Named("id") final int id,
                                   @Named("idCard") final int idCard)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        User user = users.findUserByIdCustomer(id);
        if (user == null)
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);

        final ValidateGeneralBool itCanDelete = ApiGatewayService.get().validateCreditCardForDelete(idCard);

        //log.warning("Response itCanDelete = " + itCanDelete.getData());

//    if (itCanDelete.getData().equals(Boolean.FALSE)){
//      throw new ConflictException("No se puede eliminar la tarjeta de credito por que tiene una subscripción activa");
//    }

        // SS-266 Subscribe and Save.
    /*
    //TODO: Re-hacer el servicio en Backend 3.0 para realizar esta validacion...
    final String urlBackend30 = URLConnections.URL_PREVALIDATE_DEL_CREDITCARD_BC_30.replace("{idCard}", String.valueOf(idCard));
    log.info("deleteCreditCard(): urlBackend30=" + urlBackend30);
    final CustomerPrevalidateDeleteBackend30 prevalidate = CoreConnection.getRequest(urlBackend30, CustomerPrevalidateDeleteBackend30.class);
    if(!prevalidate.getData()){
      throw new ConflictException(prevalidate.getMessage());
    }*/
        Answer answer = new Answer();
        answer.setConfirmation(false);
//    String urlString = URLConnections.URL_CUSTOMER_CREDITCARD + "/" + idCard + "/" + id;
//    CoreConnection.deleteRequest(urlString);

        if (idCard > 0 && itCanDelete.getData().equals(Boolean.TRUE)) {
            Response<com.imaginamos.farmatodo.model.customer.CustomerCreditCard> res = ApiGatewayService.get().deleteCreditCardByIdAndCustomerId((long)(idCard), (long)(id));
            if (Objects.nonNull(res) && res.isSuccessful()) {
                answer.setConfirmation(true);
            }
        } else {
            answer.setConfirmation(false);
            if (itCanDelete.getData() != null)
                answer.setMessage("Tienes una suscripción activa con esta tarjeta de crédito, por esto no la puedes eliminar.");
            else
                answer.setMessage("No se pudo eliminar la tarjeta de crédito. Por favor intenta nuevamente más tarde.");
        }
        return answer;
    }


    @ApiMethod(name = "tokenCreditCard", path = "/customerEndpoint/tokenCreditCard", httpMethod = ApiMethod.HttpMethod.GET)
    public com.imaginamos.farmatodo.model.customer.CustomerCreditCardToken tokenCreditCard(@Named("token") final String token,
                                                                                           @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                                                           @Named("id") final int id,
                                                                                           @Named("gateway") final String gateway)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        User user = users.findUserByIdCustomer(id);
        if (user == null)
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);

        Response<CustomerResult<CustomerCreditCardToken>> resultToken =
                ApiGatewayService.get().tokenCreditCardByIdAndCustomerId(gateway, (long)(id));

        if (!resultToken.isSuccessful()) {
            throw new BadRequestException(Constants.CUSTOMER_CREDIT_CARD_TOKENNOT_FOUND);
        }
        return resultToken.body().getData();

    }

    @ApiMethod(name = "gateway", path = "/customerEndpoint/gateway", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerCreditCardGateway gatewayActive(@Named("token") final String token,
                                                @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                @Nullable @Named("city") final String city)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Response<CustomerResult<CustomerCreditCardGateway>> resultToken =
                ApiGatewayService.get().gatewayActive(city);

        if (!resultToken.isSuccessful()) {
            throw new BadRequestException(Constants.CUSTOMER_CREDIT_CARD_TOKENNOT_FOUND);
        }
        return resultToken.body().getData();

    }

    @ApiMethod(name = "deleteCreditCardSupport", path = "/customerEndpoint/deleteCreditCardSupport", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteCreditCardSupport(@Named("id") final int id,
                                          @Named("idCard") final int idCard) throws BadRequestException, IOException {
        User user = users.findUserByIdCustomer(id);
        if (user == null)
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);

        // SS-266 Subscribe and Save.
        /**final String urlBackend30 = URLConnections.URL_PREVALIDATE_DEL_CREDITCARD_BC_30.replace("{idCard}", String.valueOf(idCard));
         log.info("deleteCreditCardSupport(): urlBackend30=" + urlBackend30);
         final CustomerPrevalidateDeleteBackend30 prevalidate;
         try {
         prevalidate = CoreConnection.getRequest(urlBackend30, CustomerPrevalidateDeleteBackend30.class);
         } catch (IOException e) {
         throw new BadRequestException(e);
         }

         if(!prevalidate.getData()){
         throw new BadRequestException(prevalidate.getMessage());
         }*/

        //String urlString = URLConnections.URL_CUSTOMER_CREDITCARD + "/" + idCard + "/" + id;
        //CoreConnection.deleteRequest(urlString);

        Response<com.imaginamos.farmatodo.model.customer.CustomerCreditCard> res = ApiGatewayService.get().deleteCreditCardByIdAndCustomerId((long)(idCard), (long)(id));
        Answer answer = new Answer();
        if (res.isSuccessful())
            answer.setConfirmation(true);
        else
            answer.setConfirmation(false);
        return answer;
    }

    @ApiMethod(name = "defaultCard", path = "/customerEndpoint/defaultCard", httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer defaultCard(@Named("token") final String token,
                              @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                              @Named("id") final int id,
                              @Named("idCard") final int idCard,
                              final HttpServletRequest request)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        User user = users.findUserByIdCustomer(id);
        if (user == null)
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);

        Response<com.imaginamos.farmatodo.model.customer.CustomerCreditCard> res = ApiGatewayService.get().setCreditCardDefault((long)(idCard), (long)(id), TraceUtil.getXCloudTraceId(request));
        Answer answer = new Answer();
        if (res.isSuccessful())
            answer.setConfirmation(true);
        else
            answer.setConfirmation(false);
        return answer;
    }


    @ApiMethod(name = "changePassword", path = "/customerEndpoint/changePassword", httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer changePassword(Customer customer)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {
        if (customer == null)
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        if (!authenticate.isValidToken(customer.getToken().getToken(), customer.getToken().getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);

        if (Objects.nonNull(customer.getPassword())) {
            final boolean isValidPassword = validatePassword(customer.getPassword());
            if (isValidPassword == false) {
                throw new ConflictException(Constants.PASSWORD_VALIDATE);
            }
        }

        User user = users.findUserByIdCustomer(customer.getId());
        if (user == null)
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);
        Key<User> userKey = Key.create(User.class, user.getIdUser());
        UserPass userPassword1 = ofy().load().type(UserPass.class).filter("active", true).ancestor(userKey).first().now();
        Credential credential = ofy().load().type(Credential.class).ancestor(userKey).filter("status", true).first().now();
        if (userPassword1 == null)
            throw new ConflictException("Password problem");
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        //log.warning("TEST: getOldPassword:"+customer.getOldPassword()+"<---> getPassword():"+ userPassword1.getPassword());
        if (!passwordEncryptor.checkPassword(customer.getOldPassword(), userPassword1.getPassword()))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        //String request = customers.changePasswordJson(customer).toJSONString();
        //CoreConnection.putRequest(URLConnections.URL_CUSTOMER_PASSWORD_CHANGE, request);

        ApiGatewayService.get().getCustomerChangePassword(new CustomerResetPasswordReq((long)(customer.getId()), customer.getOldPassword(), customer.getPassword()));

        Answer answer = new Answer();
        answer.setConfirmation(true);
        userPassword1.setActive(false);
        String passEnc = encrypt(customer.getPassword());
        UserPass userPass = new UserPass();
        userPass.setIdUserPass(UUID.randomUUID().toString());
        userPass.setOwner(Ref.create(userKey));
        userPass.setCreateAt(new Date());
        userPass.setActive(true);
        userPass.setPassword(passEnc);
        ofy().save().entities(userPass, userPassword1).now();
        this.changePasswordFirebase(credential.getEmail(), customer.getPassword());
        return answer;
    }

    @ApiMethod(name = "changePasswordV2", path = "/customerEndpoint/changePasswordV2", httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer changePasswordV2(HttpServletRequest data)
            throws BadRequestException, ConflictException, IOException {

//        log.info("request -> " + data.toString());
//        log.info("customerId -> " + data.getHeader("idcustomer"));
        CustomerChangePasswordV2 request = new CustomerChangePasswordV2();
        request.setIdCustomer(data.getHeader("idcustomer"));
//        log.info("idCustomer -> " + request.getIdCustomer());
        request.setValidationCode(data.getHeader("validationCode"));
        request.setPassword(data.getHeader("password"));
        request.setToken(data.getHeader("token"));
        request.setTokenIdWebSafe(data.getHeader("tokenIdWebSafe"));
        request.setIdCustomerWebSafe(data.getHeader("idCustomerWebSafe"));

        if (request == null)
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        if (!authenticate.isValidToken(request.getToken(), request.getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (Objects.isNull(request.getIdCustomer()))
            throw new BadRequestException("request.idCustomer is null");
        if (Objects.isNull(request.getPassword()))
            throw new BadRequestException("request.password is null");
        if (Objects.isNull(request.getValidationCode()))
            throw new BadRequestException("request.validationConde is null");

        if (Objects.nonNull(request.getPassword())) {
            final boolean isValidPassword = validatePassword(request.getPassword());
            if (isValidPassword == false) {
                throw new ConflictException(Constants.PASSWORD_VALIDATE);
            }
        }

        final String code = request.getValidationCode();
        final String idCustomer = request.getIdCustomer();
        final String codeFirebase = FirebaseService.get().getLoginCodeV2(code, idCustomer);

        Answer answer = new Answer();
        answer.setConfirmation(false);
//        log.info("Validando codigo de seguridad");
        if (Objects.nonNull(codeFirebase)) {
            if (code.equals(codeFirebase)) {
//                log.info("Se valido el codigo de seguridad");
                Key<User> userKey = Key.create(request.getIdCustomerWebSafe());
                User user = users.findUserByKey(userKey);

                if (user == null)
                    throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);

//                log.info("Validando el usuario request-> " + request.getIdCustomer() + " y USER -> " + user.getIdUser() + " USER -> " + user.getId());
                if (user.getId() == Long.valueOf(request.getIdCustomer())) {
//        Key<User> userKey = Key.create(User.class, user.getIdUser());
                    UserPass userPassword1 = ofy().load().type(UserPass.class).filter("active", true).ancestor(userKey).first().now();
                    Credential credential = ofy().load().type(Credential.class).ancestor(userKey).filter("status", true).first().now();
                    if (userPassword1 == null)
                        throw new ConflictException("Password problem");

                    ApiGatewayService.get().getCustomerChangePasswordV2(new CustomerResetPasswordReq(Long.parseLong(request.getIdCustomer()), request.getPassword()));


                    answer.setConfirmation(true);
                    userPassword1.setActive(false);
                    String passEnc = encrypt(request.getPassword());
                    UserPass userPass = new UserPass();
                    userPass.setIdUserPass(UUID.randomUUID().toString());
                    userPass.setOwner(Ref.create(userKey));
                    userPass.setCreateAt(new Date());
                    userPass.setActive(true);
                    userPass.setPassword(passEnc);
                    ofy().save().entities(userPass, userPassword1).now();
                    this.changePasswordFirebase(credential.getEmail(), request.getPassword());
                    answer.setMessage(Constants.CONFIRMATION_CHANGE_PASSWORD);
                    return answer;
                }
            } else {
//                log.info("El codigo de seguridad no es valido");
                answer.setMessage(Constants.CODE_INVALID);
                return answer;
            }
        }
        return answer;
    }

    @ApiMethod(name = "resetPassword", path = "/customerEndpoint/resetPassword", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer resetPassword(@Named("keyClient") final String keyClient,
                                Customer customer)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException, NotFoundException {
        if (customer == null)
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);

        Credential credential = getCredencial(customer.getEmail().toLowerCase());
        if (credential == null) {
            Answer answer = new Answer();
            //throw new BadRequestException(Constants.EMAIL_NOT_REGISTERED);
            String request = customers.resetPasswordJson(customer.getEmail().toLowerCase()).toJSONString();
            // Actuliza la contraseña en la BD
            CustomerResponse customerPassword = null;
            try {
                customerPassword = ApiGatewayService.get().getCustomerResetPassword(customer.getEmail().toLowerCase());
            } catch (Exception ex) {
                log.warning("method resetPassword: Error " + ex.getMessage());
                return getUserAnswer(ex.getMessage());
            }
            // Consulta el Usuario en la BD
            if (Objects.nonNull(customerPassword) && Objects.nonNull(customerPassword.getPassword()) && !customerPassword.getPassword().isEmpty()) {
                CustomerResponse customerResponse = ApiGatewayService.get().getCustomerByEmail(customer.getEmail().toLowerCase());
                //log.warning("method resetPassword customerResponse:" + customerResponse);
                Customer customerJSON = customerResponse.toCustomer();
                customerJSON.setPassword(customerPassword.getPassword());
                if (customerJSON != null) {
                    Key<User> userKey = Key.create(User.class, customerResponse.getId());
                    answer.setConfirmation(true);
                    List<UserPass> userPasswordList = ofy().load().type(UserPass.class).filter("active", true).ancestor(userKey).list();
                    String passEnc = encrypt(customerJSON.getPassword());
                    UserPass userPass = new UserPass();
                    userPass.setIdUserPass(UUID.randomUUID().toString());
                    userPass.setOwner(Ref.create(userKey));
                    userPass.setCreateAt(new Date());
                    userPass.setActive(true);
                    userPass.setPassword(passEnc);
                    customer.setPassword(customerJSON.getPassword());
                    ofy().save().entities(userPass).now();
                    if (Objects.nonNull(userPasswordList) && !userPasswordList.isEmpty()) {
                        //log.warning("method resetPassword inactiva contraseñas antiguas count():" + userPasswordList.size());
                        userPasswordList.stream().forEach(userPassword1 -> {
                            userPassword1.setActive(false);
                        });
                        ofy().save().entities(userPasswordList).now();
                    }

                    int responseCodeFirebase = this.changePasswordFirebase(customer.getEmail(), customerJSON.getPassword());
                    //log.warning("Response code firebase: " + responseCodeFirebase + " getPassword():"+customerJSON.getPassword());
                    // Creacion de usuario en el DataStore
                    createCustomer(customerJSON, customerResponse.toCustomerJson(), false, "Bogota", customerJSON.getTokenFacebook(), customerJSON.getTokenGoogle(), 26);
                    return answer;
                }
            } else {
                return getUserAnswer(null);
            }
        }
        User user = credential.getOwner().get();
        if (user == null)
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);
        Key<User> userKey = Key.create(User.class, user.getIdUser());
        UserPass userPassword1 = ofy().load().type(UserPass.class).filter("active", true).ancestor(userKey).first().now();

        CustomerResponse customerPassword = null;
        try {
            customerPassword = ApiGatewayService.get().getCustomerResetPassword(customer.getEmail().toLowerCase());
        } catch (Exception ex) {
            log.warning("method resetPassword: Error " + ex.getMessage());
            return getUserAnswer(ex.getMessage());
        }
        //String request = customers.resetPasswordJson(customer.getEmail().toLowerCase()).toJSONString();
        //Customer customerJSON = CoreConnection.postRequest(URLConnections.URL_CUSTOMER_PASSWORD_RESET, request, Customer.class);
        // Envio de contraseña por mensaje de Texto
        CustomerResponse customerResponse = null;
        try {
            customerResponse = ApiGatewayService.get().getCustomerByEmail(customer.getEmail().toLowerCase());
            SendSMSCloudFunctionReq req = new SendSMSCloudFunctionReq();
            req.setTo(customerResponse.getPhone());
            req.setBody(msgUtilities.obtainMsgAlgolia(MsgSmsEnum.MESSAGE_KEY_SMS)+ customerPassword.getPassword());
            CloudFunctionsService.get().postSendSms(req);

            SendWhatsappCloudFunctionReq reqW = new SendWhatsappCloudFunctionReq();
            reqW.setNumber(customerResponse.getPhone());
            reqW.setMessage("Tu clave para iniciar sesión en FARMATODO es: " + customerPassword.getPassword());
        } catch (Exception e) {
            log.warning("method resetPassword: Error to send sms:" + e);
        }
        // Envio de contraseña por Whatsapp
        try {
//            log.info("Envio de contraseña por Whatsapp");
            if (customerResponse == null) {
                customerResponse = ApiGatewayService.get().getCustomerByEmail(customer.getEmail().toLowerCase());
            }
            SendWhatsappCloudFunctionReq req = new SendWhatsappCloudFunctionReq();
            req.setNumber(customerResponse.getPhone());
            req.setMessage("Tu clave para iniciar sesión en FARMATODO es: " + customerPassword.getPassword());
//            SendWhatsappCloudFunctionRes respuesta = CloudFunctionsService.get().postSendWhatsapp(req);
            SendWhatsappCloudFunctionRes respuesta = CloudFunctionsService.get().postSendWhatsappV2(req);
//            log.info("respuesta ws:" + respuesta.getData());
        } catch (Exception e) {
            log.warning("method resetPassword: Error to send Whatsapp:" + e);
        }
        Answer answer = new Answer();
        if (customerPassword != null) {
            answer.setConfirmation(true);
            userPassword1.setActive(false);
            String passEnc = encrypt(customerPassword.getPassword());
            UserPass userPass = new UserPass();
            userPass.setIdUserPass(UUID.randomUUID().toString());
            userPass.setOwner(Ref.create(userKey));
            userPass.setCreateAt(new Date());
            userPass.setActive(true);
            userPass.setPassword(passEnc);
            customer.setPassword(customerPassword.getPassword());
            ofy().save().entities(userPass, userPassword1).now();
            int responseCodeFirebase = this.changePasswordFirebase(customer.getEmail(), customerPassword.getPassword());
            //log.warning("Respopnse code firebase: " + responseCodeFirebase);
        }
        return answer;
    }

    /* Support methods */

    @Deprecated
    @ApiMethod(name = "validateAddress", path = "/customerEndpoint/validateAddress", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer validateAddress(@Named("idCity") final String idCity,
                                  @Named("address") final String address,
                                  @Named("token") final String token,
                                  @Named("tokenIdWebSafe") final String tokenIdWebSafe)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException, NotFoundException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        //String request = customers.validateAddress(idCity, address).toJSONString();
        //CoreConnection.postRequest(URLConnections.URL_VALIDATE_ADDRESS, request, Void.class, Constants.WRONG_ADDRESS);

        ApiGatewayService.get().validateAddress(new AddAddressRequest(address, idCity));
        Answer answer = new Answer();
        answer.setConfirmation(true);
        return answer;
    }


    @ApiMethod(name = "deleteDevice", path = "/customerEndpoint/deleteDevice", httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer deleteDevice(DeleteDevice deleteDevice) throws ConflictException, BadRequestException {
        if (deleteDevice == null)
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);

        if (!authenticate.isValidToken(deleteDevice.getToken(), deleteDevice.getTokenIdWebSafe()))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(deleteDevice.getIdCustomerWebSafe());
        Query.Filter filter;
        Query.Filter filterId = new Query.FilterPredicate("deviceId", Query.FilterOperator.EQUAL, deleteDevice.getDeviceId());
        if (deleteDevice.getWeb() != null && deleteDevice.getWeb()) {
            Query.Filter filterToken = new Query.FilterPredicate("firebaseTokenDevice", Query.FilterOperator.EQUAL, deleteDevice.getFirebaseTokenDevice());
            filter = Query.CompositeFilterOperator.and(filterId, filterToken);
            //log.warning("isWeb");
        } else {
            filter = filterId;
            //log.warning("isWeb");
        }
        Answer answer = new Answer();
        answer.setConfirmation(false);
        List<DeviceRegistry> deviceRegistryList = ofy().load().type(DeviceRegistry.class).ancestor(userKey).filter(filter).list();
        if (deviceRegistryList != null && !deviceRegistryList.isEmpty()) {
            ofy().delete().entities(deviceRegistryList).now();
            answer.setConfirmation(true);
        }
        return answer;
    }

    @ApiMethod(name = "getPrivacyInfo", path = "/customerEndpoint/getPrivacyInfo", httpMethod = ApiMethod.HttpMethod.GET)
    public InfoPrivacy getPrivacyInfo(@Nullable @Named("token") final String token,
                                      @Nullable @Named("tokenIdWebSafe") final String tokenIdWebSafe)
            throws ConflictException, BadRequestException {
/*
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);



*/
        return ofy().load().type(InfoPrivacy.class).first().now();
    }

    @ApiMethod(name = "getPrivacyInfoV2", path = "/customerEndpoint/getPrivacyInfoV2", httpMethod = ApiMethod.HttpMethod.GET)
    public InfoPrivacy getPrivacyInfoV2(@Nullable @Named("token") final String token,
                                        @Nullable @Named("tokenIdWebSafe") final String tokenIdWebSafe)
            throws ConflictException, BadRequestException {
        /*if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);*/

        return ofy().load().type(InfoPrivacy.class).first().now();
    }

    @ApiMethod(
            name = "updateFavorites",
            path = "/customerEndpoint/customer/favorite",
            httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer updateFavorites(
            @Named("token") final String token,
            @Nullable @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            final FavoriteRequest request)
            throws BadRequestException, UnauthorizedException, ConflictException, InternalServerErrorException, ServiceUnavailableException {
//        log.info("method: updateFavorites()");
        if (token == null || token.isEmpty()) {
//            log.info("method: updateFavorites() --> BadRequest [token is required]");
            throw new BadRequestException("BadRequest [token is required]");
        }
        if (tokenIdWebSafe == null || tokenIdWebSafe.isEmpty()) {
//            log.info("method: updateFavorites() --> BadRequest [tokenIdWebSafe is required]");
            throw new BadRequestException("BadRequest [tokenIdWebSafe is required]");
        }
        if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
//            log.info("method: updateFavorites() --> ConflictException [bad credentials]");
            throw new UnauthorizedException(Constants.INVALID_TOKEN);
        }
        if (request == null) {
//            log.info("method: updateFavorites() --> BadRequest [request is required]");
            throw new BadRequestException("BadRequest [tokenIdWebSafe is required]");
        }
        if (request.getIdCustomerWebSafe() == null || request.getIdCustomerWebSafe().isEmpty()) {
            log.warning("method: updateFavorites() --> BadRequest [request.idCustomerWebSafe is required]");
            throw new BadRequestException("BadRequest [request.idCustomerWebSafe is required]");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
//            log.info("method: updateFavorites() --> BadRequest [request.idItem is required]");
            throw new BadRequestException("BadRequest [request.idItem is required]");
        }
        try {
            Key<Customer> customerKey = Key.create(request.getIdCustomerWebSafe());
            List<Favorite> favorites = ofy().load().type(Favorite.class).filter("customerKey", customerKey).list();
            for (Integer itemId : request.getItems()) {
                //Key<ItemGroup> itemGroupKey = Key.create(ItemGroup.class, "1");
                //Item item = ofy().load().key(Key.create(itemGroupKey, Item.class, Integer.toString(request.getIdItem()))).now();
                //if (item == null)
                //throw new ConflictException(Constants.PRODUCT_NOT_EXISTS);
                // validar si existe ese favorito para el cliente

                sendNonStockItemInBraze(itemId,"26",request.getIdCustomerWebSafe());

                Optional<Favorite> optionalFavorite = favorites.stream().filter(fav -> fav.getItemId().equals(itemId)).findFirst();

                // validar si el item ya existe entre los favoritos
                if (!optionalFavorite.isPresent()) {
                    // crear el objeto a persistir
                    Favorite favorite = new Favorite();
                    favorite.setFavoriteId(UUID.randomUUID().toString());
                    favorite.setCustomerKey(customerKey);
                    favorite.setItemId(itemId);
                    ofy().save().entity(favorite).now();
//                    log.info("method: updateFavorites() --> Favorito agregado.");
                } else {
                    // eliminar el registro
                    ofy().delete().entity(optionalFavorite.get()).now();
//                    log.info("method: updateFavorites() --> Favorito eliminado.");
                }
            }
            Answer answer = new Answer();
            answer.setConfirmation(true);
            answer.setMessage("Success");
            //log.warning("method: updateFavorites(); response -> confirmation: [" + answer.isConfirmation() + "], message: [" + answer.getMessage() + "]");
            return answer;
        } catch (Exception ex) {
            Answer answer = new Answer();
            answer.setConfirmation(false);
            answer.setMessage(ex.getMessage());
            //log.warning("method: updateFavorites(); response -> confirmation: [" + answer.isConfirmation() + "], message: [" + answer.getMessage() + "]");
            return answer;
        }
    }

    @ApiMethod(
            name = "getFavorites",
            path = "/customerEndpoint/customer/favorite",
            httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Item> getFavorites(
            @Named("token") final String token,
            @Nullable @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Nullable @Named("source") final EnableForEnum source,
            @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Nullable @Named("storeId") final String storeId,
            @Nullable @Named("deliveryType") final String deliveryType,
            @Nullable @Named("city") final String city)
            throws BadRequestException, UnauthorizedException, ConflictException, InternalServerErrorException, ServiceUnavailableException {
//        log.info("method: getFavorites()");
        if (token == null || token.isEmpty()) {
//            log.info("method: getFavorites() --> BadRequest [token is required]");
            throw new BadRequestException("BadRequest [token is required]");
        }
        if (tokenIdWebSafe == null || tokenIdWebSafe.isEmpty()) {
//            log.info("method: getFavorites() --> BadRequest [tokenIdWebSafe is required]");
            throw new BadRequestException("BadRequest [tokenIdWebSafe is required]");
        }
        if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
//            log.info("method: getFavorites() --> ConflictException [bad credentials]");
            throw new UnauthorizedException(Constants.INVALID_TOKEN);
        }
        if (idCustomerWebSafe == null || idCustomerWebSafe.isEmpty()) {
            log.warning("method: getFavorites() --> BadRequest [idCustomerWebSafe is required]");
            throw new BadRequestException("BadRequest [idCustomerWebSafe is required]");
        }
        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        List<Favorite> favorites = ofy().load().type(Favorite.class).filter("customerKey", customerKey).list();
        //log.info("favorites.size(): " + favorites.size());
        List<Item> items;
        if (favorites == null || favorites.isEmpty()) {
            items = null;
        } else {
            List<ItemQuery> itemQueryList = favorites.stream().map(item -> new ItemQuery(item.getItemId() + (URLConnections.MAIN_ID_STORE))).collect(Collectors.toList());
            List<ItemAlgolia> itemAlgoliaList = APIAlgolia.findItemByIdList(itemQueryList);
            if (Objects.nonNull(itemAlgoliaList) && !itemAlgoliaList.isEmpty()) {
                items = itemAlgoliaList.stream().filter(itemAlgolia -> Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getId()) && !itemAlgolia.getId().isEmpty())
                        .map(itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia)).filter(i -> Objects.nonNull(i)).collect(Collectors.toList());
                //log.info("El cliente con el key de DataStore: {" + customerKey + "} tiene estos items {" + items + "}");
                if(Objects.nonNull(idCustomerWebSafe))
                {
                    try{
                        TalonOneService talonOneService=new TalonOneService();
                        Key<User> userKey = Key.create(idCustomerWebSafe);
                        User user = users.findUserByKey(userKey);
                        talonOneService.sendItemsDirectToTalon(items,user.getId(),tokenIdWebSafe, idCustomerWebSafe,source, TalonAttributes.getTalonOneAttributes(storeId, deliveryType, city, source));
                    }catch (Exception e){
                        log.warning("Error sending order to TalonOneService: " + Arrays.toString(e.getStackTrace()));
                    }
                }
                return CollectionResponse.<Item>builder().setItems(items).build();
            }
        }
        return null;
    }

    private Answer getUserAnswer(final String message) {
        Answer answer = new Answer(false);
        if (Objects.isNull(message))
            answer.setMessage("Usuario no existe.");
        else
            answer.setMessage(message);
        return answer;
    }

    private GoogleAuth googleAuth(String token) {

        GoogleAuth googleAuth=null;

        if(Objects.isNull(token) || token.trim().isEmpty())  return googleAuth;

        String urlString = URLConnections.URL_GOOGLE_AUTH + token;
        try {
            GoogleResponse googleResponse = CoreConnection.getRequest(urlString, GoogleResponse.class);
            googleAuth = new GoogleAuth();
            googleAuth.setGoogleId(googleResponse != null ? googleResponse.getSub() : null);
            googleAuth.setEmail(googleResponse != null ? googleResponse.getEmail() : null);
            return googleAuth;
        } catch (Exception ex) {
            log.log(Level.WARNING, ex.getMessage()+" , URLGoogleAuth: "+urlString , ex);
            googleAuth = null;
        }
        return googleAuth;
    }


    private Token generateToken() {
        Token tokenClient = new Token();
        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator()); //investigar encriptacion
        try {
            tokenClient.setToken(oauthIssuerImpl.accessToken());
            tokenClient.setRefreshToken(oauthIssuerImpl.refreshToken());
            tokenClient.setTokenExp(7);
        } catch (OAuthSystemException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return tokenClient;
    }

    private String encrypt(String password) {
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        return passwordEncryptor.encryptPassword(password);
    }

    private void encryptToken(Token tokenClient) {
        tokenClient.setRefreshToken(encrypt(tokenClient.getRefreshToken()));
        tokenClient.setToken(encrypt(tokenClient.getToken()));
    }

    private long getIdStoreGroupLatLon(double latitude, double longitude)
            throws UnauthorizedException, BadRequestException,
            IOException, InternalServerErrorException {

        if (latitude == 0 || longitude == 0) {
            City city = ofy().load().type(City.class).filter("id", "BOG").first().now();
            if (city != null) {
                latitude = city.getLatitude();
                longitude = city.getLongitude();
            }
        }

        try {
            Store store = ApiGatewayService.get().getCloserCoordinates(latitude, longitude);
            if(Objects.nonNull(store)){
                return store.getNets().get(0).getStoreGroupId();
            }
            return ofy().load().type(City.class).filter("id", "BOG").first().now().getDefaultStore();
        }catch(Exception ex){
            log.warning("getIdStoreGroupLatLon: Error al consultar la tienda: "+ ex.getMessage());
            return ofy().load().type(City.class).filter("id", "BOG").first().now().getDefaultStore();
        }

        /*
        String urlString = URLConnections.URL_LOCATION_LATLON + latitude + "/" + longitude;
        HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.GET, urlString);
        int responseCode = httpURLConnection.getResponseCode();
        switch (responseCode) {
            case 200:
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder responseJson = new StringBuilder();

                while ((inputLine = bufferedReader.readLine()) != null) {
                    responseJson.append(inputLine);
                }
                bufferedReader.close();

                ObjectMapper objectMapper = new ObjectMapper();
                Store store = objectMapper.readValue(responseJson.toString(), Store.class);

                return store.getNets().get(0).getStoreGroupId();
            default:
                return ofy().load().type(City.class).filter("id", "BOG").first().now().getDefaultStore();
        }
         */
    }

    private Store getIdStoreGroupLatLonDefaultStores(double latitude, double longitude)
            throws UnauthorizedException, BadRequestException,
            IOException, InternalServerErrorException {

        if (latitude == 0 || longitude == 0) {
            City city = ofy().load().type(City.class).filter("id", "BOG").first().now();
            if (city != null) {
                latitude = city.getLatitude();
                longitude = city.getLongitude();
            }
        }

        Store defaultStore = new Store();
        defaultStore.setCity("BOG");

        try {
            Store store = ApiGatewayService.get().getCloserCoordinates(latitude, longitude);
            if(Objects.nonNull(store)){
                return store;
            }
            return defaultStore;
        }catch(Exception ex){
            log.warning("getIdStoreGroupLatLonDefaultStores: Error al consultar la tienda: "+ ex.getMessage());
            return defaultStore;
        }


        /*


        String urlString = URLConnections.URL_LOCATION_LATLON + latitude + "/" + longitude;
        HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.GET, urlString);
        int responseCode = httpURLConnection.getResponseCode();
        switch (responseCode) {
            case 200:
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder responseJson = new StringBuilder();

                while ((inputLine = bufferedReader.readLine()) != null) {
                    responseJson.append(inputLine);
                }
                bufferedReader.close();

                ObjectMapper objectMapper = new ObjectMapper();
                Store store = objectMapper.readValue(responseJson.toString(), Store.class);

                if (store == null) {
                    store = defaultStore;
                }
                log.info("assigned store -> " + store);

                return store;

        }


        return defaultStore;
          */
    }

    @SuppressWarnings("ALL")
    private int changePasswordFirebase(String email, String password) throws IOException {
        String urlStringFirebase = URLConnections.URL_PASSWORD_FIREBASE;
        HttpURLConnection httpURLConnectionFirebase = URLConnections.getConnection(URLConnections.POST, urlStringFirebase);
        OutputStreamWriter wrFirebase = new OutputStreamWriter(httpURLConnectionFirebase.getOutputStream(), "UTF-8");
        JSONObject firebaseRequest = new JSONObject();
        firebaseRequest.put("email", email);
        firebaseRequest.put("password", password);
        wrFirebase.write(firebaseRequest.toJSONString());
        wrFirebase.flush();
        return httpURLConnectionFirebase.getResponseCode();
    }

    private void createDevice(String firebaseTokenDevice, String deviceId, Key<User> userKey) {
//        log.info("call createDevice() deviceID -> " + deviceId);
        DeviceRegistry device = ofy().load().type(DeviceRegistry.class).ancestor(userKey).filter("deviceId", deviceId).first().now();
        if (device == null) {
            device = new DeviceRegistry();
            device.setIdDeviceRegistry(UUID.randomUUID().toString());
        }

        device.setOwner(Ref.create(userKey));
        device.setFirebaseTokenDevice(firebaseTokenDevice);
        device.setDeviceId(deviceId);
        device.setAvailable(true);
        ofy().save().entity(device);
    }

    @SuppressWarnings("ALL")
    private CustomerJSON loginFarmatodo(String email, String password, String idFacebook, String idGoogle, String traceId)
            throws UnauthorizedException, BadRequestException, InternalServerErrorException, IOException, NotFoundException, ConflictException {
        ErrorFarmatodo errorFarmatodo = null;
        JSONObject customerJson = new JSONObject();
        CustomerJSON customerJSON = null;

        if (idFacebook == null && idGoogle == null) {
            customerJson.put("email", email.toLowerCase());
            customerJson.put("password", password);
            customerJSON = ApiGatewayService.get().customerLoginEmail(URLConnections.URL_CRM_CUSTOMER_LOGIN_EMAIL, new CustomerLoginReq(email.toLowerCase(), password), traceId);
        } else if (idGoogle == null) {
            customerJson.put("facebookId", idFacebook);
            customerJSON = ApiGatewayService.get().customerLoginEmail(URLConnections.URL_CRM_CUSTOMER_LOGIN_FACEBOOK, new CustomerLoginReq(idFacebook), traceId);
        } else {
            customerJson.put("googleId", idGoogle);
            CustomerLoginReq req = new CustomerLoginReq();
            req.setGoogleId(idGoogle);
            req.setEmail(email.toLowerCase());
            customerJSON = ApiGatewayService.get().customerLoginEmail(URLConnections.URL_CRM_CUSTOMER_LOGIN_GOOGLE, req, traceId);
        }
        customerJSON.setComponents(null);
        customerJSON.setSuggested(null);
        customerJSON.setPurchases(null);

        if (customerJSON.getEmail() != null && !customerJSON.getEmail().isEmpty()){
            //TODO validar si aqui va esto.
            String documentBraze="";
            if (Objects.nonNull(customerJSON.getDocumentNumber())) {
                documentBraze=String.valueOf(customerJSON.getDocumentNumber());
                //log.info("document Braze createCustomer= " + documentBraze);
            }

            final  Optional<String> optionalBrazeUUID = ApiGatewayService
                    .get()
                    .getUUIDFromBrazeCreateUser(customerJSON.getEmail().toLowerCase(),documentBraze, null);

            if (optionalBrazeUUID.isPresent()){
                customerJSON.setAnalyticsUUID(optionalBrazeUUID.get());
            }
        }


        return customerJSON;
    }


    @ApiMethod(name = "deleteShoppingCart", path = "/customerEndpoint/customer/deleteShoppingCart", httpMethod = ApiMethod.HttpMethod.GET)
    public AnswerDeleteCart deleteShoppingCart(
            @Named("keyClient") final String keyClient,
            @Named("idCustomer") final int idCustomer
    ) throws UnauthorizedException {
        if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
        List<DeliveryOrder> deliveryOrdersSaved;
        AnswerDeleteCart answerDeleteCart = new AnswerDeleteCart();
        User userSaved;
        userSaved = ofy().load().type(User.class).filter("id", idCustomer).first().now();
        if (userSaved != null) {
            answerDeleteCart.setMessage("usuario encontrado");
            deliveryOrdersSaved = ofy().load().type(DeliveryOrder.class).ancestor(userSaved).list();
            if (deliveryOrdersSaved != null && !deliveryOrdersSaved.isEmpty()) {
                int cant = 0;
                for (DeliveryOrder order : deliveryOrdersSaved) {
                    if (order.getCurrentStatus() == 1 && order.getIdOrder() == 0) {
                        cant += 1;
                        answerDeleteCart.setDeliveryOrder(order);
                        order.setCurrentStatus(0);
                        ofy().delete().entity(order).now();

                    }
                }
                answerDeleteCart.setMessage("Carritos eliminados -> " + cant);

            } else {
                answerDeleteCart.setMessage("No fue posible encontrar el carrito para el usuario");
            }

        } else {
//            log.info("No fue posible encontrar el usuario");
            answerDeleteCart.setMessage("No fue posible encontrar el usuario");
        }

        return answerDeleteCart;
    }

    @ApiMethod(name = "deleteShoppingCartV2", path = "/customerEndpoint/customer/v2/deleteShoppingCart", httpMethod = ApiMethod.HttpMethod.GET)
    public AnswerDeleteCart deleteShoppingCartV2(@Named("keyClient") final String keyClient, @Named("idCustomer") final Long idCustomer, @Named("orderId") final Long orderId) throws UnauthorizedException {

//        log.info("method: deleteShoppingCartV2");
//        log.info("IdCustomer ->" + idCustomer + " OrderId ->"+ orderId);


        if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
        List<DeliveryOrder> deliveryOrdersSaved;
        AnswerDeleteCart answerDeleteCart = new AnswerDeleteCart();
        User userSaved;
        userSaved = ofy().load().type(User.class).filter("id", idCustomer).first().now();

        if (userSaved != null) {

//            log.info("Usuario encontrado - > " + userSaved.toString());

            DeliveryOrder deliveryOrderCustomer = ofy().load().type(DeliveryOrder.class).filter("idFarmatodo", idCustomer).filter("idOrder", orderId).filter("currentStatus", 1).first().now();

            if (deliveryOrderCustomer != null) {
                answerDeleteCart.setDeliveryOrder(deliveryOrderCustomer);
                deliveryOrderCustomer.setCurrentStatus(0);
                OfyService.ofy().save().entity(deliveryOrderCustomer).now();
            } else {
//                log.info("No fue posible encontrar el usuario");
                answerDeleteCart.setMessage("No fue posible encontrar el carrito para el usuario");
            }
        } else {
//            log.info("No fue posible encontrar el usuario");
            answerDeleteCart.setMessage("No fue posible encontrar el usuario");
        }

        return answerDeleteCart;
    }

    @ApiMethod(name = "blockUser", path = "/customerEndpoint/blockUser", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer blockUser(final BlockUserReq blockUserReq) throws BadRequestException {

        if (!blockUserReq.isValid())
            throw new BadRequestException("Bad Request");

        Answer answer = new Answer();
        answer.setConfirmation(false);


        // validar si el cliente ya a sido bloqueado
        BlockedUser blockedUserSaved = ofy().load().type(BlockedUser.class).filter("idUser", blockUserReq.getIdUser()).first().now();

        boolean userBlocked = blockedUserSaved != null;

//        log.info("usuario bloqueado -> " + userBlocked);

        if (userBlocked) {
            answer.setMessage("Error el usuario ya fue bloqueado anteriormente");
        } else {

            try {
                BlockedUser blockedUser = new BlockedUser();
                blockedUser.setIdBlockedUser(UUID.randomUUID().toString());
                blockedUser.setIdUser(blockUserReq.getIdUser());
                blockedUser.setReasonBlock(blockUserReq.getReasonBlock());
                OfyService.ofy().save().entity(blockedUser).now();

                answer.setMessage("Usuario bloqueado correctamente");
                answer.setConfirmation(true);

            } catch (Exception e) {
                log.warning("Error -> " + e.getMessage());
                throw new BadRequestException("Error al intentar bloquear el usuario");
            }
        }

        return answer;
    }

    @ApiMethod(name = "unBlockUser", path = "/customerEndpoint/unBlockUser", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer unBlockUser(final BlockUserReq blockUserReq) throws BadRequestException {
        if (!blockUserReq.isValid())
            throw new BadRequestException("Bad Request");

        Answer answer = new Answer();
        answer.setConfirmation(false);


        // validar si el cliente ya a sido bloqueado
        BlockedUser blockedUserSaved = ofy().load().type(BlockedUser.class).filter("idUser", blockUserReq.getIdUser()).first().now();

        boolean userBlocked = blockedUserSaved != null;

//        log.info("usuario bloqueado -> " + userBlocked);

        if (userBlocked) {
            ofy().delete().entity(blockedUserSaved).now();
            answer.setMessage("Usuario desbloqueado correctamente");
            answer.setConfirmation(true);
        } else {
            answer.setMessage("El usuario no esta bloqueado");
            answer.setConfirmation(false);
        }

        return answer;

    }

    @ApiMethod(name = "userIsBlocked", path = "/customerEndpoint/userIsBlocked", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer userIsBlocked(@Named("idUser") final long idUser) throws BadRequestException {
        if (idUser <= 0)
            throw new BadRequestException("Bad Request");

        Answer answer = new Answer();
        answer.setConfirmation(false);

        // validar si el cliente ya a sido bloqueado
        BlockedUser blockedUserSaved = ofy().load().type(BlockedUser.class).filter("idUser", idUser).first().now();

//        log.info("User -> " + (blockedUserSaved != null ? blockedUserSaved.toString() : null));
        boolean userBlocked = blockedUserSaved != null;

        if (userBlocked) {
            answer.setConfirmation(true);
            answer.setMessage("El usuario esta bloqueado");
        } else {
            answer.setMessage("El usuario no esta bloqueado");
            answer.setConfirmation(false);
        }

        return answer;

    }


    /**
     * Consultar el medio a traves del cual se registró un usuario.
     */
    @ApiMethod(name = "getUserOrigin", path = "/customerEndpoint/getUserOrigin", httpMethod = ApiMethod.HttpMethod.GET)
    public AnswerGetUserOrigin getUserOrigin(@Named("keyClient") final String keyClient, @Named("email") final String email) {
        try {
            if (Objects.isNull(keyClient) || keyClient.isEmpty())
                return new AnswerGetUserOrigin(400, "KeyClient required", null);
            else if (Objects.isNull(email) || email.isEmpty())
                return new AnswerGetUserOrigin(400, "Email required", null);
            else if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
                return new AnswerGetUserOrigin(401, "Unauthorized", null);
            CustomerOriginReq req = new CustomerOriginReq();
            req.setEmail(email.toLowerCase());
            return ApiGatewayService.get().getOriginByEmail(req);
        } catch (Exception e) {
            log.warning("getUserOrigin() => Error : " + e.getMessage());
            return new AnswerGetUserOrigin(204, "No Content", null, Constants.EMAIL_NOT_EXISTS);
        }
    }

    @ApiMethod(name = "handlerCustomerLifeMile", path = "/customerEndpoint/handlerCustomerLifeMile", httpMethod = ApiMethod.HttpMethod.POST)
    public CustomerJSON handlerCustomerLifeMile(final CustomerLifeMile customerLifeMile)
            throws UnauthorizedException, BadRequestException, ConflictException, IOException, InternalServerErrorException, NotFoundException {
//        log.info("method: handlerCustomerLifeMile");
        if (!authenticate.isValidToken(customerLifeMile.getToken(), customerLifeMile.getTokenIdWebSafe()))
            throw new UnauthorizedException(Constants.INVALID_TOKEN);
        if (Objects.isNull(customerLifeMile.getIdCustomerWebSafe()))
            throw new BadRequestException(Constants.CUSTOMER_INITIALIZATION);
        if (Objects.isNull(customerLifeMile.getLifeMileNumber()))
            throw new BadRequestException(Constants.CUSTOMER_LIFEMILE_VALIDATION);

        Key<User> userKey = Key.create(customerLifeMile.getIdCustomerWebSafe());
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new ConflictException(Constants.USER_NOT_FOUND);

        if (!FTDUtil.isLifeMileValid(customerLifeMile.getLifeMileNumber()))
            throw new BadRequestException(Constants.CUSTOMER_LIFEMILE_NOT_VALID);

        JSONObject customerLifeMileJson = new JSONObject();
        customerLifeMileJson.put("customer", user.getId());
        customerLifeMileJson.put("lifeMilesNumber", customerLifeMile.getLifeMileNumber());

//        log.info("method: handlerCustomerLifeMile: " + customerLifeMileJson.toString());
        //log.warning("method: handlerCustomerLifeMile: Se conecta al CORE de  -> " + URLConnections.URL_CUSTOMER_LIFEMILE);
        //return CoreConnection.postRequest(URLConnections.URL_CUSTOMER_LIFEMILE, customerLifeMileJson.toJSONString(), CustomerJSON.class);
        return ApiGatewayService.get().handleCustomerLifeMiles(new CustomerLifeMilesReq((long)(user.getId()), customerLifeMile.getLifeMileNumber()));
    }

    @ApiMethod(name = "calculateCustomerLifeMile", path = "/customerEndpoint/calculateCustomerLifeMile", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerLifeMileJSON calculateCustomerLifeMile(@Named("invoceValue") String invoceValue,
                                                          @Nullable @Named("sasValue") String sasValue,
                                                          @Nullable @Named("token") final String token,
                                                          @Nullable @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                          @Nullable @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                                          @Nullable @Named("idStoreGroup") final Integer idStoreGroup)
            throws UnauthorizedException, BadRequestException, ConflictException, IOException, InternalServerErrorException, NotFoundException, AlgoliaException {
//        log.info("method: calculateCustomerLifeMile invoiceValue:" + invoceValue);
        if (Objects.isNull(invoceValue))
            throw new BadRequestException(Constants.CUSTOMER_LIFEMILE_VALUE_VALIDATION);

        invoceValue = Objects.isNull(invoceValue) || invoceValue.isEmpty() ? "0" : invoceValue;
        sasValue = Objects.isNull(sasValue) || sasValue.isEmpty() ? "0" : sasValue;

        Double invoiceValue = Double.parseDouble(invoceValue) + Double.parseDouble(sasValue);
        /*JSONObject customerLifeMileJson = new JSONObject();
        customerLifeMileJson.put("invoiceValue", invoiceValue.toString());
        if(Objects.nonNull(idStoreGroup)) {
            customerLifeMileJson.put("idStoreGroup", APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroup));
        }*/
        List<CustomerLifeMilesReq.OrderDetailRequest> items = new ArrayList<>();
        if (Objects.nonNull(idCustomerWebSafe)) {
            Key<User> customerKey = Key.create(idCustomerWebSafe);
            DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(customerKey)).first().now();
            if (Objects.nonNull(deliveryOrder)) {
                final List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
                if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
                    items = deliveryOrderItemList.parallelStream()
                            .map(orderItem -> new CustomerLifeMilesReq.OrderDetailRequest(orderItem.getId(), orderItem.getQuantitySold()))
                            .collect(Collectors.toList());
                    /*
                    JsonArray items = deliveryOrderItemList.stream().map(orderItem -> {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("itemId", orderItem.getId());
                        jsonObject.addProperty("quantityRequested", orderItem.getQuantitySold());
                        return jsonObject;
                    }).reduce(new JsonArray(), (jsonArray, jsonObject) -> {
                        jsonArray.add(jsonObject);
                        return jsonArray;
                    }, (jsonArray, otherJsonArray) -> {
                        jsonArray.addAll(otherJsonArray);
                        return jsonArray;
                    });
                    customerLifeMileJson.put("items", items);*/
                }
            }
        }
        //log.info("method: calculateCustomerLifeMile: Se conecta al CORE de  -> " + URLConnections.URL_CUSTOMER_LIFEMILE_CALCULATE);
        //return CoreConnection.postRequest(URLConnections.URL_CUSTOMER_LIFEMILE_CALCULATE, customerLifeMileJson.toJSONString(), CustomerLifeMileJSON.class);
        return ApiGatewayService.get().calculateLifeMiles(
                new CustomerLifeMilesReq(invoiceValue, (Objects.nonNull(idStoreGroup) ? APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroup) : null), items));
    }

    @ApiMethod(name = "getCustomerLifeMile", path = "/customerEndpoint/getCustomerLifeMile", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerLifeMileJSON getCustomerLifeMile(@Named("token") final String token,
                                                    @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                    @Named("idCustomerWebSafe") final String idCustomerWebSafe)
            throws UnauthorizedException, BadRequestException, ConflictException, IOException, InternalServerErrorException, NotFoundException {
//        log.info("method: getCustomerLifeMile");
        if (Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe) ||
                token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()) {
            throw new UnauthorizedException(Constants.INVALID_TOKEN);
        }
        if (!authenticate.isValidToken(token, tokenIdWebSafe)) {
            throw new UnauthorizedException(Constants.INVALID_TOKEN);
        }
        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new ConflictException(Constants.USER_NOT_FOUND);
        /*JSONObject customerLifeMileJson = new JSONObject();
        customerLifeMileJson.put("customer", user.getId());
        return CoreConnection.postRequest(URLConnections.URL_CUSTOMER_LIFEMILE_NUMBER, customerLifeMileJson.toJSONString(), CustomerLifeMileJSON.class);*/
        return ApiGatewayService.get().getCustomerLifeMilesNumber(new CustomerLifeMilesReq((long)(user.getId())));
    }

    @ApiMethod(name = "inactiveCustomerLifeMile", path = "/customerEndpoint/inactiveCustomerLifeMile", httpMethod = ApiMethod.HttpMethod.PUT)
    public CustomerLifeMileJSON inactiveCustomerLifeMile(@RequestBody final CustomerLifeMile customerLifeMile)
            throws UnauthorizedException, BadRequestException, ConflictException, IOException, InternalServerErrorException, NotFoundException {
        log.info("method: inactiveCustomerLifeMile: " + customerLifeMile.toString());
        if (Objects.isNull(customerLifeMile) || Objects.isNull(customerLifeMile.getToken()) || Objects.isNull(customerLifeMile.getTokenIdWebSafe()) ||
                Objects.isNull(customerLifeMile.getIdCustomerWebSafe()) || customerLifeMile.getToken().isEmpty() ||
                customerLifeMile.getTokenIdWebSafe().isEmpty()) {
            throw new UnauthorizedException(Constants.INVALID_TOKEN);
        }
        if (!authenticate.isValidToken(customerLifeMile.getToken(), customerLifeMile.getTokenIdWebSafe())) {
            throw new UnauthorizedException(Constants.INVALID_TOKEN);
        }
        Key<User> userKey = Key.create(customerLifeMile.getIdCustomerWebSafe());
        User user = users.findUserByKey(userKey);

        if (user == null)
            throw new ConflictException(Constants.USER_NOT_FOUND);
        /*JSONObject customerLifeMileJson = new JSONObject();
        customerLifeMileJson.put("customer", user.getId());
        return CoreConnection.postRequest(URLConnections.URL_CUSTOMER_LIFEMILE_INACTIVE, customerLifeMileJson.toJSONString(), CustomerLifeMileJSON.class);*/
        return ApiGatewayService.get().inactiveLifeMiles(new CustomerLifeMilesReq((long)(user.getId())));
    }

    @SuppressWarnings("ALL")
    @ApiMethod(name = "getValidationTokenPhoneFinal", path = "/customerEndpoint/getValidationTokenPhoneFinal", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer getValidationTokenPhoneFinal(@Named("keyClient") final String keyClient,
                                               final Customer customer)
            throws UnauthorizedException, BadRequestException, IOException {
        if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
            throw new UnauthorizedException(Constants.ERROR_ACCESS_DENIED);
        Property property = ofy().load().type(Property.class).filter(Constants.DATASTORE_KEY_PROPERTY, Constants.DATASTORE_KEY_PROPERTY_CODE_PHONE).first().now();
        //log.warning("CODE" + property.getValue());

        Answer answer = new Answer();
        answer.setConfirmation(true);
        answer.setTokenFarmatodo("-" + property.getValue());
        return answer;
    }

    @ApiMethod(name = "emailCustomerClick", path = "/customerEndpoint/emailCustomerClick", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer emailCustomerClick(@Named("token") final String token,
                                     @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                     @Named("idCustomerWebSafe") final String idCustomerWebSafe,
                                     @Named("newEmail") final String newEmail
    ) throws ConflictException, BadRequestException, UnauthorizedException, IOException {

        if (token == null || tokenIdWebSafe == null || idCustomerWebSafe == null ||
                token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty())
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        if (newEmail == null || newEmail.isEmpty()) {
            throw new ConflictException(Constants.MESSAGE_MAIL_NOT_VALID);
        }

        return customers.changeEmailCustomerClick(idCustomerWebSafe, newEmail);
    }

    @ApiMethod(name = "updateCustomerEmail", path = "/customerEndpoint/updateCustomerEmail", httpMethod = ApiMethod.HttpMethod.PUT)
    public Answer updateCustomerEmail(final UpdateEmailCustomerReq request) throws BadRequestException, ConflictException, UnauthorizedException, IOException, URISyntaxException {

        if (!request.isValid()) {
            throw new BadRequestException("BadRequest [request is not valid]");
        }

        if (!authenticate.isValidToken(request.getToken(), request.getTokenIdWebSafe())) {
//            log.info("method: getFavorites() --> ConflictException [bad credentials]");
            throw new UnauthorizedException(Constants.INVALID_TOKEN);
        }

        Key<User> userKey = Key.create(request.getIdCustomerWebSafe());
        User user = users.findUserByKey(userKey);

        if (userKey == null || user == null) {
//            log.info("method: getFavorites() --> ConflictException [IdCustomerWebSafe Nor Found]");
            throw new UnauthorizedException(Constants.DEFAULT_MESSAGE);
        }

        // BlockedUser blockedUserSaved = ofy().load().type(BlockedUser.class).filter("idUser",idUser).first().now();

        EmailChangeUser emailChangeUser = ofy().load().type(EmailChangeUser.class).filter("userId", user.getId()).first().now();

//        log.info("emailchange -> " + emailChangeUser);
        if (emailChangeUser != null) {
            throw new ConflictException(Constants.ERROR_MAIL_CHANGE);
        }

        Answer answer = new Answer();

        Ref<User> referenceUser = Ref.create(userKey);
//        log.info("reference user -> " + referenceUser.toString());
        Credential credential = users.findCredentialByKey(referenceUser);

        boolean emailValid = Util.isValidEmailAddress(request.getNewEmail());

        if (!emailValid) {
            throw new ConflictException(Constants.MESSAGE_MAIL_NOT_VALID);
        }

        // validate if email aready exists
        Credential credentialNewEmail = users.findUserByEmail(request.getNewEmail());
        if (credentialNewEmail != null) {
            throw new ConflictException(Constants.ERROR_MAIL_ALREADY_EXISTS);
        }


        Optional<AlgoliaEmailConfig> emailConfigOptional = APIAlgolia.getEmailConfigLogin();
        if (!emailConfigOptional.isPresent() || emailConfigOptional.get().getMessage() == null) {
            throw new ConflictException(Constants.ERROR_MAIL_CONFIG);
        }

        // get username

        CustomerOnlyData customerData = ApiGatewayService.get().getCustomerOnlyById(user.getId());

        if (customerData == null || customerData.getFirstName() == null || customerData.getFirstName().isEmpty()) {
            throw new ConflictException(Constants.ERROR_MAIL_CONFIG);
        }

        AlgoliaEmailConfig emailConfig = emailConfigOptional.get();

        SendMailReq sendMailReq = new SendMailReq();

        sendMailReq.setSubject(emailConfig.getSubject());
        sendMailReq.setTo(request.getNewEmail());

        URIBuilder uriBuilder = new URIBuilder(URLConnections.URL_BCK2_CLICK_EMAIL_UPDATE)
                .addParameter("idCustomerWebSafe", request.getIdCustomerWebSafe())
                .addParameter("token", request.getToken())
                .addParameter("tokenIdWebSafe", request.getTokenIdWebSafe())
                .addParameter("newEmail", request.getNewEmail());

        ShortUrlReq shortUrlReq = new ShortUrlReq(uriBuilder.toString());

        Optional<ShortUrlRes> optionalShortUrlRes = CloudFunctionsService.get().shortUrl(shortUrlReq);
        if (!optionalShortUrlRes.isPresent() || !optionalShortUrlRes.get().isValid()) {
            throw new ConflictException(Constants.ERROR_MAIL_CONFIG);
        }

//        log.info("short url -> " + optionalShortUrlRes.get().toString());

        String msg = FTDUtil.replaceStringVar(emailConfig.getMessage(), "url", optionalShortUrlRes.get().getNewURL());
        msg = FTDUtil.replaceStringVar(msg, "username", customerData.getFirstName());

        sendMailReq.setText(msg);
//depecrated
//        boolean sendEmailSuccess = CloudFunctionsService.get().sendEmail(sendMailReq);

        boolean sendEmailSuccess = ApiGatewayService.get().sendEmailBraze(sendMailReq);

        if (sendEmailSuccess) {
            answer.setConfirmation(true);
            answer.setMessage(Constants.SUCCESS);
        } else {
            answer.setConfirmation(false);
            answer.setMessage(Constants.ERROR_EMAIL_USER);
        }

        return answer;
    }

    @ApiMethod(name = "emailToLowerCase", path = "/customerEndpoint/emailToLowerCase", httpMethod = ApiMethod.HttpMethod.PUT)
    public EmailToLowerCaseResponse emailToLowerCase(EmailToLowerCaseRequest request) {
        try {
            if (request == null)
                return new EmailToLowerCaseResponse(400, "Bad Request", "Request is null");

            if (request.getEmail() == null)
                return new EmailToLowerCaseResponse(400, "Bad Request", "Email is null");

            Credential credential = users.findUserByEmail(request.getEmail());
            if (credential != null) {
                final String NEW_EMAIL = credential.getEmail().toLowerCase();
                credential.setEmail(NEW_EMAIL);
                ofy().save().entity(credential).now();
                return new EmailToLowerCaseResponse(NEW_EMAIL, 200, "Ok", "Email updated");
            } else {
                return new EmailToLowerCaseResponse(404, "Not Found", "User not found");
            }
        } catch (Exception e) {
            log.warning("Error en emailToLowerCase. Mensaje: " + e.getMessage());
            return new EmailToLowerCaseResponse(500, "Internal Server Error", "Ocurrio un error inesperado.");
        }
    }

    /**
     * Consultar el medio a traves del cual se registró un usuario.
     */
    @ApiMethod(name = "getUserOriginByLocalId", path = "/customerEndpoint/getUserOriginByLocalId", httpMethod = ApiMethod.HttpMethod.GET)
    public AnswerGetUserOrigin getUserOriginByLocalId(@Named("keyClient") final String keyClient, @Named("localId") final String localId) {
        try {
            if (Objects.isNull(keyClient) || keyClient.isEmpty())
                return new AnswerGetUserOrigin(400, "KeyClient required", null);
            else if (Objects.isNull(localId) || localId.isEmpty())
                return new AnswerGetUserOrigin(400, "LocalId required", null);
            else if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
                return new AnswerGetUserOrigin(401, "Unauthorized", null);

            return ApiGatewayService.get().getOriginByPhone(new CustomerOriginReq(localId));
        } catch (Exception e) {
            log.warning("getUserOrigin() => Error : " + e.getMessage());
            return new AnswerGetUserOrigin(500, "Internal Server Error", null);
        }
    }

    /**
     * Consultar el medio a traves del cual se registró un usuario.
     */
    @ApiMethod(name = "getUserOriginByUid", path = "/customerEndpoint/getUserOriginByUid", httpMethod = ApiMethod.HttpMethod.GET)
    public AnswerGetUserOrigin getUserOriginByUid(@Named("keyClient") final String keyClient, @Named("uid") final String uid) {
        try {
            if (Objects.isNull(keyClient) || keyClient.isEmpty())
                return new AnswerGetUserOrigin(400, "KeyClient required", null);
            else if (Objects.isNull(uid) || uid.isEmpty())
                return new AnswerGetUserOrigin(400, "UID required", null);
            else if (!keyClient.equals(Constants.KEY_SECURE_CLIENT))
                return new AnswerGetUserOrigin(401, "Unauthorized", null);

            CustomerOriginReq req = new CustomerOriginReq();
            req.setUid(uid);
            return ApiGatewayService.get().getOriginByUID(req);
        } catch (Exception e) {
            log.warning("getUserOrigin() => Error : " + e.getMessage());
            return new AnswerGetUserOrigin(500, "Internal Server Error", null);
        }
    }

    /**
     * Send to Atom
     *
     * @param request
     * @return json object
     * @throws ConflictException
     * @throws BadRequestException
     * @throws IOException
     * @throws InternalServerErrorException
     */
    @ApiMethod(name = "sendCustomerToAtom", path = "/customerEndpoint/sendCustomerToAtom", httpMethod = ApiMethod.HttpMethod.PUT)
    public JSONObject sendCustomerToAtom(
            final CustomerAtomRequest request) throws ConflictException, BadRequestException, IOException, InternalServerErrorException {

        JSONObject objectJson = new JSONObject();
        objectJson.put("sourceCode", request.getSourceCode());
        objectJson.put("customerId", request.getCustomerId());

        HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.PUT, URLConnections.URL_SEND_CUSTOMER_ATOM);
//        log.info("URL:\n" + URLConnections.URL_SEND_CUSTOMER_ATOM);
//        log.info("Request:\n" + objectJson.toJSONString());
        OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
        wr.write(objectJson.toJSONString());
        wr.flush();
        int responseCode = httpURLConnection.getResponseCode();
//        log.info("Code response [" + responseCode + "]");
        switch (responseCode) {
            case 201:
            case 200:
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                String inputLine;
                StringBuilder responseJson = new StringBuilder();
                while ((inputLine = bufferedReader.readLine()) != null) {
                    responseJson.append(inputLine);
                }
                bufferedReader.close();
                ObjectMapper objectMapper = new ObjectMapper();
                JSONObject jsonObject = objectMapper.readValue(responseJson.toString(), JSONObject.class);
//                log.info("Response:\n" + jsonObject.toJSONString());
                return jsonObject;
            default:
                if (httpURLConnection.getErrorStream() != null) {
                    BufferedReader bufferedReaderError = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream(), "UTF-8"));
                    if (bufferedReaderError != null) {
                        String inputLineError;
                        StringBuilder responseJsonError = new StringBuilder();
                        while ((inputLineError = bufferedReaderError.readLine()) != null) {
                            responseJsonError.append(inputLineError);
                        }
                        bufferedReaderError.close();
                        ObjectMapper objectMapperError = new ObjectMapper();
                        return objectMapperError.readValue(responseJsonError.toString(), JSONObject.class);
                    } else {
                        throw new ConflictException(Constants.DEFAULT_MESSAGE);
                    }
                } else {
                    throw new ConflictException(Constants.DEFAULT_MESSAGE);
                }
        }
    }

   /*
//   DEPRECATED
   @ApiMethod(name = "home", path = "/customerEndPoint/v2/home", httpMethod = ApiMethod.HttpMethod.POST)
    public DynamicResponse getHomeTempV2(final HomeRequest homeRequest) throws ConflictException, BadRequestException, AlgoliaException, IOException {
        return getHomeAPI(homeRequest);
    }


    @ApiMethod(name = "home", path = "/customerEndPoint/home", httpMethod = ApiMethod.HttpMethod.POST)
    public DynamicResponse getHomeTemp(final HomeRequest homeRequest) throws ConflictException, BadRequestException, AlgoliaException, IOException {
        return getHomeAPI(homeRequest);
    }
    //   END  - DEPRECATED
*/
    @ApiMethod(name = "home", path = "/customerEndpoint/home", httpMethod = ApiMethod.HttpMethod.POST)
    public DynamicResponse getHome(final HomeRequest homeRequest) throws ConflictException, BadRequestException, AlgoliaException, IOException {
        return getHomeAPI(homeRequest);
    }

    @ApiMethod(name = "homeV2", path = "/customerEndpoint/v2/home", httpMethod = ApiMethod.HttpMethod.POST)
    public DynamicResponse getHomeV2(final HomeRequest homeRequest) throws ConflictException, BadRequestException, AlgoliaException, IOException, NotFoundException, ExecutionException, InterruptedException {
        return getHomeAPIV2(homeRequest);
    }

    @ApiMethod(name = "landingPages", path = "/customerEndpoint/landingPages", httpMethod = ApiMethod.HttpMethod.POST)
    public DynamicResponse getLandingPageProvider(final LandingPagesRequest landingPagesRequest) throws ConflictException, BadRequestException, AlgoliaException, IOException, NotFoundException {
        return getLandingPages(landingPagesRequest);
    }

    private DynamicResponse getLandingPages(LandingPagesRequest landingPagesRequest) throws ConflictException, BadRequestException, AlgoliaException, IOException, NotFoundException {
        if (Objects.isNull(landingPagesRequest)) {
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        }

//        log.info("request ->  " + landingPagesRequest.toString());

        if (Objects.isNull(landingPagesRequest.getToken()) ||
                Objects.isNull(landingPagesRequest.getIdCustomerWebSafe()) ||
                Objects.isNull(landingPagesRequest.getTokenIdWebSafe())) {
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        }

        if (Objects.isNull(landingPagesRequest.getSource()))
            throw new ConflictException(Constants.ERROR_SOURCE_NULL);

        if (Objects.isNull(landingPagesRequest.getIdStoreGroup()))
            throw new ConflictException(Constants.ERROR_ID_STORE_GROUP_NULL);

        /*if (!authenticate.isValidToken(landingPagesRequest.getToken(), landingPagesRequest.getTokenIdWebSafe())) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }*/

        Boolean isActiveLanding = APIAlgolia.isActiveLandingPages();
//        log.info("Landing pages is active? -> " + isActiveLanding);

        if (!isActiveLanding)
            throw new NotFoundException(Constants.PROVIDER_NOT_FOUND);

        ProviderResponse providerResponse = new ProviderResponse();

        if (Objects.nonNull(landingPagesRequest)){

            if (Objects.nonNull(landingPagesRequest.getProvider())) {
                String provider = landingPagesRequest.getProvider().toUpperCase();
                providerResponse = APIAlgolia.getProviderDataForLandingPage(provider);
                if (Objects.isNull(providerResponse))
                    throw new NotFoundException(Constants.PROVIDER_NOT_FOUND);
            }
        }

        HomeConfigAlgolia homeConfigAlgolia = new HomeConfigAlgolia();
        try{
            HomeConfig homeConfig = new HomeConfig();
            homeConfig.setHeaderComponents(providerResponse.getProviderConfig().getHeaderComponents());
            homeConfig.setBodyComponents(providerResponse.getProviderConfig().getBodyComponents());
            homeConfig.setFooterComponents(providerResponse.getProviderConfig().getFooterComponents());
            homeConfigAlgolia.setHomeConfig(homeConfig);
        }catch (Exception e){
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

        if (!homeConfigAlgolia.isValid()) {
            log.warning("info algolia is null");
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

        //log.info("Config Algolia -> " + homeConfigAlgolia.toString());

        int idStoreGroupFromReq = landingPagesRequest.getIdStoreGroup() < 0 ? 26 : landingPagesRequest.getIdStoreGroup();
        //log.info("id store group from request -> " + idStoreGroupFromReq);
        int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromReq);

        DynamicResponse dynamicResponse = new DynamicResponse();

        // validate source

        getOnlyDataFromSource(landingPagesRequest.getSource(), homeConfigAlgolia);

        List<DynamicSection> dynamicSectionList = new ArrayList<>();

        Key<User> userKey = Key.create(landingPagesRequest.getIdCustomerWebSafe());

        User user = users.findUserByKey(userKey);

        CustomerOnlyData customerOnlyData = ApiGatewayService.get().getCustomerOnlyById(user.getId());


        // get header sections.
        HomeInfoConfig homeInfoConfig = new HomeInfoConfig();
        homeInfoConfig.setHomeConfigAlgolia(homeConfigAlgolia);
        homeInfoConfig.setCustomerOnlyData(customerOnlyData);
        homeInfoConfig.setIdStoreGroup(idStoreGroup);
        homeInfoConfig.setProviderResponse(providerResponse);
        homeInfoConfig.setLandingPagesRequest(landingPagesRequest);
        homeInfoConfig.setHomeRequest(new HomeRequest());

        if (!homeInfoConfig.isValidLandingPages()) {
            log.warning("Error config Data Home");
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

        // get header sections.

        List<DynamicSection> headerSectionAuxList = getHeaderSectionsV2(homeInfoConfig);

        // get body sections.

        List<DynamicSection> bodySectionAuxList = getBodySectionsV2(homeInfoConfig);

        // get footer sections

        List<DynamicSection> footerSectionAuxList = getFooterSectionsV2(homeInfoConfig);

        // set header sections in response
        dynamicSectionList.addAll(headerSectionAuxList);

        // set body sections in response
        dynamicSectionList.addAll(bodySectionAuxList);

        // set footer sections in response
        dynamicSectionList.addAll(footerSectionAuxList);

        //set toolbarIcon and toolbarBackground
        setIconAndBackground(dynamicResponse, landingPagesRequest.getProvider());

        // set sections.
        dynamicResponse.setHomeSections(dynamicSectionList);

//        log.info("dynamicResponse ->  " + dynamicResponse.toString());

        try{
            TalonOneService talonOneService=new TalonOneService();
            talonOneService.sendItemsToTalon(dynamicResponse.getHomeSections(), user.getId(),
                    landingPagesRequest.getTokenIdWebSafe(), landingPagesRequest.getIdCustomerWebSafe(),
                    landingPagesRequest.getSource(), landingPagesRequest.getTalonOneData());
        }catch (Exception e){
            log.warning("Error sending order to TalonOneService: " + Arrays.toString(e.getStackTrace()));
        }

        return dynamicResponse;
    }

    private void setIconAndBackground(DynamicResponse dynamicResponse, String provider) {

        ProviderResponse providerResponse = null;

        if (Objects.nonNull(provider)){
            String providerName = provider.toUpperCase();
            providerResponse = APIAlgolia.getProviderDataForLandingPage(providerName);
        }

        if (Objects.nonNull(providerResponse)){
            dynamicResponse.setToolbarBackground(providerResponse.getToolbarBackground());
            dynamicResponse.setToolbarIcon(providerResponse.getToolbarIcon());
        }
    }


    @NotNull
    private DynamicResponse getHomeAPI(HomeRequest homeRequest) throws ConflictException, BadRequestException, AlgoliaException, IOException {
        if (Objects.isNull(homeRequest)) {
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        }

        if (!homeRequest.isValid()) {
            throw new ConflictException(Constants.ERROR_BAD_REQUEST);
        }
//        log.info("request ->  " + homeRequest.toString());
        //log.info("request is valid?  ->  " + homeRequest.isValid());

        if (Objects.isNull(homeRequest.getToken()) ||
                Objects.isNull(homeRequest.getIdCustomerWebSafe()) ||
                Objects.isNull(homeRequest.getTokenIdWebSafe())) {
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        }

        if (!authenticate.isValidToken(homeRequest.getToken(), homeRequest.getTokenIdWebSafe())) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }

        // get config algolia ->
        HomeConfigAlgolia homeConfigAlgolia = APIAlgolia.getHomeConfig();

        if (!homeConfigAlgolia.isValid()) {
            log.warning("info algolia is null");
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

        //log.info("Config Algolia -> " + homeConfigAlgolia.toString());

        int idStoreGroupFromReq = homeRequest.getIdStoreGroup() == null || homeRequest.getIdStoreGroup() < 0 ? 26 : homeRequest.getIdStoreGroup();
        //log.info("id store group from request -> " + idStoreGroupFromReq);
        int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromReq);

        DynamicResponse dynamicResponse = new DynamicResponse();

        // validate source

        getOnlyDataFromSource(homeRequest.getSource(), homeConfigAlgolia);

        // sections

        List<DynamicSection> dynamicSectionList = new ArrayList<>();

        Key<User> userKey = Key.create(homeRequest.getIdCustomerWebSafe());
        //         Key<Customer> customerKey = Key.create(homeRequest.getIdCustomerWebSafe());

        User user = users.findUserByKey(userKey);
        CustomerOnlyData customerOnlyData = null;
        try {
            customerOnlyData = ApiGatewayService.get().getCustomerOnlyById(user.getId());
        } catch (SocketTimeoutException e) {
            log.warning("Error@getHomeAPI: SocketTimeoutException -> " + e.getMessage());
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

//        log.info("User mail -> " + customerOnlyData.getEmail());
        if (customerOnlyData == null) {
            log.warning("Error@getHomeAPI: customerOnlyData is null");
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

        // get header sections.
        HomeInfoConfig homeInfoConfig = new HomeInfoConfig();
        homeInfoConfig.setHomeConfigAlgolia(homeConfigAlgolia);
        homeInfoConfig.setCustomerOnlyData(customerOnlyData);
        homeInfoConfig.setIdStoreGroup(idStoreGroup);
        homeInfoConfig.setHomeRequest(homeRequest);

        if (!homeInfoConfig.isValid()) {
            log.warning("Error config Data Home");
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }
        List<DynamicSection> headerSectionAuxList = getHeaderSections(homeInfoConfig);

        // get body sections.

        List<DynamicSection> bodySectionAuxList = getBodySections(homeInfoConfig);

        // get footer sections

        List<DynamicSection> footerSectionAuxList = getFooterSections(homeInfoConfig);

        // set header sections in response
        dynamicSectionList.addAll(headerSectionAuxList);

        // set body sections in response
        dynamicSectionList.addAll(bodySectionAuxList);

        // set footer sections in response
        dynamicSectionList.addAll(footerSectionAuxList);

        // set sections.
        dynamicResponse.setHomeSections(dynamicSectionList);


        return dynamicResponse;
    }

    @NotNull
    private DynamicResponse getHomeAPIV2(HomeRequest homeRequest) throws ConflictException, BadRequestException, AlgoliaException, IOException, NotFoundException, ExecutionException, InterruptedException {
        long initialTime = System.currentTimeMillis();
        if (Objects.isNull(homeRequest)) {
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        }

        if (!homeRequest.isValid()) {
            throw new ConflictException(Constants.ERROR_BAD_REQUEST);
        }
//        log.info("request ->  " + homeRequest.toString());
        //log.info("request is valid?  ->  " + homeRequest.isValid());

        if (Objects.isNull(homeRequest.getToken()) ||
                Objects.isNull(homeRequest.getIdCustomerWebSafe()) ||
                Objects.isNull(homeRequest.getTokenIdWebSafe())) {
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        }

        if (!authenticate.isValidToken(homeRequest.getToken(), homeRequest.getTokenIdWebSafe())) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }

        // get config algolia ->
        //HomeConfigAlgolia homeConfigAlgolia = CachedDataManager.algoliaHomeConfig();
        HomeConfigAlgolia homeConfigAlgolia = APIAlgolia.getHomeV2Config();

        if (!homeConfigAlgolia.isValid()) {
            log.warning("info algolia is null");
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

        //log.info("Config_Algolia -> " + homeConfigAlgolia.toString());

        int idStoreGroupFromReq = homeRequest.getIdStoreGroup() == null || homeRequest.getIdStoreGroup() < 0 ? 26 : homeRequest.getIdStoreGroup();
        //log.info("id store group from request -> " + idStoreGroupFromReq);
        //int idStoreGroup = CachedDataManager.defaultStoreAlgolia(idStoreGroupFromReq);
        int idStoreGroup = APIAlgolia.getDefaultStoreIdByStoreId(idStoreGroupFromReq);
        //String storeCity = CachedDataManager.storeCityAlgolia(idStoreGroupFromReq);
        String storeCity = APIAlgolia.getStoreCityByStoreId(idStoreGroupFromReq);
        //log.info("Store CITY --> " + storeCity);
        homeRequest.setCity(storeCity);

        //Validate the activation of square banner
        homeConfigAlgolia.getHomeConfig().setHeaderComponents(validateSquareBanner(homeConfigAlgolia.getHomeConfig().getHeaderComponents(),homeRequest));



        DynamicResponse dynamicResponse = new DynamicResponse();

        // validate source

        getOnlyDataFromSource(homeRequest.getSource(), homeConfigAlgolia);

        // sections

        List<DynamicSection> dynamicSectionList = new ArrayList<>();

        Key<User> userKey = Key.create(homeRequest.getIdCustomerWebSafe());
        //         Key<Customer> customerKey = Key.create(homeRequest.getIdCustomerWebSafe());

//        log.info("userKey >>" + userKey);
        User user = users.findUserByKey(userKey);
//        log.info("user >>" + user);
        CustomerOnlyData customerOnlyData = null;
        try {
            if (Objects.nonNull(user)) {
                customerOnlyData = ApiGatewayService.get().getCustomerOnlyById(user.getId());
//                log.info("userID >>" + user.getId());
            }
        } catch (SocketTimeoutException e) {
            log.warning("Error@getHomeAPIV2: SocketTimeoutException -> " + e.getMessage());
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

        try {
            Optional<RecommendConfig> config = APIAlgolia.getAlgoliaRecommendConfig();
            if(user.getId() != 0 && config.isPresent() && config.get().isDepartmentsAfinity()) {
//                log.info("Algolia recommend department activado");
                customerOnlyData.setAnalyticsUUID(ApiGatewayService.get().getUUIDFromBraze(customerOnlyData.getEmail()).orElse(null));
//                log.info("Algolia recommend department activado uuid asignado");
            }
        } catch (Exception e) {
            log.warning("No se pudo asignar uuid");
        }

        CompletableFuture<CustomerResponseCart> isCustomerPrimeFuture = CompletableFuture.supplyAsync(()
                -> this.isCustomerPrime(user.getId()));

//        log.info("User mail -> " + customerOnlyData.getEmail());
        if (customerOnlyData == null) {
            log.warning("Error@getHomeAPIV2: customerOnlyData is null");
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }
//        log.info("User mail -> " + customerOnlyData.getEmail());

        try {
            if(customerOnlyData.getId() != 0) {
                DataAtomUtilities dataAtomUtilities = new DataAtomUtilities();
                dataAtomUtilities.setCustomerId(customerOnlyData.getId().longValue());
                GenericResponse response = ApiGatewayService.get().updateCustomerAtom(dataAtomUtilities);
            }
        } catch (Exception e) {
            log.warning("Error enviando la actualización a Atom");
        }

        //select prime banner in header
//       isPrimeBanner(homeConfigAlgolia.getHomeConfig().getHeaderComponents(),isCustomerPrime);
//        //select prime banner in body
//        isPrimeBanner(homeConfigAlgolia.getHomeConfig().getBodyComponents(),isCustomerPrime);
//        //select prime banner in footer
//        isPrimeBanner(homeConfigAlgolia.getHomeConfig().getFooterComponents(),isCustomerPrime);


        // get header sections
        HomeInfoConfig homeInfoConfig = new HomeInfoConfig();
        homeInfoConfig.setHomeConfigAlgolia(homeConfigAlgolia);
        homeInfoConfig.setCustomerOnlyData(customerOnlyData);
        homeInfoConfig.setIdStoreGroup(idStoreGroup);
        homeInfoConfig.setHomeRequest(homeRequest);
        LandingPagesRequest landing = new LandingPagesRequest();
        landing.setSource(homeRequest.getSource());
        homeInfoConfig.setLandingPagesRequest(landing);

        if (!homeInfoConfig.isValid()) {
            log.warning("Error config Data Home");
            throw new ConflictException(Constants.DEFAULT_MESSAGE);
        }

//        log.info("Llamado servicios asincronamente");
        generateCarrouselsAsync(homeInfoConfig);

//        log.info("size of()"+ homeInfoConfig.getCarrouselItemListAsync().getFavorites().size());
//        log.info("size of()"+ homeInfoConfig.getCarrouselItemListAsync().getViewed().size());
//        log.info("size of()"+ homeInfoConfig.getCarrouselItemListAsync().getPurchases().size());
        List<DynamicSection> headerSectionAuxList = getHeaderSectionsV2(homeInfoConfig);



        List<DynamicSection> bodySectionAuxList = getBodySectionsV2(homeInfoConfig);

        // get footer sections

        List<DynamicSection> footerSectionAuxList = getFooterSectionsV2(homeInfoConfig);

        // set header sections in response
        dynamicSectionList.addAll(headerSectionAuxList);

        // set body sections in response
        dynamicSectionList.addAll(bodySectionAuxList);

        // set footer sections in response
        dynamicSectionList.addAll(footerSectionAuxList);

        // set sections.
        dynamicResponse.setHomeSections(dynamicSectionList);

        while(!isCustomerPrimeFuture.isDone()) {
            if(initialTime > System.currentTimeMillis() - 3000){
//                log.info("isCustomerPrimeFuture is  done time out");
                break;
            }
        }
        try {
            SavingCustomerNoPrime(isCustomerPrimeFuture.get(),user.getId());
            CustomerResponseCart responseCart = new CustomerResponseCart();
            responseCart.setActive(false);
            responseCart.setSavingUserPrime(0);
            addNewDynamicSession(dynamicResponse.getHomeSections(), isCustomerPrimeFuture.isDone() ? isCustomerPrimeFuture.get() : responseCart);
            isPrimeBannerDynamicSection(dynamicResponse.getHomeSections(), isCustomerPrimeFuture.isDone() ? isCustomerPrimeFuture.get().isActive(): responseCart.isActive());
        } catch (Exception e) {
            log.warning("Error@getHomeAPIV2: addNewDynamicSession");
        }
        TalonOneService talonOneService=new TalonOneService();
        talonOneService.sendItemsToTalon(dynamicResponse.getHomeSections(), user.getId(), homeRequest.getTokenIdWebSafe(), homeRequest.getIdCustomerWebSafe(),homeRequest.getSource(), homeRequest.getTalonOneData());
        deleteBannerBelleza(dynamicResponse);
        return dynamicResponse;
    }

    private List<Component> validateSquareBanner(List<Component> components,HomeRequest homeRequest){
        String component = APIAlgolia.getAppVersionConfig(homeRequest.getVersion(),homeRequest.getSource().toValue());

        for(Component componentBody: components){

            if(componentBody.getComponentType().getComponentType().equals("SQUARE_TWO_BANNER")){
                if(component.equals("SQUARE_TWO_BANNER")){
                    componentBody.setActive(true);
                }else{
                    componentBody.setActive(false);
                }

            } else if(componentBody.getComponentType().getComponentType().equals("BANNER")) {
                if (component.equals("BANNER")) {
                    componentBody.setActive(true);
                    List<EnableForEnum> appEnable = Arrays.asList(EnableForEnum.valueOf(homeRequest.getSource().toValue()));
                    componentBody.setEnableFor(appEnable);

                }
            }
        }

        return components;
    }


    /**
     * validate source data -> WEB,IOS,ANDROID,RESPONSIVE
     *
     * @param source
     * @param homeConfigAlgolia
     */
    private void getOnlyDataFromSource(EnableForEnum source, HomeConfigAlgolia homeConfigAlgolia) {

        //log.warning("source -> " + source.toValue());
        // remove header components
        if (Objects.nonNull(homeConfigAlgolia)
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig())
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig().getHeaderComponents())
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig().getBodyComponents())
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig().getFooterComponents())) {

            /* remove components according source */
            homeConfigAlgolia.getHomeConfig().getHeaderComponents().removeIf(component -> componentIsEnableForSource(component, source));
            homeConfigAlgolia.getHomeConfig().getBodyComponents().removeIf(component -> componentIsEnableForSource(component, source));
            homeConfigAlgolia.getHomeConfig().getFooterComponents().removeIf(component -> componentIsEnableForSource(component, source));

        }


    }

    private boolean componentIsEnableForSource(Component component, EnableForEnum source) {
        if (Objects.nonNull(component.getEnableFor())) {
            Optional<EnableForEnum> optionalEnableForEnum = component.getEnableFor().stream().filter(sourceAux -> sourceAux.equals(source)).findFirst();
            return !optionalEnableForEnum.isPresent();
        }
        return false;
    }

    /**
     * get footer sections from algolia
     *
     * @param homeInfoConfig
     * @return
     */
    private List<DynamicSection> getFooterSections(HomeInfoConfig homeInfoConfig) {
        HomeConfigAlgolia homeConfigAlgolia = homeInfoConfig.getHomeConfigAlgolia();
        if (homeConfigAlgolia != null && homeConfigAlgolia.isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig())
                && homeConfigAlgolia.getHomeConfig().isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig().getFooterComponents())) {
            List<Component> footerSectionsAlgolia = homeConfigAlgolia.getHomeConfig().getFooterComponents();
            List<DynamicSection> footerSectionsList = getHomeSections(footerSectionsAlgolia, homeInfoConfig);
            if (footerSectionsList != null) return footerSectionsList;

        }
        return new ArrayList<>();
    }

    /**
     * get footer sections from algolia
     *
     * @param homeInfoConfig
     * @return
     * @version v2
     */
    private List<DynamicSection> getFooterSectionsV2(HomeInfoConfig homeInfoConfig) throws NotFoundException {
        HomeConfigAlgolia homeConfigAlgolia = homeInfoConfig.getHomeConfigAlgolia();
        if (homeConfigAlgolia != null && homeConfigAlgolia.isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig())
                && homeConfigAlgolia.getHomeConfig().isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig().getFooterComponents())) {
            List<Component> footerSectionsAlgolia = homeConfigAlgolia.getHomeConfig().getFooterComponents();
//            log.info("footerSectionsAlgolia -> " + homeConfigAlgolia.getHomeConfig().getFooterComponents());
            List<DynamicSection> footerSectionsList = getHomeSectionsV2(footerSectionsAlgolia, homeInfoConfig);
            if (footerSectionsList != null) return footerSectionsList;

        }
        return new ArrayList<>();
    }


    /**
     * get body sections from algolia
     *
     * @param homeInfoConfig
     * @return
     */
    private List<DynamicSection> getBodySections(HomeInfoConfig homeInfoConfig) {

        HomeConfigAlgolia homeConfigAlgolia = homeInfoConfig.getHomeConfigAlgolia();
        if (homeConfigAlgolia != null && homeConfigAlgolia.isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig())
                && homeConfigAlgolia.getHomeConfig().isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig().getBodyComponents())) {
            List<Component> bodySectionsAlgolia = homeConfigAlgolia.getHomeConfig().getBodyComponents();

            List<DynamicSection> bodySectionsList = getHomeSections(bodySectionsAlgolia, homeInfoConfig);
            if (bodySectionsList != null) return bodySectionsList;

        }
        return new ArrayList<>();

    }

    /**
     * get body sections from algolia
     *
     * @param homeInfoConfig
     * @return
     * @version v2
     */
    private List<DynamicSection> getBodySectionsV2(HomeInfoConfig homeInfoConfig) throws NotFoundException {

        HomeConfigAlgolia homeConfigAlgolia = homeInfoConfig.getHomeConfigAlgolia();
        if (homeConfigAlgolia != null && homeConfigAlgolia.isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig())
                && homeConfigAlgolia.getHomeConfig().isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig().getBodyComponents())) {
            List<Component> bodySectionsAlgolia = homeConfigAlgolia.getHomeConfig().getBodyComponents();
//            log.info("bodySectionsAlgolia -> " + homeConfigAlgolia.getHomeConfig().getBodyComponents());
            List<DynamicSection> bodySectionsList = getHomeSectionsV2(bodySectionsAlgolia, homeInfoConfig);
            if (bodySectionsList != null) return bodySectionsList;

        }
        return new ArrayList<>();

    }

    /**
     * get header sections from algolia
     *
     * @param homeInfoConfig
     * @return sections
     */
    private List<DynamicSection> getHeaderSections(HomeInfoConfig homeInfoConfig) {

        HomeConfigAlgolia homeConfigAlgolia = homeInfoConfig.getHomeConfigAlgolia();
        if (homeConfigAlgolia != null && homeConfigAlgolia.isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig())
                && homeConfigAlgolia.getHomeConfig().isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig().getHeaderComponents())) {

            // order list header ascending by position
            List<Component> headerSectionsAlgolia = homeConfigAlgolia.getHomeConfig().getHeaderComponents();
            List<DynamicSection> headerSectionsList = getHomeSections(headerSectionsAlgolia, homeInfoConfig);


            if (headerSectionsList != null) return headerSectionsList;

        }
        return new ArrayList<>();
    }

    /**
     * get header sections from algolia
     *
     * @param homeInfoConfig
     * @return sections
     */
    private List<DynamicSection> getHeaderSectionsV2(HomeInfoConfig homeInfoConfig) throws NotFoundException {

        HomeConfigAlgolia homeConfigAlgolia = homeInfoConfig.getHomeConfigAlgolia();
        if (homeConfigAlgolia != null && homeConfigAlgolia.isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig())
                && homeConfigAlgolia.getHomeConfig().isValid()
                && Objects.nonNull(homeConfigAlgolia.getHomeConfig().getHeaderComponents())) {

            // order list header ascending by position
            List<Component> headerSectionsAlgolia = homeConfigAlgolia.getHomeConfig().getHeaderComponents();
            List<DynamicSection> headerSectionsList = getHomeSectionsV2(headerSectionsAlgolia, homeInfoConfig);


            if (headerSectionsList != null) return headerSectionsList;

        }
        return new ArrayList<>();
    }

    /**
     * create and get data from all sections
     *
     * @param sectionsAlgolia
     * @param homeInfoConfig
     * @return
     */
    @org.jetbrains.annotations.Nullable
    private List<DynamicSection> getHomeSections(List<Component> sectionsAlgolia, HomeInfoConfig homeInfoConfig) {
        if (Objects.nonNull(sectionsAlgolia)) {
            // sort
            sectionsAlgolia.sort(Comparator.comparing(Component::getPosition));

            // create data
            List<DynamicSection> responseDynamicSectionList = new ArrayList<>();
            try {
                sectionsAlgolia.forEach(
                        sectionAlgolia -> {
                            // log.info("method getHomeSections() section Algolia -> " +
                            // sectionAlgolia.toString());

                            DynamicSection dynamicSectionAux = new DynamicSection();

                            dynamicSectionAux.setRedirectURL(sectionAlgolia.getRedirectUrl());
                            dynamicSectionAux.setLabel(sectionAlgolia.getLabel());
                            dynamicSectionAux.setLabelWeb(sectionAlgolia.getLabelWeb());
                            dynamicSectionAux.setComponentType(sectionAlgolia.getComponentType());
                            dynamicSectionAux.setUrlBanner(sectionAlgolia.getUrlBanner());

                            // ** GET INFO FROM SECTION **
                            getDataFromSection(sectionAlgolia, dynamicSectionAux, homeInfoConfig);
                            //
                            if (sectionAlgolia.getActive() != null && sectionAlgolia.getActive()) {
                                if (dynamicSectionAux.getComponentType() != null) {
                                    responseDynamicSectionList.add(dynamicSectionAux);
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseDynamicSectionList;
        }
        return null;
    }

    /**
     * create and get data from all sections
     *
     * @param sectionsAlgolia
     * @param homeInfoConfig
     * @return
     */
    @org.jetbrains.annotations.Nullable
    private List<DynamicSection> getHomeSectionsV2(List<Component> sectionsAlgolia, HomeInfoConfig homeInfoConfig) throws NotFoundException {
        if (Objects.nonNull(sectionsAlgolia)) {
            // sort
            sectionsAlgolia.sort(Comparator.comparing(Component::getPosition));

            // create data
            List<DynamicSection> responseDynamicSectionList = new ArrayList<>();

            BannersDTFRes bannersDTFResponse = new BannersDTFRes();
            BannersDTFRes bannersDTFResponseMinLeft = new BannersDTFRes();

            if (Objects.isNull(homeInfoConfig.getProviderResponse()))
                bannersDTFResponse = getBannersDTFRes(homeInfoConfig);

            bannersDTFResponseMinLeft = getBannersDTFMinLeftV1(homeInfoConfig);
            deleteComponentNewHome(sectionsAlgolia, homeInfoConfig);

            try {
                BannersDTFRes finalBannersDTFResponse = bannersDTFResponse;
                ProviderResponse finalProviderResponse = homeInfoConfig.getProviderResponse();
                BannersDTFRes finalBannersDTFResponseMinLeft = bannersDTFResponseMinLeft;
                sectionsAlgolia.forEach(
                        sectionAlgolia -> {
//                            log.info("Section component -> " + sectionAlgolia.getComponentType());
                            DynamicSection dynamicSectionAux = new DynamicSection();

                            dynamicSectionAux.setRedirectURL(sectionAlgolia.getRedirectUrl());
                            dynamicSectionAux.setLabel(sectionAlgolia.getLabel());
                            dynamicSectionAux.setLabelWeb(sectionAlgolia.getLabelWeb());
                            dynamicSectionAux.setComponentType(sectionAlgolia.getComponentType());
                            dynamicSectionAux.setUrlBanner(sectionAlgolia.getUrlBanner());

                            // ** GET INFO FROM SECTION **
                            getDataFromSectionV2(sectionAlgolia, dynamicSectionAux, homeInfoConfig, finalBannersDTFResponse, finalProviderResponse, finalBannersDTFResponseMinLeft);
                            //
                            if (sectionAlgolia.getActive() != null && sectionAlgolia.getActive()) {

                                if (dynamicSectionAux.getComponentType() != null) {
//                                    log.info("add_component -> " + dinamicSectionAux.toString());

                                    if (componentIsValid(dynamicSectionAux)){
//                                        log.info("add_component -> " + dinamicSectionAux.toString());
                                        responseDynamicSectionList.add(dynamicSectionAux);
                                    }

                                }

                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseDynamicSectionList;
        }
        return null;
    }

    private void deleteComponentNewHome(List<Component> sectionsAlgolia, HomeInfoConfig homeInfoConfig) {
        if (homeInfoConfig.getCustomerOnlyData() != null
                && homeInfoConfig.getCustomerOnlyData().getId() != null
                && homeInfoConfig.getCustomerOnlyData().getId() != 0){
            for (Component component : sectionsAlgolia ){
                if (Objects.equals(component.getComponentType(), ComponentTypeEnum.BANNER_LOGIN)){
                    component.setActive(false);
                }

            }

        }

    }

    /**
     * valida si el componente tiene todos los requerimientos para mostrase en la respuesta del servicio
     * @param dynamicSectionAux
     * @return
     */
    private boolean componentIsValid(DynamicSection dynamicSectionAux) {

        if (dynamicSectionAux != null && dynamicSectionAux.getComponentType() != null){

            switch (dynamicSectionAux.getComponentType()) {
                case ITEM_LIST:
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case BANNER_LEFT_ADVERTISING:
                    //log.warning("verificando banners BANNER_LEFT_ADVERTISING");
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case BANNER_RIGHT_ADVERTISING:
                    //log.warning("verificando banners BANNER_RIGHT_ADVERTISING");
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case STATIC_BANNER:
                    //log.warning("verificando banners STATIC_BANNER");
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case GRID_BLOG_LIST:
                    //log.warning("verificando banners GRID_BLOG_LIST");
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case VIDEO_LIST:
                    //log.warning("verificando banners VIDEO_LIST");
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case SHORTCUT_BANNER:
                    //log.warning("verificando banners SHORTCUT_BANNER");
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case ITEM_LIST_TWO_ROWS:
                    //log.warning("verificando banners ITEM_LIST_TWO_ROWS");
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();
                case SMALL_ITEM_LIST:
                    return dynamicSectionAux.getList() != null && !dynamicSectionAux.getList().isEmpty();

            }
        }
        return true;

    }

    private boolean validateFavorites(HomeInfoConfig homeInfoConfig) {
//        log.info("method validateFavorites");
        CustomerOnlyData customerOnlyData = homeInfoConfig.getCustomerOnlyData();

        Key<Customer> customerKey = Key.create(homeInfoConfig.getHomeRequest().getIdCustomerWebSafe());
        List<Favorite> favorites = ofy().load().type(Favorite.class).filter("customerKey", customerKey).list();

        boolean isValidFavorites = customerOnlyData != null && favorites != null && !favorites.isEmpty();
//        if (isValidFavorites) {
//            log.info("FAVORITES_IS_VALID");
//        }else {
//            log.info("FAVORITES_IS_NOT_VALID");
//        }

        return isValidFavorites;
    }

    /**
     * generate data from section.
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     */
    private void getDataFromSection(
            Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {
        if (Objects.nonNull(sectionAlgolia)
                && Objects.nonNull(sectionAlgolia.getComponentType())
                && Objects.nonNull(dynamicSectionAux)) {

            switch (sectionAlgolia.getComponentType()) {
                case WEB_DIRECT_BANNER:
                case HTML_LABEL:
                    setLabelHtml(sectionAlgolia, dynamicSectionAux, homeInfoConfig, new ProviderResponse());
                    break;
                case GRID_ITEM_LIST:
                case ITEM_LIST:
                case SMALL_ITEM_LIST:
                    setItemListData(sectionAlgolia, dynamicSectionAux, homeInfoConfig);
                    break;
                case TRACKING_INFO:
                    setTrackingInfo(sectionAlgolia, dynamicSectionAux, homeInfoConfig);
                    break;
                case CIRCLE_BANNER:
                    setCircleBanners(sectionAlgolia, dynamicSectionAux);
                    break;
                case SQUARE_BANNER:
                    setSquareBanners(sectionAlgolia, dynamicSectionAux);
                    break;
                case BANNER:
                    //log.info("METODOTO BANNERS --> " );
                    setBannersHome(sectionAlgolia, dynamicSectionAux, homeInfoConfig);
                    break;

            }
        }
    }

    /**
     * generate data from section.
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     */
    private void getDataFromSectionV2(
            Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig, BannersDTFRes bannersDTFResponse, ProviderResponse providerResponse, BannersDTFRes bannersDTFResponseMinLeft) {
        if (Objects.nonNull(sectionAlgolia)
                && Objects.nonNull(sectionAlgolia.getComponentType())
                && Objects.nonNull(dynamicSectionAux)) {

            switch (sectionAlgolia.getComponentType()) {
                case WEB_DIRECT_BANNER:
                case HTML_LABEL:
                    setLabelHtml(sectionAlgolia, dynamicSectionAux, homeInfoConfig, providerResponse);
                    break;
                case GRID_ITEM_LIST:
                case ITEM_LIST:
                case BRAZE_CAROUSEL:
                case SMALL_ITEM_LIST:
                case FLASH_OFFERS:
                    setItemListData(sectionAlgolia, dynamicSectionAux, homeInfoConfig);
                    break;
                case TRACKING_INFO:
                    setTrackingInfo(sectionAlgolia, dynamicSectionAux, homeInfoConfig);
                    break;
                case CIRCLE_BANNER:
                    setCircleBanners(sectionAlgolia, dynamicSectionAux);
                    break;
                //case BRAZE_CAROUSEL:
                //    setBrazeEmptyCarousel(sectionAlgolia, dynamicSectionAux);
                //    break;
                case SQUARE_BANNER:
                    setSquareBanners(sectionAlgolia, dynamicSectionAux);
                    break;
                case BANNER:
                //case BANNER_LOGIN:
                case SQUARE_TWO_BANNER:
                    setMainBanners(sectionAlgolia, dynamicSectionAux, homeInfoConfig, bannersDTFResponse);
                    break;
                case BANNER_LOGIN:
                    break;
                case MIN_BANNER_LEFT:
                    setMainBannersMinLeft(sectionAlgolia, dynamicSectionAux, homeInfoConfig, bannersDTFResponseMinLeft);
                    break;
                case BANNER_LEFT_ADVERTISING:
                    setBannersLeftAdvertising(dynamicSectionAux, bannersDTFResponse);
                    break;
                case BANNER_RIGHT_ADVERTISING:
                    setBannersRightAdvertising(dynamicSectionAux, bannersDTFResponse);
                    break;
                case STATIC_BANNER:
                    setStaticBanner(dynamicSectionAux, bannersDTFResponse);
                    break;
                case SMALL_BANNER:
                    setBannersProviderV2(dynamicSectionAux, providerResponse, sectionAlgolia, homeInfoConfig);
                    break;
                case RECT_BANNER:
                    setBrandsGalleryDataV2(dynamicSectionAux, providerResponse, sectionAlgolia, homeInfoConfig);
                    break;
                case TWO_RECT_BANNER:
                    setTwoBrandsGalleryDataV2(dynamicSectionAux, providerResponse, sectionAlgolia, homeInfoConfig);
                    break;
                case SHORTCUT_BANNER:
                    setBrandsBannersProviderV2(dynamicSectionAux, providerResponse, sectionAlgolia, homeInfoConfig);
                    break;
                case GRID_BLOG_LIST:
                    setCategoryProviderItemListV2(dynamicSectionAux, providerResponse, sectionAlgolia, homeInfoConfig);
                    break;
                case ITEM_LIST_TWO_ROWS:
                    setRecommendedProviderItemsV2(dynamicSectionAux, providerResponse, sectionAlgolia, homeInfoConfig);
                    break;
                case VIDEO_LIST:
                    setTutorialsProvidersV2(dynamicSectionAux, providerResponse, sectionAlgolia);
                    break;
                case PHOTOSLURP:
                    setPhotoSlurpData(sectionAlgolia, dynamicSectionAux, homeInfoConfig, null);
                    break;
                case PHOTOSLURPGRID:
                    setPhotoSlurpGridData(sectionAlgolia, dynamicSectionAux, homeInfoConfig,null);
                    break;
                case PRIME_SAVINGS:
                    setPrimeSavingsData(dynamicSectionAux,sectionAlgolia);
                    break;
                case ITEM_SEO:
                    setItemSeoData(sectionAlgolia, dynamicSectionAux);
                    break;
                case ITEM_HTML:
                    setItemHtmlData(sectionAlgolia, dynamicSectionAux);
                    break;

            }
        }
    }

    /**
     * set data from section
     * @param sectionAlgolia -> section
     * @param dynamicSectionAux -> section To return
     */
    private void setItemHtmlData(Component sectionAlgolia, DynamicSection dynamicSectionAux) {

        if (isInvalidHtmlData(sectionAlgolia)) {
            return;
        }

        String htmlData = "";

        htmlData = sectionAlgolia.getHtml();

        dynamicSectionAux.setHtml(htmlData);
    }

    private static boolean isInvalidHtmlData(Component sectionAlgolia) {

        if (sectionAlgolia == null || sectionAlgolia.getComponentType() == null) {
            return true;
        }

        if (!sectionAlgolia.getComponentType().equals(ComponentTypeEnum.ITEM_HTML) ) {
            return true;
        }

        return sectionAlgolia.getHtml() == null || sectionAlgolia.getHtml().isBlank();
    }

    /**
     * set data HTML for SEO
     *
     * @param sectionAlgolia -> section
     * @param dynamicSectionAux -> section To return
     */
    private void setItemSeoData(Component sectionAlgolia, DynamicSection dynamicSectionAux) {

        if (isInvalidItemSeoData(sectionAlgolia)) {
            return;
        }
        String dataSeo = "";
        dataSeo = sectionAlgolia.getHtml();

        dynamicSectionAux.setHtml(dataSeo);
    }

    private static boolean isInvalidItemSeoData(Component sectionAlgolia) {

        if (sectionAlgolia == null || sectionAlgolia.getComponentType() == null) {
            return true;
        }

        if (!sectionAlgolia.getComponentType().equals(ComponentTypeEnum.ITEM_SEO) ) {
            return true;
        }

        return sectionAlgolia.getHtml() == null || sectionAlgolia.getHtml().isBlank();
    }

    private void setPrimeSavingsData(DynamicSection dynamicSectionAux, Component sectionAlgolia) {
        if (Objects.nonNull(dynamicSectionAux)&&Objects.nonNull(sectionAlgolia)) {
            dynamicSectionAux.setPrime(false);
            dynamicSectionAux.setTotal_saved(0.0);
            dynamicSectionAux.setUserType(sectionAlgolia.getUserType());
//            log.info("PRIME_SAVINGS" + dynamicSectionAux);
        }
    }



    private void setBrazeEmptyCarousel(Component sectionAlgolia, DynamicSection dynamicSectionAux) {
//        log.info("set empty setBrazeEmptyCorousel");
        dynamicSectionAux.setList(new ArrayList());
    }

    private void setTutorialsProvidersV2(DynamicSection dynamicSectionAux, ProviderResponse providerResponse, Component sectionAlgolia) {
        if (Objects.nonNull(sectionAlgolia)){

            if (Objects.nonNull(sectionAlgolia.getList())){
                List<ElementProvider> elementProviderList = sectionAlgolia.getList();
                setTutorialsProviders(dynamicSectionAux, elementProviderList);
            }
        }
    }

    private void setRecommendedProviderItemsV2(DynamicSection dynamicSectionAux, ProviderResponse providerResponse, Component sectionAlgolia, HomeInfoConfig homeInfoConfig) {
        if (Objects.nonNull(sectionAlgolia)){

            if (Objects.nonNull(sectionAlgolia.getList())){
                List<ElementProvider> recommendedItems = sectionAlgolia.getList();
                setRecommendedProviderItems(dynamicSectionAux, recommendedItems, homeInfoConfig);
            }
        }
    }

    private void setCategoryProviderItemListV2(DynamicSection dynamicSectionAux, ProviderResponse providerResponse, Component sectionAlgolia, HomeInfoConfig homeInfoConfig) {
        if (Objects.nonNull(sectionAlgolia)){

            if (Objects.nonNull(sectionAlgolia.getList()) && Objects.nonNull(sectionAlgolia.getListMobile())){
                List<ElementProvider> elementProviderList = new ArrayList<>();
                switch (homeInfoConfig.getLandingPagesRequest().getSource()){
                    case ANDROID:
                    case IOS:
                        elementProviderList = sectionAlgolia.getListMobile();
                        setCategoryProviderItemList(dynamicSectionAux, elementProviderList, homeInfoConfig);
                        break;
                    case WEB:
                    case RESPONSIVE:
//                        log.info("GRID_BLOG_LIST size -> " + sectionAlgolia.getList().size());
                        elementProviderList = sectionAlgolia.getList();
                        setCategoryProviderItemList(dynamicSectionAux, elementProviderList, homeInfoConfig);
                        break;
                }
            }
        }
    }

    private void setBrandsBannersProviderV2(DynamicSection dynamicSectionAux, ProviderResponse providerResponse, Component sectionAlgolia, HomeInfoConfig homeInfoConfig) {
        if (Objects.nonNull(sectionAlgolia)){

            if (Objects.nonNull(sectionAlgolia.getList()) && Objects.nonNull(sectionAlgolia.getListMobile())){
                List<ElementProvider> elementProviderList = new ArrayList<>();
                switch (homeInfoConfig.getLandingPagesRequest().getSource()){
                    case ANDROID:
                    case IOS:
                        elementProviderList = sectionAlgolia.getListMobile();
                        setBrandsBannersProvider(dynamicSectionAux, elementProviderList, homeInfoConfig);
                        break;
                    case WEB:
                    case RESPONSIVE:
                        elementProviderList = sectionAlgolia.getList();
                        setBrandsBannersProvider(dynamicSectionAux, elementProviderList, homeInfoConfig);
                        break;

                }
            }
        }
    }

    /**
     * Set TWO_RECT_BANNER of Algolia
     * @param dynamicSectionAux
     * @param providerResponse
     * @param sectionAlgolia
     * @param homeInfoConfig
     */
    private void setTwoBrandsGalleryDataV2(DynamicSection dynamicSectionAux, ProviderResponse providerResponse, Component sectionAlgolia, HomeInfoConfig homeInfoConfig) {
        if (Objects.nonNull(sectionAlgolia)){

            if (Objects.nonNull(sectionAlgolia.getList()) && Objects.nonNull(sectionAlgolia.getListMobile())){
                List<ElementProvider> bannersGallery = new ArrayList<>();
                switch (homeInfoConfig.getLandingPagesRequest().getSource()){
                    case ANDROID:
                    case IOS:
//                        log.info("setBrandsGalleryDataV2 size -> " + sectionAlgolia.getListMobile().size());
                        bannersGallery = sectionAlgolia.getListMobile();
                        setBrandsGalleryData(dynamicSectionAux, bannersGallery, homeInfoConfig);
                        break;
                    case WEB:
                    case RESPONSIVE:
//                        log.info("setBrandsGalleryDataV2 size -> " + sectionAlgolia.getList().size());
                        bannersGallery = sectionAlgolia.getList();
                        setBrandsGalleryData(dynamicSectionAux, bannersGallery, homeInfoConfig);
                        break;

                }
            }
        }
    }

    private void setBrandsGalleryDataV2(DynamicSection dynamicSectionAux, ProviderResponse providerResponse, Component sectionAlgolia, HomeInfoConfig homeInfoConfig) {
        if (Objects.nonNull(sectionAlgolia)){

            if (Objects.nonNull(sectionAlgolia.getList()) && Objects.nonNull(sectionAlgolia.getListMobile())){
                List<ElementProvider> bannersGallery = new ArrayList<>();
                switch (homeInfoConfig.getLandingPagesRequest().getSource()){
                    case ANDROID:
                    case IOS:
//                        log.info("setBrandsGalleryDataV2 size -> " + sectionAlgolia.getListMobile().size());
                        bannersGallery = sectionAlgolia.getListMobile();
                        setBrandsGalleryData(dynamicSectionAux, bannersGallery, homeInfoConfig);
                        break;
                    case WEB:
                    case RESPONSIVE:
//                        log.info("setBrandsGalleryDataV2 size -> " + sectionAlgolia.getList().size());
                        bannersGallery = sectionAlgolia.getList();
                        setBrandsGalleryData(dynamicSectionAux, bannersGallery, homeInfoConfig);
                        break;

                }
            }
        }
    }

    private void setBannersProviderV2(DynamicSection dynamicSectionAux, ProviderResponse providerResponse, Component sectionAlgolia, HomeInfoConfig homeInfoConfig) {
        if (Objects.nonNull(sectionAlgolia)){

            if (Objects.nonNull(sectionAlgolia.getList()) && Objects.nonNull(sectionAlgolia.getListMobile())){
                List<ElementProvider> bannersProvider = new ArrayList<>();
                switch (homeInfoConfig.getLandingPagesRequest().getSource()){
                    case ANDROID:
                    case IOS:
//                        log.info("SMALL_BANNER size -> " + sectionAlgolia.getListMobile().size());
                        bannersProvider = sectionAlgolia.getListMobile();
                        setBannersProviderSections(dynamicSectionAux, bannersProvider, homeInfoConfig);
                        break;
                    case WEB:
                    case RESPONSIVE:
//                        log.info("SMALL_BANNER size -> " + sectionAlgolia.getList().size());
                        bannersProvider = sectionAlgolia.getList();
                        setBannersProviderSections(dynamicSectionAux, bannersProvider, homeInfoConfig);
                        break;

                }
            }
        }
    }

    private List<ElementProvider> setDataList(ProviderSections section, EnableForEnum source) {
        List<ElementProvider> response = null;
        if (Objects.nonNull(section)){

            switch (source){
                case ANDROID:
                case IOS:
                case RESPONSIVE:
                    response = section.getListMobile();
                    return response;

                case WEB:
                    response = section.getList();
                    return response;
            }

        }

        return response;
    }

    private void setTutorialsProviders(DynamicSection dynamicSectionAux, List<ElementProvider> tutorials) {
        List<Element> elementBannerList = new ArrayList<>();
        if (!tutorials.isEmpty()){
            tutorials.forEach(tutorial -> {
                Element elementBannerAux = new Element();
                elementBannerAux.setId(tutorial.getId());
                elementBannerAux.setUrl(tutorial.getUrl());
                elementBannerAux.setOrderingNumber(tutorial.getOrderingNumber());
                elementBannerAux.setTitle(tutorial.getTitle());
                elementBannerAux.setThumbnail(tutorial.getThumbnail());
                elementBannerAux.setProducts(tutorial.getProducts());
                elementBannerAux.setAuthor(tutorial.getAuthor());
//                log.info("setTutorialsProviders -> " + elementBannerAux.toString());
                elementBannerList.add(elementBannerAux);
            });
//            log.info("elementBannerList size -> " + elementBannerList.size());
            dynamicSectionAux.setList(elementBannerList);
        }
    }

    private void setRecommendedProviderItems(DynamicSection dynamicSectionAux, List<ElementProvider> recommendedItems, HomeInfoConfig homeInfoConfig) {
        if (!recommendedItems.isEmpty()){
            List<Element> finalelementList = new ArrayList<>();
            final int store = homeInfoConfig.getLandingPagesRequest().getIdStoreGroup();
//            log.info("Items recomendados -> " + recommendedItems.size());
            List<ItemQuery> itemQueryList = recommendedItems.stream().map(item -> new ItemQuery(item.getItem() + store)).collect(Collectors.toList());
            List<ItemAlgolia> itemAlgoliaList = APIAlgolia.findItemByIdList(itemQueryList);

            List<Item> items = new ArrayList<>();

            if (Objects.nonNull(itemAlgoliaList) && !itemAlgoliaList.isEmpty()) {
                items = itemAlgoliaList.stream().filter(itemAlgolia -> Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getId()) && !itemAlgolia.getId().isEmpty())
                        .map(itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia)).collect(Collectors.toList());
            }

            if (!items.isEmpty()) {
                items.forEach(recommended -> {

//                    log.info("Item recomendado -> " + recommended.getMediaDescription());
//                    log.info("Item id -> " + recommended.getId());

                    Element elementAux = new Element();
                    List<Item> itemTempList = new ArrayList<>();
                    itemTempList.add(recommended);
                    if (recommended.getId() > 0) elementAux.setId(String.valueOf(recommended.getId()));
                    elementAux.setProduct(itemTempList);
                    elementAux.setType(ProductTypeEnum.UNIQUE);
                    finalelementList.add(elementAux);

                });
            }
//            log.info(new Gson().toJson(items));
            if (finalelementList.size() > 0) {
//                log.info("elementListRes size -> " + finalelementList.size());
                dynamicSectionAux.setList(finalelementList);
            }else {
                dynamicSectionAux.setComponentType(null);
            }
        }
    }

    private void setCategoryProviderItemList(DynamicSection dynamicSectionAux, List<ElementProvider> categoryList, HomeInfoConfig homeInfoConfig) {
        List<Element> elementBannerList = new ArrayList<>();
        if (!categoryList.isEmpty()){
            categoryList.forEach(banner -> {
                final String redirectURL = setRedirectUrl(banner.getRedirectUrl(), homeInfoConfig.getLandingPagesRequest().getSource());
                Element elementBannerAux = new Element();
                elementBannerAux.setId(banner.getId());
                elementBannerAux.setUrlBanner(banner.getUrlBanner());
                elementBannerAux.setRedirectURL(redirectURL);
                elementBannerAux.setFirstDescription(banner.getFirstDescription());
                elementBannerAux.setOrderingNumber(banner.getOrderingNumber());
                elementBannerAux.setLabel(banner.getLabel());
                elementBannerAux.setDescription(banner.getDescription());
//                log.info("setCategoryProviderItemList -> " + elementBannerAux.toString());
                elementBannerList.add(elementBannerAux);
            });
//            log.info("elementBannerList size -> " + elementBannerList.size());
            dynamicSectionAux.setList(elementBannerList);
        }
    }

    private void setBrandsBannersProvider(DynamicSection dynamicSectionAux, List<ElementProvider> banners, HomeInfoConfig homeInfoConfig) {
        List<Element> elementBannerList = new ArrayList<>();
        if (!banners.isEmpty()){
            banners.forEach(banner -> {
                Element elementBannerAux = new Element();
                final String redirectURL = setRedirectUrl(banner.getRedirectUrl(), homeInfoConfig.getLandingPagesRequest().getSource());
                elementBannerAux.setId(banner.getId());
                elementBannerAux.setUrlBanner(banner.getUrlBanner());
                elementBannerAux.setRedirectURL(redirectURL);
                elementBannerAux.setFirstDescription(banner.getFirstDescription());
                elementBannerAux.setOrderingNumber(banner.getOrderingNumber());
//                log.info("setBrandsBannersProvider -> " + elementBannerAux.toString());
                elementBannerList.add(elementBannerAux);
            });
//            log.info("elementBannerList size -> " + elementBannerList.size());
            dynamicSectionAux.setList(elementBannerList);
        }
    }


    private void setBrandsGalleryData(DynamicSection dynamicSectionAux, List<ElementProvider> banners, HomeInfoConfig homeInfoConfig) {
        List<Element> elementBannerList = new ArrayList<>();
        if (!banners.isEmpty()){
            banners.forEach(banner -> {
                Element elementBannerAux = new Element();
                final String redirectURL = setRedirectUrl(banner.getRedirectUrl(), homeInfoConfig.getLandingPagesRequest().getSource());
                elementBannerAux.setId(banner.getId());
                elementBannerAux.setUrlBanner(banner.getUrlBanner());
                elementBannerAux.setRedirectURL(redirectURL);
                elementBannerAux.setFirstDescription(banner.getFirstDescription());
                elementBannerAux.setOrderingNumber(banner.getOrderingNumber());
//                log.info("setBrandsGalleryData -> " + elementBannerAux.toString());
                elementBannerList.add(elementBannerAux);
            });
//            log.info("elementBannerList size -> " + elementBannerList.size());
            dynamicSectionAux.setList(elementBannerList);
        }
    }

    private void setBannersProviderSections(DynamicSection dynamicSectionAux, List<ElementProvider> banners, HomeInfoConfig homeInfoConfig) {
        List<Element> elementBannerList = new ArrayList<>();
        if (!banners.isEmpty()){

            banners.forEach(banner -> {
                Element elementBannerAux = new Element();
                final String redirectURL = setRedirectUrl(banner.getRedirectUrl(), homeInfoConfig.getLandingPagesRequest().getSource());
                elementBannerAux.setId(banner.getId());
                elementBannerAux.setUrlBanner(banner.getUrlBanner());
                elementBannerAux.setRedirectURL(redirectURL);
                elementBannerAux.setFirstDescription(banner.getFirstDescription());
                elementBannerAux.setOrderingNumber(banner.getOrderingNumber());
//                log.info("setBannersProviderSections -> " + elementBannerAux.toString());
                elementBannerList.add(elementBannerAux);
            });
//            log.info("elementBannerList size -> " + elementBannerList.size());
            dynamicSectionAux.setList(elementBannerList);
        }
    }

    private String setRedirectUrl(String redirectUrl, EnableForEnum source) {
//        log.info("Validando el source para la url");
        switch (source){
            case RESPONSIVE:
            case WEB:
//                log.info("WEB y RESPONSIVE");
                String response = redirectUrl.replace("https://www.farmatodo.com.co","");
//                log.info("URL -> " + response);
                return response;
            case IOS:
            case ANDROID:
//                log.info("IOS y ANDROID");
                return redirectUrl;
        }
        return redirectUrl;
    }

    private void setStaticBanner(DynamicSection dynamicSectionAux, BannersDTFRes bannersDTFResponse) {
        if (dynamicSectionAux != null) {
            // set banners

            if (bannersDTFResponse.getData().getDesktop() != null && bannersDTFResponse.getData().getDesktop().getStaticBanner() != null) {
                if (Objects.nonNull(bannersDTFResponse.getData().getDesktop().getStaticBanner())) {
                    List<BannerCMSData> desktopBanners = bannersDTFResponse.getData().getDesktop().getStaticBanner();
/*                    desktopBanners.forEach(bannerCMSData -> {
                        log.info("banner static -> " + bannerCMSData.getCampaignName());
                    });*/

                    if (!desktopBanners.isEmpty()) {
                        setMainBanners(dynamicSectionAux, desktopBanners);
                    }
                }
            }


        }
    }

    /**
     * return banners right advertising
     *
     * @param dynamicSectionAux
     * @param bannersDTFResponse
     */
    private void setBannersRightAdvertising(DynamicSection dynamicSectionAux, BannersDTFRes bannersDTFResponse) {
        if (dynamicSectionAux != null) {
            // set banners
            if (bannersDTFResponse.getData() != null && bannersDTFResponse.getData().getDesktop() != null && bannersDTFResponse.getData().getDesktop().getRightAdvertising() != null) {
                List<BannerCMSData> desktopBanners = bannersDTFResponse.getData().getDesktop().getRightAdvertising();
/*                desktopBanners.forEach(bannerCMSData -> {
                    log.info("banner rigth -> " + bannerCMSData.getCampaignName());
                });*/

                if (!desktopBanners.isEmpty()) {
                    setMainBanners(dynamicSectionAux, desktopBanners);
                }
            }
        }
    }


    /**
     * return banners left adevertising
     *
     * @param dynamicSectionAux
     * @param bannersDTFResponse
     */
    private void setBannersLeftAdvertising(DynamicSection dynamicSectionAux, BannersDTFRes bannersDTFResponse) {
        if (dynamicSectionAux != null) {
            // set banners
            if (bannersDTFResponse.getData() != null && bannersDTFResponse.getData().getDesktop() != null && bannersDTFResponse.getData().getDesktop().getLeftAdvertising() != null) {
                List<BannerCMSData> desktopBanners = bannersDTFResponse.getData().getDesktop().getLeftAdvertising();

                if (!desktopBanners.isEmpty()) {
                    setMainBanners(dynamicSectionAux, desktopBanners);
                }
            }
        }
    }

    /**
     * set square banner
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     */
    private void setSquareBanners(Component sectionAlgolia, DynamicSection dynamicSectionAux) {


        if (sectionAlgolia.getDataFrom() != null
                && sectionAlgolia.getDataFrom().getListData() != null
                && !sectionAlgolia.getDataFrom().getListData().isEmpty()) {

//            log.info("square Banners");
            setDataFromConfig(sectionAlgolia, dynamicSectionAux);
        }

    }


    /**
     * set data from algolia config
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     */
    private void setDataFromConfig(Component sectionAlgolia, DynamicSection dynamicSectionAux) {
        List<Element> elementList = new ArrayList<>();
        sectionAlgolia.getDataFrom().getListData().forEach(info -> {

            Element elementAux = new Element();
            elementAux.setLabel(info.getLabel());
            elementAux.setUrlBanner(info.getUrlBanner());
            elementAux.setRedirectURL(info.getRedirectUrl());
            elementAux.setRedirectUrlSub(info.getRedirectUrlSub());
            elementList.add(elementAux);


        });

        if (!elementList.isEmpty()) {
            dynamicSectionAux.setList(elementList);
        }
    }

    /**
     * setCircleBanner
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     */
    private void setCircleBanners(Component sectionAlgolia, DynamicSection dynamicSectionAux) {


        if (sectionAlgolia.getDataFrom() != null
                && sectionAlgolia.getDataFrom().getListData() != null
                && !sectionAlgolia.getDataFrom().getListData().isEmpty()) {

//            log.info("Circle Banners");
            setDataFromConfig(sectionAlgolia, dynamicSectionAux);
        }
    }

    /**
     * setLabelHtml
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     * @param providerResponse
     */
    private void setLabelHtml(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig, ProviderResponse providerResponse) {
        setMostSalesLabelPersonalization(sectionAlgolia, dynamicSectionAux, homeInfoConfig);

        if (sectionAlgolia.getDataFrom() != null && sectionAlgolia.getDataFrom().getFrom() != null) {
            switch (sectionAlgolia.getDataFrom().getFrom()) {
                case USER_NAME:
                    setUserNameInLabel(sectionAlgolia, dynamicSectionAux, homeInfoConfig);
                    break;
                case PREVIOUS_ITEMS:
                    setLabelPreviousItems(sectionAlgolia, dynamicSectionAux, homeInfoConfig);
                    break;
                case FAVORITES:
                    setLabelFavorites(sectionAlgolia, dynamicSectionAux, homeInfoConfig);
                    break;
                case VIDEO_LIST:
                    setLabelTutorials(sectionAlgolia, dynamicSectionAux, homeInfoConfig, providerResponse);
                    break;
                case ITEM_LIST_TWO_ROWS:
                    setLabelsItemsRecomended(sectionAlgolia, dynamicSectionAux, homeInfoConfig, providerResponse);
                    break;
                case GRID_BLOG_LIST:
                    setLabelCategories(sectionAlgolia, dynamicSectionAux, homeInfoConfig, providerResponse);
                    break;
                case RECENTLY_VIEWED:
                    //log.info("getFrom -> " + sectionAlgolia.getDataFrom().getFrom());
                    setLabelRecentlyViewed(sectionAlgolia, dynamicSectionAux, homeInfoConfig);
                    break;
            }

        }
    }

    /**
     * set label recently viewed items
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     */
    private void setLabelRecentlyViewed(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {
        try {
            CustomerOnlyData customerOnlyData = homeInfoConfig.getCustomerOnlyData();
            if (customerOnlyData != null && customerOnlyData.getId() > 0 && Objects.nonNull(homeInfoConfig.getIdStoreGroup()) ) {

                Optional<HistoryUser> optionalHistoryUser = APIAlgolia.getHistoryByUserId(customerOnlyData.getId().toString());
                //log.info("data history by user" +  optionalHistoryUser);

                if (optionalHistoryUser.isPresent() && Objects.requireNonNull(optionalHistoryUser.get().getItems()).size() > 0) {

                    HistoryUser historyUser = optionalHistoryUser.get();

                    if (sectionAlgolia.getLabel() != null && !historyUser.getItems().isEmpty()) {
                        //log.info("method setLabelRencentlyViewed customerOnlyData ->" + customerOnlyData.toString());
                        String labelOld = sectionAlgolia.getLabel();
                        String newLabel = FTDUtil.replaceStringVar(labelOld, "user_name", "");

                        if (customerOnlyData.getId() > 0) {
                            newLabel = FTDUtil.replaceStringVar(labelOld, "user_name", customerOnlyData.getFirstName());
                        }
                        List<ItemAlgolia> existItems= new ArrayList<>();
//                        historyUser.getItems().forEach(
//                                items -> {
//                                    String codeItemsForStore=String.valueOf(items)+String.valueOf(homeInfoConfig.getIdStoreGroup());
//                                    //log.info("5325-->codeItemsForStore"+codeItemsForStore);
//                                    ItemAlgolia existItemsAlgolia=  APIAlgolia.getItemAlgolia(codeItemsForStore);
//                                    //log.info("5328-->existItemsAlgolia!=null"+(existItemsAlgolia!=null));
//                                    if(existItemsAlgolia!=null && validateItems(existItemsAlgolia)) {
//                                        //log.info("5330-->validateItems(existItemsAlgolia)" + validateItems(existItemsAlgolia));
//                                        existItems.add(existItemsAlgolia);
//                                    }
//                                }
//                        );
//                        existItems = APIAlgolia.getItemListAlgoliaFromStringList(historyUser.getItems().stream().map(item ->  String.valueOf(item)+String.valueOf(homeInfoConfig.getIdStoreGroup())).collect(Collectors.toList()));
                        existItems =homeInfoConfig.getCarrouselItemListAsync().getViewed();
                        //log.info("5334-->validation it's no is null"+(existItems!=null && !existItems.isEmpty()));
                        if(existItems!=null && !existItems.isEmpty()){
                            dynamicSectionAux.setLabel(newLabel);
                            dynamicSectionAux.setLabelWeb(newLabel);
                        }else{
                            dynamicSectionAux.setComponentType(null);
                            dynamicSectionAux.setLabel(null);
                            dynamicSectionAux.setLabelWeb(null);
                        }

                    }
                } else {
                    dynamicSectionAux.setComponentType(null);
                }
            } else {
                dynamicSectionAux.setComponentType(null);
                dynamicSectionAux.setLabel(null);
                dynamicSectionAux.setLabelWeb(null);
            }
        }catch (Exception e) {
            log.warning("No se pudo setear el label de RECENTLY_VIEWED error --> " + e.getMessage());
        }

    }

    /***
     * validation items
     * @param existItemsAlgolia
     * @return true or false
     */
    private boolean validateItems(ItemAlgolia existItemsAlgolia){
        //log.info("5360-->validateItems"+existItemsAlgolia);
        if(existItemsAlgolia==null){
            return false;
        }
        //log.info("5366-->it's not null");
        if(existItemsAlgolia.getBarcode()!=null && existItemsAlgolia.getFullPrice()!=null && existItemsAlgolia.getDescription()!=null
                && (existItemsAlgolia.getStock()!=null && existItemsAlgolia.getStock()>0)){
            return true;
        }
        return false;

    }

    /**
     * set label categories
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     * @param providerResponse
     */
    private void setLabelCategories(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig, ProviderResponse providerResponse) {
        if (Objects.nonNull(sectionAlgolia)){

            if (Objects.nonNull(sectionAlgolia.getLabel())){
                dynamicSectionAux.setLabel(sectionAlgolia.getLabel());
            }else {
                dynamicSectionAux.setLabel(null);
                dynamicSectionAux.setComponentType(null);
            }
        }
    }

    /**
     * set label Items recomended
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     * @param providerResponse
     */
    private void setLabelsItemsRecomended(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig, ProviderResponse providerResponse) {
        if (Objects.nonNull(sectionAlgolia)){

            if (Objects.nonNull(sectionAlgolia.getLabel())){
                dynamicSectionAux.setLabel(sectionAlgolia.getLabel());
            }else {
                dynamicSectionAux.setLabel(null);
                dynamicSectionAux.setComponentType(null);
            }
        }
    }

    /**
     * set label tutorials
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     * @param providerResponse
     */
    private void setLabelTutorials(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig, ProviderResponse providerResponse) {
        if (Objects.nonNull(sectionAlgolia)){

            if (Objects.nonNull(sectionAlgolia.getLabel())){
                dynamicSectionAux.setLabel(sectionAlgolia.getLabel());
            }else {
                dynamicSectionAux.setLabel(null);
                dynamicSectionAux.setComponentType(null);
            }
        }
    }

    private void setLabelFavorites(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {

        try {
            List<Favorite> favorites = null;
            CustomerOnlyData customerOnlyData = homeInfoConfig.getCustomerOnlyData();

            Key<Customer> customerKey = Key.create(homeInfoConfig.getHomeRequest().getIdCustomerWebSafe());
            favorites = ofy().load().type(Favorite.class).filter("customerKey", customerKey).list();

            if (customerOnlyData != null && favorites != null && !favorites.isEmpty()){
//                log.info("favoritos size suafazon-> " +  favorites.size());
                customerOnlyData.setFavorites(favorites);
            }else {
                customerOnlyData.setFavorites(null);
                dynamicSectionAux.setComponentType(null);
            }

            if (sectionAlgolia.getDataFrom() != null
                    && sectionAlgolia.getDataFrom().getFrom() != null
                    && customerOnlyData != null  && customerOnlyData.getFavorites() != null
                    && !customerOnlyData.getFavorites().isEmpty()) {

                if (sectionAlgolia.getLabel() != null) {
//                    log.info("method setLabelFavorites customerOnlyData ->" + customerOnlyData.toString());
                    String labelOld = sectionAlgolia.getLabel();
                    String newLabel = FTDUtil.replaceStringVar(labelOld, "user_name", "");
                    if (customerOnlyData.getId() > 0) {
                        newLabel = FTDUtil.replaceStringVar(labelOld, "user_name", customerOnlyData.getFirstName());
                    }
                    dynamicSectionAux.setLabel(newLabel);
                    dynamicSectionAux.setLabelWeb(newLabel);
                }

            }

        }catch (Exception e){
            log.severe("Error label favorites -> " + e.getMessage());
        }

    }

    private void setLabelPreviousItems(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {

        if (homeInfoConfig.getCustomerOnlyData() == null
                || homeInfoConfig.getCustomerOnlyData().getPurchases() == null
                || homeInfoConfig.getCustomerOnlyData().getPurchases().isEmpty()) {
            if (Objects.nonNull(homeInfoConfig.getCustomerOnlyData()) && homeInfoConfig.getCustomerOnlyData().getId() <= 0) {
                dynamicSectionAux.setComponentType(null);
            }
        }
    }

    /**
     * setUserNameInLabel
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     */
    private void setUserNameInLabel(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {

        CustomerOnlyData customerOnlyData = homeInfoConfig.getCustomerOnlyData();

        if (sectionAlgolia.getDataFrom() != null && sectionAlgolia.getDataFrom().getFrom() != null && customerOnlyData != null) {

            if (sectionAlgolia.getLabel() != null) {
//                log.info("method setUserNameInLabel customerOnlyData ->" + customerOnlyData.toString());
                String labelOld = sectionAlgolia.getLabel();
                String newLabel = FTDUtil.replaceStringVar(labelOld, "user_name", "");
                if (customerOnlyData.getId() > 0) {
                    newLabel = FTDUtil.replaceStringVar(labelOld, "user_name", customerOnlyData.getFirstName());
                }
                dynamicSectionAux.setLabel(newLabel);
                dynamicSectionAux.setLabelWeb(newLabel);
            }

        }
    }


    /**
     * set items list in section.
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     */
    private void setItemListData(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {

        // log.info("method() setItemListInGrid component section data from -> " + sectionAlgolia.getDataFrom().toString());
        try {
            if (sectionAlgolia.getDataFrom() != null && sectionAlgolia.getDataFrom().getFrom() != null) {

                switch (sectionAlgolia.getDataFrom().getFrom()) {
                    case SUGGESTS:
                        setSuggestItems(dynamicSectionAux, homeInfoConfig);
                        break;
                    case PREVIOUS_ITEMS:
                        setPreviousItems(dynamicSectionAux, homeInfoConfig);
                        break;
                    case HIGHLIGHTS:
                        setHighlightItems(dynamicSectionAux, homeInfoConfig);
                        break;
                    case MOST_SALES:
                        setMostSalesByDept(dynamicSectionAux, homeInfoConfig, sectionAlgolia);
                        break;
                    case FAVORITES:
//                        log.info("Seteando los favoritos...");
                        setFavoritesItems(dynamicSectionAux, homeInfoConfig);
                        break;
                    case RECENTLY_VIEWED:
//                        log.info("Seteando los productos mas vistos...");
                        setRecentlyViewed(dynamicSectionAux, homeInfoConfig);
                        break;
                    case FLASH_OFFERS_ITEMS:
                        setFlashOfferItems(dynamicSectionAux);
                        break;
                }

            }

        } catch (Exception e) {
            log.warning("Error no idStoreGroup From algolia");
            e.printStackTrace();
        }


    }


    /**
     * set recentry viewed items by customer
     * @param dynamicSectionAux
     * @param homeInfoConfig
     */
    private void setRecentlyViewed(DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {
        CustomerOnlyData customer = homeInfoConfig.getCustomerOnlyData();
        if (customer != null && customer.getId() > 0 ) {

            if(!homeInfoConfig.getCarrouselItemListAsync().getViewed().isEmpty()) {
//                log.info("Evitar llamado extra a vistos recientemente");
                List<ItemAlgolia> itemAlgoliaList = homeInfoConfig.getCarrouselItemListAsync().getViewed();
                List<Long> idsLastPurchases = customer.getPurchases().stream().map(p -> p.getItem()).collect(Collectors.toList());
                itemAlgoliaList = itemAlgoliaList.stream().filter(ia -> !idsLastPurchases.contains(Long.valueOf(ia.getId()))).collect(Collectors.toList());
                List<Item> items = new ArrayList<>();
                List<Element> elementListRes = new ArrayList<>();
                List<Element> finalElementList = new ArrayList<>();
                if (Objects.nonNull(itemAlgoliaList) && !itemAlgoliaList.isEmpty()) {
                    items = itemAlgoliaList.stream().filter(itemAlgolia -> Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getId()) && !itemAlgolia.getId().isEmpty())
                            .map(itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia)).collect(Collectors.toList());
                }
                List<Long> historyUser = itemAlgoliaList.stream().map(i -> Long.valueOf(i.getId())).collect(Collectors.toList());
                if (!items.isEmpty()) {
                    items.sort(Comparator.comparing(i -> historyUser.indexOf(i.getId())));
                    items.forEach(productAux -> {
                        Element elementAux = new Element();
                        List<Item> itemTempList = new ArrayList<>();
                        itemTempList.add(productAux);
                        if (productAux.getId() > 0) elementAux.setId(String.valueOf(productAux.getId()));
                        elementAux.setProduct(itemTempList);
                        elementAux.setType(ProductTypeEnum.UNIQUE);
                        elementAux.setAction(ActionEnum.RECENTLY_VIEWED);
                        finalElementList.add(elementAux);
                    });
                }


                if (finalElementList.size() > 0) {
                    elementListRes = finalElementList.stream().limit(Constants.RECENTLY_VIEWED_CAROUSEL_LIMIT).collect(Collectors.toList());
//                    log.info("Seteando items RECENTLY_VIEWED total -> " + elementListRes.size());
//                    log.info("Seteando items RECENTLY_VIEWED total -> " + elementListRes.toString());
                    dynamicSectionAux.setList(elementListRes);
                } else {
                    dynamicSectionAux.setComponentType(null);
                }
            } else {
                Optional<HistoryUser> optionalHistoryUser = APIAlgolia.getHistoryByUserId(customer.getId().toString());
                CustomerOnlyData customerOnlyData = homeInfoConfig.getCustomerOnlyData();
                List<Item> itemList = new ArrayList<>();

                if (customerOnlyData != null && homeInfoConfig.getIdStoreGroup() != null && Objects.nonNull(customer.getPurchases())) {
                    try {
                        itemList = productsMethods.getItemsByIds(customer.getPurchases(), homeInfoConfig.getIdStoreGroup());
//                    log.info("Ultimas compras -> " + itemList.size());
                    } catch (Exception e) {
                        log.warning("Ocurrio un error en las ultimas compras ERROR -> " + e.getMessage());
                    }
                }

                if (optionalHistoryUser.isPresent()) {

                    if (!itemList.isEmpty()) {
                        for (Item historyUser : itemList) {

                            try {
                                Objects.requireNonNull(optionalHistoryUser.get().getItems()).removeIf(hit -> hit.equals(historyUser.getId()));
//                            log.info("Try getHistoryByUserId -> user: " + optionalHistoryUser);
                            } catch (Exception e) {
                                log.warning("Error controlado. -> " + e.getMessage());
                            }
                        }
                    }

                    HistoryUser historyUser = optionalHistoryUser.get();
//                log.info("Ultimos vistos final -> " + historyUser.getItems().size());
                    List<Item> items = new ArrayList<>();
                    List<Element> elementListRes = new ArrayList<>();
                    List<Element> finalElementList = new ArrayList<>();

                    if (historyUser.getItems() == null || historyUser.getItems().isEmpty()) {
                        dynamicSectionAux.setComponentType(null);
                    } else {
                        List<ItemQuery> itemQueryList = historyUser.getItems().stream().map(item -> new ItemQuery(item + (URLConnections.MAIN_ID_STORE))).collect(Collectors.toList());

                        List<ItemAlgolia> itemAlgoliaList = APIAlgolia.findItemByIdList(itemQueryList);

                        if (Objects.nonNull(itemAlgoliaList) && !itemAlgoliaList.isEmpty()) {
                            items = itemAlgoliaList.stream().filter(itemAlgolia -> Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getId()) && !itemAlgolia.getId().isEmpty())
                                    .map(itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia)).collect(Collectors.toList());
                        }

                        if (!items.isEmpty()) {
                            items.sort(Comparator.comparing(i -> historyUser.getItems().indexOf(i.getId())));
                            items.forEach(productAux -> {
                                Element elementAux = new Element();
                                List<Item> itemTempList = new ArrayList<>();
                                itemTempList.add(productAux);
                                if (productAux.getId() > 0) elementAux.setId(String.valueOf(productAux.getId()));
                                elementAux.setProduct(itemTempList);
                                elementAux.setType(ProductTypeEnum.UNIQUE);
                                elementAux.setAction(ActionEnum.RECENTLY_VIEWED);
                                finalElementList.add(elementAux);
                            });
                        }
                    }


                    if (finalElementList.size() > 0) {
                        elementListRes = finalElementList.stream().limit(Constants.RECENTLY_VIEWED_CAROUSEL_LIMIT).collect(Collectors.toList());
//                    log.info("Seteando items RECENTLY_VIEWED total -> " + elementListRes.size());
//                    log.info("Seteando items RECENTLY_VIEWED total -> " + elementListRes.toString());
                        dynamicSectionAux.setList(elementListRes);
                    } else {
                        dynamicSectionAux.setComponentType(null);
                    }
                }
            }
        }
    }

    private void setFavoritesItems(DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {

        if (homeInfoConfig.getHomeRequest() != null
                && homeInfoConfig.getHomeRequest().getIdCustomerWebSafe() != null
                && homeInfoConfig.getCustomerOnlyData() != null) {

            List<Favorite> favorites = homeInfoConfig.getCustomerOnlyData().getFavorites();
            if (favorites == null){
                Key<Customer> customerKey = Key.create(homeInfoConfig.getHomeRequest().getIdCustomerWebSafe());
                favorites = ofy().load().type(Favorite.class).filter("customerKey", customerKey).list();
            }


            if (favorites != null && !favorites.isEmpty()) {

                List<Element> finalElementList = new ArrayList<>();
                List<Element> elementListRes = new ArrayList<>();

                if(!homeInfoConfig.getCarrouselItemListAsync().getFavorites().isEmpty()) {
                    log.info("Favoritos Ahorrados");
                }

//                List<ItemQuery> itemQueryList = favorites.stream().map(item -> new ItemQuery(item.getItemId() + (URLConnections.MAIN_ID_STORE))).collect(Collectors.toList());
//                List<ItemAlgolia> itemAlgoliaList = APIAlgolia.findItemByIdList(itemQueryList);
                List<ItemAlgolia> itemAlgoliaList = homeInfoConfig.getCarrouselItemListAsync().getFavorites().isEmpty() ?
                        APIAlgolia.getItemListAlgoliaFromStringList(favorites.stream().map(item -> String.valueOf(item.getItemId()) + (URLConnections.MAIN_ID_STORE)).collect(Collectors.toList())) :
                        homeInfoConfig.getCarrouselItemListAsync().getFavorites();
                Comparator<ItemAlgolia> comparator = nullsLast((ItemAlgolia item, ItemAlgolia itemD) -> itemD.getSales().compareTo(item.getSales()));
                Predicate<ItemAlgolia> predicate = e -> e.getSales() != null;
                itemAlgoliaList =  itemAlgoliaList.stream()
                        .filter(predicate)
                        .sorted(comparator)
                        .collect(Collectors.toList());
                List<Item> items = new ArrayList<>();

                if (Objects.nonNull(itemAlgoliaList) && !itemAlgoliaList.isEmpty()) {
                    items = itemAlgoliaList.stream().filter(itemAlgolia -> Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getId()) && !itemAlgolia.getId().isEmpty())
                            .map(itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia))
                            .filter(i -> Objects.nonNull(i)).collect(Collectors.toList());
                }

                if (!items.isEmpty()) {
                    items.forEach(productAux -> {
                        Element elementAux = new Element();
                        List<Item> itemTempList = new ArrayList<>();
                        itemTempList.add(productAux);
                        if (productAux.getId() > 0) elementAux.setId(String.valueOf(productAux.getId()));
                        elementAux.setProduct(itemTempList);
                        elementAux.setType(ProductTypeEnum.UNIQUE);
                        elementAux.setAction(ActionEnum.FAVORITE);
                        finalElementList.add(elementAux);
                    });
                }

                if (homeInfoConfig.getHomeRequest() != null && homeInfoConfig.getHomeRequest().getCarouselLimit() != null
                        && homeInfoConfig.getHomeRequest().getCarouselLimit() > 0) {

                    int limitList = homeInfoConfig.getHomeRequest().getCarouselLimit();

                    elementListRes = finalElementList.stream().limit(limitList).collect(Collectors.toList());

                }else {
                    elementListRes = finalElementList;
                }

                if (elementListRes.size() > 0) {
//                    log.info("Seteando items favoritos total -> " + elementListRes.size());
                    dynamicSectionAux.setList(elementListRes);
                }else {
                    dynamicSectionAux.setComponentType(null);
                }

            }
        }
    }

    /**
     * most sales by depto
     *
     * @param dynamicSectionAux
     * @param homeInfoConfig
     * @param sectionAlgolia
     */
    private void setMostSalesByDept(DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig, Component sectionAlgolia) throws ConflictException, IOException {


        if (sectionAlgolia != null
                && dynamicSectionAux != null
                && homeInfoConfig.getIdStoreGroup() != null
                && homeInfoConfig.getIdStoreGroup() > 0
                && sectionAlgolia.getDataFrom() != null
                && sectionAlgolia.getDataFrom().getId() != null) {

            long departmentId = Long.parseLong(sectionAlgolia.getDataFrom().getId());

            Department department = ofy()
                    .load()
                    .type(Department.class)
                    .filter("id", departmentId)
                    .first()
                    .now();

            List<Item> itemsMostSalesList = new ArrayList<>();

            if (department != null) {
//                log.info("department -> " + department.toString());

                ItemMostSales itemMostSales = ofy().load().type(ItemMostSales.class).ancestor(department).first().now();
                if (itemMostSales != null && itemMostSales.getSuggested() != null) {
                    Optional<RecommendConfig> config = APIAlgolia.getAlgoliaRecommendConfig();
                    String keySuggested = itemMostSales.getSuggested().stream().map(c -> String.valueOf(c.getItem())).collect(Collectors.joining(",")).concat(","+String.valueOf(homeInfoConfig.getIdStoreGroup()));
                    if(config.isPresent() && config.get().isDepartmentsCarrousel()) {
                        String departmentName = department.getName();
                        if(config.get().isDepartmentsAfinity()  && Objects.nonNull(homeInfoConfig.getCustomerOnlyData()) && Objects.nonNull(homeInfoConfig.getCustomerOnlyData().getAnalyticsUUID()) ) {
//                            log.info("department ->  input" + departmentName);
                            departmentName = algoliaRecommendManager.getFavoriteDepartmentByLabel(homeInfoConfig.getCustomerOnlyData().getAnalyticsUUID(), department.getName());
//                            log.info("department ->  output" + departmentName);
                        }

                        String cachedJson = CachedDataManager.algoliaGetItemListMostSales(departmentName + "-recommend");
                        if(cachedJson == null) {

                            RecommendResponse response = ApiGatewayService.get().getTrendingItemsByDepartment(departmentName);
                            itemsMostSalesList = response.getResults().get(0).getHits().stream().map(r -> APIAlgolia.getItemToItemAlgolia(new Item(), r)).collect(Collectors.toList());
                            itemsMostSalesList= itemsMostSalesList.stream().filter(i -> i.getTotalStock() != 0).collect(Collectors.toList());
                            CachedDataManager.algoliaSetItemListMostSales(departmentName + "-recommend", new Gson().toJson(itemsMostSalesList));

                        } else {
//                            log.info("Item list cacheada - recommend");
                            //use store if is null or empty use default 26

                            itemsMostSalesList = Arrays.asList(new Gson().fromJson(cachedJson, Item[].class));
//                            itemsMostSalesList = APIAlgolia.getItemListAlgoliaFromStringList(itemsMostSalesList.stream().map(i -> String.valueOf(i.getId()).concat("26")).collect(Collectors.toList())).stream().map(r -> APIAlgolia.getItemToItemAlgolia(new Item(), r)).collect(Collectors.toList());

                        }
                    } else {
                        String cachedJson = CachedDataManager.algoliaGetItemListMostSales(keySuggested);
                        if(cachedJson == null) {
                            //log.info("Item list no cacheada");
                            itemsMostSalesList = productsMethods.getItemsBySuggestedIdsAndStoreGroup(
                                    itemMostSales.getSuggested(),
                                    homeInfoConfig.getIdStoreGroup()
                            );
                            itemsMostSalesList= itemsMostSalesList.stream().filter(i -> i.getTotalStock() != 0).collect(Collectors.toList());
                            CachedDataManager.algoliaSetItemListMostSales(keySuggested, new Gson().toJson(itemsMostSalesList));
                        } else {
//                            log.info("Item list cacheada");
                            String storeGroup = homeInfoConfig.getIdStoreGroup() != null ? String.valueOf(homeInfoConfig.getIdStoreGroup()) : "26";
//                            log.info("storeGroup -> " + storeGroup);
                            itemsMostSalesList = Arrays.asList(new Gson().fromJson(cachedJson, Item[].class));
                            itemsMostSalesList = APIAlgolia.getItemListAlgoliaFromStringList(itemsMostSalesList.stream().map(i -> String.valueOf(i.getId()).concat(storeGroup)).collect(Collectors.toList())).stream().map(r -> APIAlgolia.getItemToItemAlgolia(new Item(), r)).collect(Collectors.toList());

                        }
                    }

                }
                List<Element> elementList = new ArrayList<>();
/*                Element elementAux = new Element();
                elementAux.setProduct(itemsMostSalesList);
                elementList.add(elementAux);*/
                List<Element> finalElementList = elementList;
                itemsMostSalesList.forEach(productAux -> {
//            log.info("small_item_list item -> " + productAux.getId());
                    Element elementAux = new Element();
                    List<Item> itemTempList = new ArrayList<>();
                    itemTempList.add(productAux);
                    if (productAux.getId() > 0) elementAux.setId(String.valueOf(productAux.getId()));
                    elementAux.setProduct(itemTempList);
                    elementAux.setType(ProductTypeEnum.UNIQUE);
                    finalElementList.add(elementAux);
                });

//                homeSectionAux.setAction(ActionEnum.HIGHLIGHTED);
                if (homeInfoConfig.getHomeRequest() != null && homeInfoConfig.getHomeRequest().getCarouselLimit() != null
                        && homeInfoConfig.getHomeRequest().getCarouselLimit() > 0) {

                    int limitList = homeInfoConfig.getHomeRequest().getCarouselLimit();

                    elementList = elementList.stream().limit(limitList).collect(Collectors.toList());

                }
                dynamicSectionAux.setList(elementList);
            }
        }

    }

    /**
     * set tracking info to user
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     */
    private void setTrackingInfo(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {
        //get if active Orders/
        //

        CustomerOnlyData customerOnlyData = homeInfoConfig.getCustomerOnlyData();
        if (customerOnlyData != null && customerOnlyData.getId() != null && customerOnlyData.getId() > 0) {
//            log.info("method() setTrackingInfo");
            List<DeliveryOrder> activeOrders = getActiveOrdersByUserId(customerOnlyData.getId());
            if (!activeOrders.isEmpty()) {

                if (activeOrders.size() == 1) {

                    Optional<DeliveryOrder> optionalDeliveryOrder = activeOrders.stream().findFirst();

                    optionalDeliveryOrder.ifPresent(deliveryOrder -> {
                        dynamicSectionAux.setIdOrder(deliveryOrder.getIdOrder());
                        dynamicSectionAux.setRedirectURL(sectionAlgolia.getUrlTracking() + deliveryOrder.getIdOrder());
                    });

                } else {
                    dynamicSectionAux.setRedirectURL(sectionAlgolia.getRedirectUrl());
                }

//                log.info("method() ordenes activas -> " + activeOrders);
//                log.info("section algolia tracking -> " + sectionAlgolia.toString());
                dynamicSectionAux.setLabel(String.valueOf(activeOrders.size()));
//                log.info("section tracking -> " + dinamicSectionAux.toString());
            } else {
                sectionAlgolia.setActive(Boolean.FALSE);
            }

        } else {
            dynamicSectionAux.setComponentType(null);
            dynamicSectionAux.setRedirectURL(null);
        }

    }

    /**
     * set banners home
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     */
    private void setBannersHome(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {
        if (sectionAlgolia != null
                && dynamicSectionAux != null
                && homeInfoConfig != null
                && homeInfoConfig.getHomeRequest() != null
                && homeInfoConfig.getHomeRequest().getSource() != null) {

            // duartion time  response banners
            Instant start = Instant.now();

            BannersDTFRes bannersDTFResponse = new BannersDTFRes();


            EnableForEnum sourceEnum = homeInfoConfig.getHomeRequest().getSource();

            switch (sourceEnum) {
                case ANDROID:
                case IOS:
                    bannersDTFResponse = getBannersDTFResMobile(homeInfoConfig);
                    break;
                default:
                    bannersDTFResponse = getBannersDTFResDesktop(homeInfoConfig);
                    break;
            }

            Instant finish = Instant.now();
            //log.info("Banners DURATION in millis : " + Duration.between(start, finish).toMillis());

            // Validate Default Banners:

            if (bannersDTFResponse == null || !bannersDTFResponse.isValid()) {

                // set default banners
                //log.warning("Not found Banners! use default!!");

                Optional<BannerDataCMSType> bannerDataCMSTypeOptional = APIAlgolia.getDefaultBannersHome();

                if (bannerDataCMSTypeOptional.isPresent()) {

                    bannersDTFResponse = new BannersDTFRes();
                    bannersDTFResponse.setCode("200");
                    bannersDTFResponse.setMessage("Success");
                    bannersDTFResponse.setData(bannerDataCMSTypeOptional.get());

                }

            }

            // set banners
            if (bannersDTFResponse != null) {
                switch (homeInfoConfig.getHomeRequest().getSource()) {
                    case ANDROID:
                    case IOS:
                        mobileBanners(dynamicSectionAux, bannersDTFResponse);
                        break;
                    case RESPONSIVE:
                    case WEB:
                    default:
                        desktopBanners(dynamicSectionAux, bannersDTFResponse);
                        break;
                }
            }
        }
    }


    private void mobileBanners(DynamicSection dynamicSectionAux, BannersDTFRes bannersDTFResponse) {

        if (bannersDTFResponse.isValid() && bannersDTFResponse.getData() != null
                && bannersDTFResponse.getData().getMobile() != null
                && bannersDTFResponse.getData().getMobile().getMainBanner() != null
                && !bannersDTFResponse.getData().getMobile().getMainBanner().isEmpty()) {
            List<BannerCMSData> bannerList = bannersDTFResponse.getData().getMobile().getMainBanner();

            List<Element> elementBannerList = new ArrayList<>();
            bannerList.forEach(bannerCMSData -> {
                Element elementBannerAux = new Element();
                elementBannerAux.setId(String.valueOf(bannerCMSData.getIdBanner()));
                elementBannerAux.setUrlBanner(bannerCMSData.getUrlBanner());
                elementBannerAux.setRedirectURL(bannerCMSData.getRedirectUrl());
                elementBannerAux.setFirstDescription(bannerCMSData.getCreative());
                elementBannerAux.setOrderingNumber(bannerCMSData.getOrder());
                elementBannerList.add(elementBannerAux);
            });

            dynamicSectionAux.setList(elementBannerList);
        }
    }

    private void desktopBanners(DynamicSection dynamicSectionAux, BannersDTFRes bannersDTFResponse) {

        if (bannersDTFResponse.isValid() && bannersDTFResponse.getData() != null
                && bannersDTFResponse.getData().getDesktop() != null
                && bannersDTFResponse.getData().getDesktop().getMainBanner() != null
                && !bannersDTFResponse.getData().getDesktop().getMainBanner().isEmpty()) {

            dynamicSectionAux.setBannersDesktop(bannersDTFResponse.getData());

        }

    }

    /**
     * set banners home
     *
     * @param sectionAlgolia
     * @param dynamicSectionAux
     * @param homeInfoConfig
     */
    private void setMainBanners(Component sectionAlgolia,
                                DynamicSection dynamicSectionAux,
                                HomeInfoConfig homeInfoConfig,
                                BannersDTFRes bannersDTFResponse
    ) {
        if (sectionAlgolia != null
                && dynamicSectionAux != null
                && homeInfoConfig != null
                && homeInfoConfig.getHomeRequest() != null
                && homeInfoConfig.getHomeRequest().getSource() != null) {
            // set banners
            if (bannersDTFResponse != null) {
                switch (homeInfoConfig.getHomeRequest().getSource()) {
                    case ANDROID:
                    case IOS:
                    case RESPONSIVE:
                        // todo: VALIDAR NULOS
                        if (Objects.nonNull(bannersDTFResponse.getData().getMobile())) {
                            List<BannerCMSData> mobileBanners = bannersDTFResponse.getData().getMobile().getMainBanner();
                            setMainBanners(dynamicSectionAux, mobileBanners);
                        }
                        break;
                    case WEB:
                    default:
                        // todo: VALIDAR NULOS
                        if (Objects.nonNull(bannersDTFResponse.getData().getDesktop())) {
                            List<BannerCMSData> desktopBanners = bannersDTFResponse.getData().getDesktop().getMainBanner();
                            setMainBanners(dynamicSectionAux, desktopBanners);
                        }
                        break;
                }
            }
        }
    }


    private void setMainBannersMinLeft(Component sectionAlgolia,
                                DynamicSection dynamicSectionAux,
                                HomeInfoConfig homeInfoConfig,
                                BannersDTFRes bannersDTFResponse
    ) {
        if (sectionAlgolia != null
                && dynamicSectionAux != null
                && homeInfoConfig != null
                && homeInfoConfig.getHomeRequest() != null
                && homeInfoConfig.getHomeRequest().getSource() != null) {
            // set banners
            if (bannersDTFResponse != null) {
                switch (homeInfoConfig.getHomeRequest().getSource()) {
                    case WEB:
                    default:
                        // todo: VALIDAR NULOS
                        if (Objects.nonNull(bannersDTFResponse.getData().getDesktop())) {
                            List<BannerCMSData> desktopBanners = bannersDTFResponse.getData().getDesktop().getMainBanner();
//                            log.info("setMainBannersMinLeft -> " + desktopBanners);
                            setMainBanners(dynamicSectionAux, desktopBanners);
                        }
                        break;
                }
            }
        }
    }

    @org.jetbrains.annotations.Nullable
    private BannersDTFRes getBannersDTFRes(HomeInfoConfig homeInfoConfig) {
        // duartion time  response banners
        if(Objects.nonNull(homeInfoConfig.getAsyncBannersDTFRes())) {
//            log.info("Banners main ahorrado");
            return homeInfoConfig.getAsyncBannersDTFRes();
        }


        Instant start = Instant.now();

        BannersDTFRes bannersDTFResponse = new BannersDTFRes();


        EnableForEnum sourceEnum = homeInfoConfig.getHomeRequest().getSource();

        switch (sourceEnum) {
            case ANDROID:
            case IOS:
            case RESPONSIVE:
                bannersDTFResponse = getBannersDTFResMobile(homeInfoConfig);
                break;
            default:
                bannersDTFResponse = getBannersDTFResDesktop(homeInfoConfig);
                break;
        }

        Instant finish = Instant.now();
        //log.info("Banners DURATION in millis : " + Duration.between(start, finish).toMillis());

        // Validate Default Banners:

        if (bannersDTFResponse == null || !bannersDTFResponse.isValid()) {

            // set default banners
            //log.warning("Not found Banners! use default!!");

            Optional<BannerDataCMSType> bannerDataCMSTypeOptional = APIAlgolia.getDefaultBannersHome();

            if (bannerDataCMSTypeOptional.isPresent()) {

                bannersDTFResponse = new BannersDTFRes();
                bannersDTFResponse.setCode("200");
                bannersDTFResponse.setMessage("Success");
                bannersDTFResponse.setData(bannerDataCMSTypeOptional.get());

            }

        }
        return bannersDTFResponse;
    }

    @org.jetbrains.annotations.Nullable
    private BannersDTFRes getBannersDTFMinLeftV1(HomeInfoConfig homeInfoConfig) {

        // duartion time  response banners
        if(Objects.nonNull(homeInfoConfig.getAsyncBannersDTFResponseMinLeft())) {
//            log.info("Bannerds min left ahorrado");
            return homeInfoConfig.getAsyncBannersDTFResponseMinLeft();
        }

        BannersDTFRes bannersDTFResponse = new BannersDTFRes();

        EnableForEnum sourceEnum = homeInfoConfig.getHomeRequest().getSource();

        if (sourceEnum != null) {
            switch (sourceEnum) {
                case WEB:
                    bannersDTFResponse = getBannersDTFMinLeft(homeInfoConfig);
//                    log.info("getBannersDTFMinLeft -> " + bannersDTFResponse);
                    break;
            }
        }
        if (homeInfoConfig.getCustomerOnlyData() != null
                && Objects.requireNonNull(homeInfoConfig.getCustomerOnlyData()).getId() != null
                && homeInfoConfig.getCustomerOnlyData().getId() == 0
                && bannersDTFResponse != null
                && bannersDTFResponse.getData() != null
                && bannersDTFResponse.getData().getDesktop() != null
                && bannersDTFResponse.getData().getDesktop().getMainBanner() != null){

            bannersDTFResponse.getData().getDesktop().getMainBanner().removeIf(data -> Objects.equals(data.getOrder(), 0));
        }else if (bannersDTFResponse == null || bannersDTFResponse.getData() == null || bannersDTFResponse.getData().getDesktop() == null || bannersDTFResponse.getData().getDesktop().getMainBanner() == null){
            // Poner un banner por defecto min left cuando es null
//            log.info("homeInfoConfig.getCustomerOnlyData().getEmail() -> " + homeInfoConfig.getCustomerOnlyData().getEmail());
//            log.info("homeInfoConfig.getCustomerOnlyData().getDocumentType() -> " + homeInfoConfig.getCustomerOnlyData().getDocumentType());
//            log.info("homeInfoConfig.getCustomerOnlyData().getFirstName() -> " + homeInfoConfig.getCustomerOnlyData().getFirstName());
            String json = "{\"code\":\"OK\",\"message\":\"Success\",\"data\":{\"desktop\":{\"mainBanner\":[{\"idBanner\":\"11757\",\"urlBanner\":\"https://lh3.googleusercontent.com/xMhspwTY9A0ZNVic86k3ZdmBZTKkpQCzq6lhY0dreOl7_aMQ0v8YROnuI_QsHBJ0MYz-584qMnYbKkiUTsCgX3EFJK2si9zpVOPJhlAF33QCeI0\",\"redirectUrl\":\"https://www.farmatodo.com.co/destacados/44497?utm_source=braze&utm_medium=banner_home&utm_campaign=saleoff&utm_content=panini&utm_term=general29ago\",\"idWebSafeBanner\":\"nsdfsbndfiojsdu84932o29430\",\"order\":1,\"directionBanner\":true,\"bannerWeb\":true,\"campaignName\":\"Isdin_ago22\",\"creative\":\"Isdin_ago22\",\"position\":\"MAIN_BANNER_ABOVE\",\"listClusteres\":[0 ],\"category\":0,\"home\":true },{\"idBanner\":\"11727\",\"urlBanner\":\"https://lh3.googleusercontent.com/hDctmT11gfP5D6x0LT90Dhs0jG8LgKbzjwttPqnA_PfTMuc2dijhovtO6Zx36zI1QGPVWqpCewHjO6W21xprZUG_lr6Kso-yhcpEN6OBUuf1jP8i\",\"redirectUrl\":\"https://www.farmatodo.com.co/destacados/42994?utm_source=braze&utm_medium=banner_home&utm_campaign=supplier&utm_content=bdf&utm_term=nivealuminous02ago\",\"idWebSafeBanner\":\"nsdfsbndfiojsdu84932o29430\",\"order\":1,\"directionBanner\":true,\"bannerWeb\":true,\"campaignName\":\"Nivea_AGO22\",\"creative\":\"Nivea_AGO22\",\"position\":\"MAIN_BANNER_BELOW\",\"listClusteres\":[0 ],\"category\":0,\"home\":true }],\"leftAdvertising\":[],\"rightAdvertising\":[],\"staticBanner\":[],\"categoriesAdvertising\":[]}}}";
            bannersDTFResponse = new Gson().fromJson(json, BannersDTFRes.class);
        }
        return bannersDTFResponse;
    }

    private BannersDTFRes getBannersDTFResMobile(HomeInfoConfig homeInfoConfig) {
        Optional<BannersDTFRes> bannersDTFResponse;
        try {
            // call service only mobile
            if (homeInfoConfig.getCustomerOnlyData() != null
                    && homeInfoConfig.getCustomerOnlyData().getEmail() != null
                    && !homeInfoConfig.getCustomerOnlyData().getEmail().isEmpty()) {
                bannersDTFResponse = ApiGatewayService.get().getBannerHome(
                        homeInfoConfig.getCustomerOnlyData().getId(),
                        homeInfoConfig.getCustomerOnlyData().getEmail(),
                        null,
                        null,
                        Objects.nonNull(homeInfoConfig.getHomeRequest()) && Objects.nonNull(homeInfoConfig.getHomeRequest().getCity()) ? homeInfoConfig.getHomeRequest().getCity() : null ,
                        true
                );

            } else {
                bannersDTFResponse = ApiGatewayService.get().getBannerHome(null,null, null, null, homeInfoConfig.getHomeRequest().getCity(), true);
            }
        } catch (Exception e) {
            bannersDTFResponse = Optional.empty();
            e.printStackTrace();
        }

        return bannersDTFResponse.orElse(null);
    }

    @org.jetbrains.annotations.Nullable
    private BannersDTFRes getBannersDTFResDesktop(HomeInfoConfig homeInfoConfig) {
        Optional<BannersDTFRes> bannersDTFResponse;
        try {
            //log.info("method() setBannersHome");

            // set mail user for banners
            if (homeInfoConfig.getCustomerOnlyData() != null
                    && homeInfoConfig.getCustomerOnlyData().getEmail() != null
                    && !homeInfoConfig.getCustomerOnlyData().getEmail().isEmpty()) {
                bannersDTFResponse = ApiGatewayService.get().getBannerHome(homeInfoConfig.getCustomerOnlyData().getId(), homeInfoConfig
                                .getCustomerOnlyData()
                                .getEmail(), null, null, Objects.nonNull(homeInfoConfig.getHomeRequest()) && Objects.nonNull(homeInfoConfig.getHomeRequest().getCity()) ? homeInfoConfig.getHomeRequest().getCity() : null, false
                        );

            } else {
                bannersDTFResponse = ApiGatewayService.get().getBannerHome(null, null, null, null, Objects.nonNull(homeInfoConfig.getHomeRequest()) && Objects.nonNull(homeInfoConfig.getHomeRequest().getCity()) ? homeInfoConfig.getHomeRequest().getCity() : null, false);
            }
        } catch (Exception e) {
            bannersDTFResponse = Optional.empty();
            e.printStackTrace();
        }
        return bannersDTFResponse.orElse(null);
    }

    private BannersDTFRes getBannersDTFMinLeft(HomeInfoConfig homeInfoConfig) {

        Optional<BannersDTFRes> bannersDTFResponse;
        String city = Objects.nonNull(homeInfoConfig.getHomeRequest()) ? homeInfoConfig.getHomeRequest().getCity() : "BOG";
        Integer id = Objects.nonNull(homeInfoConfig.getCustomerOnlyData()) ? homeInfoConfig.getCustomerOnlyData().getId() : 0;
        try {
            if (homeInfoConfig.getCustomerOnlyData() != null
                    && homeInfoConfig.getCustomerOnlyData().getEmail() != null
                    && !homeInfoConfig.getCustomerOnlyData().getEmail().isEmpty()) {


                bannersDTFResponse = ApiGatewayService.get().getBannerHomeMinLeft(id, city,homeInfoConfig
                        .getCustomerOnlyData()
                        .getEmail()
                );

            } else {
                bannersDTFResponse = ApiGatewayService.get().getBannerHomeMinLeft(id, city, null);
            }
        } catch (Exception e) {
            bannersDTFResponse = null;
            e.printStackTrace();
        }
        if(bannersDTFResponse.isPresent()){
            return bannersDTFResponse.get();
        }else{
            return null;
        }
    }

    private void setMainBanners(DynamicSection dynamicSectionAux, List<BannerCMSData> bannerList) {
        if (bannerList != null && !bannerList.isEmpty()) {

            List<Element> elementBannerList = new ArrayList<>();
            bannerList.forEach(bannerCMSData -> {
                //log.info("banner -> " + bannerCMSData.getCampaignName());
                //log.info("banner -> " + bannerCMSData.getCreative());
                Element elementBannerAux = new Element();
                elementBannerAux.setId(String.valueOf(bannerCMSData.getIdBanner()));
                elementBannerAux.setUrlBanner(bannerCMSData.getUrlBanner());
                elementBannerAux.setRedirectURL(bannerCMSData.getRedirectUrl());
                elementBannerAux.setFirstDescription(bannerCMSData.getCreative());
                elementBannerAux.setOrderingNumber(bannerCMSData.getOrder());
                elementBannerList.add(elementBannerAux);
            });

            dynamicSectionAux.setList(elementBannerList);
        }
    }



/*    private List<BannerData> reorganizeBanners(BannersDTFRes bannersRes, List<Long> categories, HomeInfoConfig homeInfoConfig) {

        List<BannerData> bannerDataList = new ArrayList<>();
        if (bannersRes != null
                && bannersRes.getData() != null
                && categories != null ) {
            for (Long categoryId : categories) {

                if (categoryId != null ){

                    List<BannerDTFData> bannersResByCategory = bannersRes
                            .getData()
                            .stream()
                            .filter(bannerFiltering ->
                                    bannerFiltering.getIdCategory() != null
                                            && bannerFiltering
                                            .getIdCategory()
                                            .equals(categoryId)
                            ).collect(Collectors.toList());

                    BannerData bannerDataAux = new BannerData();
                    bannerDataAux.setCategoryId(categoryId);

                    List<BannerInfo> bannerInfoList = getBannerInfoList(bannersResByCategory ,homeInfoConfig);

                    bannerDataAux.setBannerList(bannerInfoList);


                    bannerDataList.add(bannerDataAux);
                }
            }
        }

        return bannerDataList;
    }*/


    /**
     * getBannerInfoList
     * @param bannerByCategory
     * @param homeInfoConfig
     * @return
     */
/*    private List<BannerInfo> getBannerInfoList(List<BannerDTFData> bannerByCategory, HomeInfoConfig homeInfoConfig) {

        if ( bannerByCategory != null ) {

            List<BannerInfo> bannerInfoList = new ArrayList<>();

            // sort
            bannerByCategory.sort(Comparator.comparing(BannerDTFData::getPosition));

            Stream.of(BannerTypeEnum.values()).forEachOrdered(bannerTypeEnum -> {

                List<BannerDTFData> bannerByTypeList = bannerByCategory
                        .stream()
                        .filter( bannerFiltering -> {

                            if ( bannerFiltering != null && bannerFiltering.getTypeImage() != null )
                                return bannerFiltering.getTypeImage().equals(bannerTypeEnum);

                            return false;
                        })
                        .collect(Collectors.toList());

                BannerInfo bannerInfo = new BannerInfo();

                List<Element> elementList = getElementsBannerData(bannerByTypeList , homeInfoConfig);
                if (elementList != null && !elementList.isEmpty()) {
                    bannerInfo.setBannerType(bannerTypeEnum);
                    bannerInfo.setData(elementList);
                    bannerInfoList.add(bannerInfo);
                }

            });

            return bannerInfoList;
        }
        return new ArrayList<>();
    }*/

    /**
     * getElementsBannerData
     * @param bannerByTypeList
     * @param homeInfoConfig
     * @return
     */
/*    private List<Element> getElementsBannerData(List<BannerDTFData> bannerByTypeList, HomeInfoConfig homeInfoConfig) {

        if (bannerByTypeList != null
                && !bannerByTypeList.isEmpty()
                && homeInfoConfig.isValid()
                && homeInfoConfig.getHomeRequest() != null
                && homeInfoConfig.getHomeRequest().isValid()
                && homeInfoConfig.getHomeRequest().getSource() != null ) {

            EnableForEnum enableFor = homeInfoConfig.getHomeRequest().getSource();

            List<Element> elementList = new ArrayList<>();

            bannerByTypeList.forEach(banner -> {


                Element elementAux = new Element();
                elementAux.setId(String.valueOf(banner.getId()));
                elementAux.setRedirectURL(banner.getRedirectURL());
                elementAux.setFirstDescription(banner.getCampaignName());
                //elementAux.setStartDate(banner.getDateTimeFinished());
                elementAux.setOrderingNumber(banner.getPosition());

                // set banner mobile or desktop according source
                switch (enableFor) {
                    case IOS:
                    case ANDROID:
                    case RESPONSIVE:
                        if (banner.getImageMobile() != null && !banner.getImageMobile().isEmpty()){
                            elementAux.setUrlBanner(banner.getImageMobile());
                            elementList.add(elementAux);
                        }
                        break;
                    case WEB:
                        elementAux.setUrlBanner(banner.getImageDesktop());
                        elementList.add(elementAux);
                        break;
                }

            });

            return elementList;

        }

        return new ArrayList<>();
    }*/


    /**
     * set Highlight items
     *
     * @param dynamicSectionAux
     * @param homeInfoConfig
     */
    private void setHighlightItems(DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {

        // TODO: CHANGE 0L DEPT
        List<Highlight> highlightList = new ArrayList<>();
        if (homeInfoConfig != null && homeInfoConfig.getIdStoreGroup() != null && homeInfoConfig.getIdStoreGroup() > 0) {
            highlightList = productsMethods.getHighlightsFromDeptAndStore(0L, homeInfoConfig.getIdStoreGroup());
        }
        if (!highlightList.isEmpty() && homeInfoConfig.getIdStoreGroup() != null) {
            Long currentDate = System.currentTimeMillis();

            List<Element> elementList = new ArrayList<>();

            List<Element> finalElementList = elementList;
            highlightList.forEach(highlightAux -> {
                Element elementAux = new Element();

                elementAux.setId(String.valueOf(highlightAux.getId()));
                elementAux.setFirstDescription(highlightAux.getFirstDescription());
                elementAux.setUrlImage(highlightAux.getUrlImage());
                elementAux.setStartDate(highlightAux.getStartDate());
                elementAux.setEndDate(highlightAux.getEndDate());
                elementAux.setOfferText(highlightAux.getOfferText());
                elementAux.setType(ProductTypeEnum.valueOf(highlightAux.getType()));

                if (Objects.equals(elementAux.getType(), ProductTypeEnum.GROUP)) {
                    elementAux.setAction(ActionEnum.HIGHLIGHTED);
                }

                if (Objects.equals(elementAux.getType(), ProductTypeEnum.UNIQUE)) {
                    elementAux.setProduct(highlightAux.getProduct());
                }

                elementAux.setOrderingNumber(highlightAux.getOrderingNumber());

                finalElementList.add(elementAux);

            });

            if (homeInfoConfig.getHomeRequest() != null && homeInfoConfig.getHomeRequest().getCarouselLimit() != null
                    && homeInfoConfig.getHomeRequest().getCarouselLimit() > 0) {

                int limitList = homeInfoConfig.getHomeRequest().getCarouselLimit();

                elementList = elementList.stream().limit(limitList).collect(Collectors.toList());

            }
            // sort
            Comparator<? super Element> sortElements = Comparator.comparing(Element::getOrderingNumber);
            ;
            elementList = elementList.stream().sorted(sortElements).collect(Collectors.toList());

            dynamicSectionAux.setList(elementList);

        }

    }

    /**
     * set items in previous orders
     *
     * @param dynamicSectionAux
     * @param homeInfoConfig
     */
    private void setPreviousItems(DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {

        CustomerOnlyData customerOnlyData = homeInfoConfig.getCustomerOnlyData();
        if (customerOnlyData != null && homeInfoConfig.getIdStoreGroup() != null) {

            List<Element> elementList = new ArrayList<>();

//            if(!homeInfoConfig.getCarrouselItemListAsync().getPurchases().isEmpty()) {
//                log.info("Purchases Ahorrado");
//            }

            List<Item> itemList = homeInfoConfig.getCarrouselItemListAsync().getPurchases().isEmpty() ?
                    productsMethods.getItemsByIds(customerOnlyData.getPurchases(), homeInfoConfig.getIdStoreGroup()) :
                    homeInfoConfig.getCarrouselItemListAsync()
                            .getPurchases().stream().map(itemAlgolia -> APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia))
                            .filter(Objects::nonNull).collect(Collectors.toList());

            if (itemList != null && !itemList.isEmpty()) {
                List<Element> finalElementList = elementList;
                itemList.forEach(productAux -> {
//            log.info("small_item_list item -> " + productAux.getId());
                    Element elementAux = new Element();
                    List<Item> itemTempList = new ArrayList<>();
                    itemTempList.add(productAux);
                    if (productAux.getId() > 0) elementAux.setId(String.valueOf(productAux.getId()));
                    elementAux.setProduct(itemTempList);
                    elementAux.setType(ProductTypeEnum.UNIQUE);
                    finalElementList.add(elementAux);
                });
//            homeSectionAux.setAction(ActionEnum.DETAIL);
                if (homeInfoConfig.getHomeRequest() != null && homeInfoConfig.getHomeRequest().getCarouselLimit() != null
                        && homeInfoConfig.getHomeRequest().getCarouselLimit() > 0) {

                    int limitList = homeInfoConfig.getHomeRequest().getCarouselLimit();

                    elementList = elementList.stream().limit(limitList).collect(Collectors.toList());

                }
                dynamicSectionAux.setList(elementList);
            } else {
                dynamicSectionAux.setComponentType(null);
            }


        }

    }

    /**
     * set best offers (suggested) to user
     *
     * @param dynamicSectionAux
     * @param homeInfoConfig
     * @throws IOException
     */
    private void setSuggestItems(DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) throws IOException, ConflictException, UnauthorizedException, BadRequestException, InternalServerErrorException, ServiceUnavailableException {


        Suggesteds suggestedList = productsMethods.getSuggesteds(homeInfoConfig.getIdStoreGroup(),0  );
        if (homeInfoConfig.getCustomerOnlyData() != null
                && homeInfoConfig.getCustomerOnlyData().getId() != null
                && homeInfoConfig.getCustomerOnlyData().getId() == 0){
            try {
//                log.info("USUARIO_ANONIMO setSuggestItems ");

                Suggesteds suggestedListAnonymous = productsMethods.getSuggesteds(homeInfoConfig.getIdStoreGroup(), -1 );
                if (suggestedListAnonymous != null
                        && suggestedListAnonymous.getSuggestsList() != null
                        && !suggestedListAnonymous.getSuggestsList().isEmpty() && suggestedList != null
                        && suggestedList.getSuggestsList() != null){

//                    log.info("USUARIO_ANONIMO se agregan para anonimos ");
                    suggestedList.getSuggestsList().addAll(suggestedListAnonymous.getSuggestsList());
                }
            }catch (Exception e){
                log.warning("ERROR ANONIMOS -> " + e.getMessage());
            }


        }

        List<SuggestedObject> suggestsList = getSuggestedPersonalizedInOffer(homeInfoConfig);


        if (suggestedList != null && suggestedList.getSuggestsList() != null) {

            List<Element> elementList = new ArrayList<>();
            List<Element> finalElementList = elementList;
            suggestedList.getSuggestsList().addAll(suggestsList);
            suggestedList.getSuggestsList().forEach(suggestedAux -> {
//        log.info("item suggested -> " + suggestedAux.toStringJson());

                // parse Object

                Element elementAux = new Element();
                elementAux.setId(suggestedAux.getId());
                elementAux.setFirstDescription(suggestedAux.getFirstDescription());
                elementAux.setUrlImage(suggestedAux.getUrlImage());
                elementAux.setStartDate(suggestedAux.getStartDate());
                elementAux.setEndDate(suggestedAux.getEndDate());
                elementAux.setType(ProductTypeEnum.valueOf(suggestedAux.getType()));
                elementAux.setOfferText(suggestedAux.getOfferText());

                // SET PETALO PRIME.
                if (suggestedAux.isPrime()){
                    elementAux.setPrimeDescription(elementAux.getFirstDescription() == null ? "" : elementAux.getFirstDescription());
                    elementAux.setPrimeTextDiscount(elementAux.getOfferText() == null ? "" : elementAux.getOfferText());
                }

                if (Objects.equals(elementAux.getType(), ProductTypeEnum.GROUP)) {
                    elementAux.setAction(ActionEnum.SUGGESTED);
                }
                elementAux.setOrderingNumber(suggestedAux.getPosition());
                elementAux.setPosition(suggestedAux.getPosition());

                if (suggestedAux.getProduct() != null && !suggestedAux.getProduct().isEmpty()) {
                    elementAux.setProduct(suggestedAux.getProduct());
                }

                finalElementList.add(elementAux);
            });

//            homeSectionAux.setAction(ActionEnum.SUGGESTED);
            if (homeInfoConfig.getHomeRequest() != null && homeInfoConfig.getHomeRequest().getCarouselLimit() != null
                    && homeInfoConfig.getHomeRequest().getCarouselLimit() > 0) {

                int limitList = homeInfoConfig.getHomeRequest().getCarouselLimit();

                elementList = elementList.stream().limit(limitList).collect(Collectors.toList());

            }
            dynamicSectionAux.setList(elementList);

        }

    }


    private List<DeliveryOrder> getActiveOrdersByUserId(int userId) {
        Query.Filter filterActive = new Query.FilterPredicate("isActive",
                Query.FilterOperator.EQUAL, true);
        List<DeliveryOrder> deliveryOrderList;
        deliveryOrderList = ofy().load().type(DeliveryOrder.class)
                .filter("idFarmatodo", userId)
                .filter("currentStatus", 0)
                .filter(filterActive).list();

        deliveryOrderList.removeIf(deliveryOrderAux -> deliveryOrderAux.getDeliveryType().equals(DeliveryType.SCANANDGO));
        //log.info("activos -> " + deliveryOrderList.size());

        return deliveryOrderList;
    }

    @SuppressWarnings("ALL")
    @ApiMethod(name = "updateCustomerOnly", path = "/customerEndpoint/updateCustomerOnly", httpMethod = ApiMethod.HttpMethod.PUT)
    public CustomerJSON updateCustomerOnly(final Customer customer) throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException, NotFoundException {
        if (Objects.isNull(customer) || Objects.isNull(customer.getIdCustomerWebSafe()) || customer.getIdCustomerWebSafe().isEmpty()) {
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);
        }
        if (Objects.isNull(customer.getToken()) || !authenticate.isValidToken(customer.getToken().getToken(), customer.getToken().getTokenIdWebSafe())) {
            throw new ConflictException(Constants.INVALID_TOKEN);
        }
        Key<User> userKey = Key.create(customer.getIdCustomerWebSafe());
        User user = users.findUserByKey(userKey);

        if (Objects.isNull(user) || (customer.getId() != user.getId()))
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);

        if (customer.getFirstName() != null && customer.getLastName() != null && customer.getGender() != null && customer.getPhone() != null) {
            //customers.updateCostumer(customerJson, "Customer", "Update");
            customers.customerUpdate(new Customer(customer.getId(), customer.getDocumentNumber(), customer.getFirstName(), customer.getLastName(), customer.getGender(), customer.getPhone(), customer.getProfileImageUrl()));
        }
        Optional<CustomerJSON> optionalCustomerJSON = this.customers.customerInformation(user, 0, false);

        if (!optionalCustomerJSON.isPresent()){
            throw new ConflictException(Constants.CUSTOMER_NOT_FOUND);
        }
        CustomerJSON customerJSON = optionalCustomerJSON.get();


        customerJSON.setBanners(null);
        customerJSON.setHighlightedItems(null);
        customerJSON.setSuggestedProducts(null);
        customerJSON.setPreviousItems(null);
        customerJSON.setAddresses(null);
        customerJSON.setCreditCards(null);
        customerJSON.setPhotos(ApiGatewayService.get().getCustomerPhotos(customer.getId()));

        TalonOneService talonOneService = new TalonOneService();
        talonOneService.updateCustomer(customerJSON);

        try {
            KustomerService kustomerService = new KustomerService();
            CustomerRequestKustomer customerRequestKustomer = CustomerRequestKustomer.buildFromCustomerToUpdate(customerJSON);
            kustomerService.sendCustomerPublisher(customerRequestKustomer);
        } catch (Exception e) {
            log.warning("Error al enviar el cliente a ACTUALIZAR - publiser Kustomer -> " + e.getMessage());
        }

        Braze.updateUserProfileOnBraze(customer);

        return customerJSON;
    }

    @ApiMethod(name = "banner", path = "/customerEndpoint/banner", httpMethod = ApiMethod.HttpMethod.GET)
    public List<Banner> getBanner(@Named("token") final String token,
                                  @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                  @Named("idCustomerWebSafe") final String idCustomerWebSafe)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException, InternalServerErrorException {

        if (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty())
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);

        //Key<Department> classificationLevel1 = Key.create(Department.class, 1L);
        //return ofy().load().type(Banner.class).filter("bannerWeb", false).filterKey("<", classificationLevel1)
        //   .filter("directionBanner", false).list();


        List<com.imaginamos.farmatodo.model.algolia.Banner> bannerList = APIAlgolia.getBanners(null, false, false);
        return bannerList.stream().map(banner -> {
            Banner banner1 = new Banner();
            banner1.setRedirectUrl(banner.getRedirectUrl());
            banner1.setRedirectType(banner.getRedirectType());
            banner1.setRedirectId(banner.getRedirectId());
            banner1.setUrlBanner(banner.getUrlBanner());
            banner1.setDirectionBanner(banner.isDirectionBanner());
            banner1.setBannerWeb(banner.isBannerWeb());
            banner1.setOrder(banner.getOrder());
            banner1.setCampaignName(banner.getCampaignName());
            banner1.setCreative(banner.getCreative());
            banner1.setPosition(banner.getPosition());
            banner1.setIdWebSafeBanner(banner.getIdWebSafeBanner());
            //log.warning("method: getBanners - "+ banner1.getBannerWeb() +" : "+banner1.getIdBanner()+" : "+ banner1.getRedirectUrl() +" : "+ banner1.getUrlBanner());
            return banner1;
        }).collect(Collectors.toList());
    }

    @ApiMethod(name = "deleteCustomerAnonymous", path = "/customerEndpoint/deleteCustomerAnonymous", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Answer deleteCustomerAnonimous(@Named("idCustomerWebSafe") final String idCustomerWebSafe) throws BadRequestException, ConflictException {
        if (Objects.isNull(idCustomerWebSafe)) {
            throw new BadRequestException(Constants.PARAM_IS_EMPTY);
        }
        try {
            //log.info("method deleteCustomerAnonymous(" + idCustomerWebSafe + ")");
            Key<User> userKey = Key.create(idCustomerWebSafe);
            User user = users.findUserByKey(userKey);
            if (user == null)
                throw new ConflictException(Constants.USER_NOT_FOUND);
            else
                ofy().delete().entity(user).now();
        } catch (Exception e) {
            throw new ConflictException(e);
        }
        return new Answer(true);
    }

    @ApiMethod(name = "getAllBlockedUsers", path = "/customerEndpoint/users/blocked", httpMethod = ApiMethod.HttpMethod.GET)
    public List<Integer> getAllBlockedUsers() throws BadRequestException {
        List<Integer> result = new ArrayList<>();
        List<BlockedUser> blockedUserSaved = ofy().load().type(BlockedUser.class).list();
        return (Objects.nonNull(blockedUserSaved) && !blockedUserSaved.isEmpty()) ?
                blockedUserSaved.parallelStream().map(blockUser -> blockUser.getIdUser()).collect(Collectors.toList()) :
                null;
    }

    @ApiMethod(name = "isBlockedUserForId", path = "/customerEndpoint/isBlockedUserForId", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer isBlockedUserForId(@Named("idCustomer") final Integer idCustomer) {
        try {
            final BlockedUser blockedUser = ofy().load().type(BlockedUser.class).filter("idUser", idCustomer).first().now();
            return new Answer(blockedUser != null);
        } catch (Exception e) {
            return new Answer(false);
        }
    }

    @ApiMethod(name = "getCustomerCallCenter", path = "/customerEndpoint/getCustomerCallCenter", httpMethod = ApiMethod.HttpMethod.POST)
    public GetCustomerCallCenterResponse getCustomerCallCenter(final CustomerCallReq request) {
//        log.info("getCustomer(" + request.toString() + ")");
        List<CustomerCallResponseData> customerCallCenter = null;
        try {
            customerCallCenter = ApiGatewayService.get().getCustomerCallCenter(request);

                if (customerCallCenter == null || customerCallCenter.isEmpty())
                    return new GetCustomerCallCenterResponse("NO_CONTENT", "no content customers", new ArrayList<>());

//                log.info("customers: -> {" + customerCallCenter.toString() + "}");

                List<CustomerCallCenterJSON> listCustomerCallCenterJSONS = new ArrayList<>();

                customerCallCenter.forEach(customer -> {
//                    log.info("Customer.email: " + customer.getEmail());
                // Credential credential = users.findUserByEmail(customer.getEmail().toLowerCase());
                User user = users.findUserByIdCustomer(customer.getId().intValue());
                if (user != null) {
                    Token tokenTransport = generateToken();

                    Token tokenClient = new Token();
                    tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
                    tokenClient.setToken(tokenTransport.getToken());
                    encryptToken(tokenClient);
                    tokenClient.setTokenId(UUID.randomUUID().toString());
                    // tokenClient.setOwner(Ref.create(credential.getOwner().getKey()));
                    tokenClient.setTokenExpDate(tokenTransport.getTokenExp());

                    Key<Token> keyToken = ofy().save().entity(tokenClient).now();
                    // user.setIdUserWebSafe(credential.getOwner().getKey().toWebSafeString());
                    user.setIdUserWebSafe(Key.create(User.class, user.getIdUser()).toWebSafeString());
                    tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
                    user.setToken(tokenTransport);

                    user.setLastLogin(new Date().getTime());
                    // credential.setLastLogin(new Date());
                    ofy().save().entities(user);
                    final String idCustomerWebSafe = user.getIdUserWebSafe();
                    CustomerCallCenterJSON customerCallCenterJSONJSON = new CustomerCallCenterJSON(customer, idCustomerWebSafe, tokenTransport);
                    customerCallCenterJSONJSON.setAnalyticsUUID(getAnalyticsUUID(customer.getEmail()));
                    listCustomerCallCenterJSONS.add(customerCallCenterJSONJSON);
                }else{
                    User userCreateDs = new User();
                    userCreateDs.setIdUser(UUID.randomUUID().toString());
                    userCreateDs.setRole("Customer");
                    userCreateDs.setId(customer.getId().intValue());
                    userCreateDs.setLastLogin(new Date().getTime());
                    Key<User> userKey = Key.create(User.class, userCreateDs.getIdUser());
                    userCreateDs.setIdUserWebSafe(userKey.toWebSafeString());

                    Token tokenTransport = generateToken();
                    Token tokenClient = new Token();
                    tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
                    tokenClient.setToken(tokenTransport.getToken());
                    encryptToken(tokenClient);
                    tokenClient.setTokenId(UUID.randomUUID().toString());
                    tokenClient.setOwner(Ref.create(userKey));
                    tokenClient.setTokenExpDate(tokenTransport.getTokenExp());
                    Key<Token> keyToken = ofy().save().entity(tokenClient).now();
                    tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
                    userCreateDs.setIdUserWebSafe(userKey.toWebSafeString());
                    ofy().save().entity(userCreateDs).now();

                    Credential credential = getCredencial(customer.getEmail().toLowerCase());
                    if(credential == null){
                        savedCredentialsDS(userKey, customer.getEmail().toLowerCase());
                    }
                    final String idCustomerWebSafe = userCreateDs.getIdUserWebSafe();
                    CustomerCallCenterJSON customerCallCenterJSONJSON = new CustomerCallCenterJSON(customer, idCustomerWebSafe, tokenTransport);
                    customerCallCenterJSONJSON.setAnalyticsUUID(getAnalyticsUUID(customer.getEmail()));
                    listCustomerCallCenterJSONS.add(customerCallCenterJSONJSON);
                }

            });
//            log.info("Customers size -> " + listCustomerCallCenterJSONS.size());
            final GetCustomerCallCenterResponse callCenterResponse = new GetCustomerCallCenterResponse("OK", "success", listCustomerCallCenterJSONS);
            return callCenterResponse;
        } catch (IOException e) {
            log.severe("Error: {" + e.getMessage() + "}");
            return new GetCustomerCallCenterResponse("ERROR", "error querying customer", new ArrayList<>());
        }
    }

    private String getAnalyticsUUID(String email) {
        return ApiGatewayService.get().getUUIDFromBraze(email).orElse(null);
    }


    //    private boolean validatePassword(final String password) {
//        return password.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[a-zA-Z]).{6,}$");
//    }
    //VALIDDATE PASSWORD pero no hay necesidad de que hallan minusculas, mayusculas combinadas
    private boolean validatePassword(final String password) {
        return password.matches("^(?=.*\\d)(?=.*[a-zA-Z]).{6,}$");
    }



    @ApiMethod(name = "activeOrderCustomer", path = "/customerEndpoint/activeOrderCustomer", httpMethod = ApiMethod.HttpMethod.POST)
    public OrderActiveListRes activeOrderCustomer(final OrderActiveRequest orderActiveRequest) throws ConflictException, IOException {
//modificar para la excepcion
        if (orderActiveRequest.getIdFarmatodo() != null && orderActiveRequest.getIdFarmatodo() > 0) {
            return getOrderActiveList(orderActiveRequest.getIdFarmatodo());
        } else if (orderActiveRequest.getDocumentNumber() != null && !orderActiveRequest.getDocumentNumber().isEmpty()) {
            Customer customer = customers.findCustomerByDocumentNumber(orderActiveRequest.getDocumentNumber());
//            log.info("s" + customer);
            if (customer != null && customer.getId() > 0) {
                return getOrderActiveList((long) customer.getId());
            }
        }
        return new OrderActiveListRes();

    }

    @NotNull
    private OrderActiveListRes getOrderActiveList(final Long idFarmatodo) {
        List<DeliveryOrder> deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idFarmatodo", idFarmatodo).filter("isActive", true).list();
        //log.warning("order" + deliveryOrder);
        OrderActiveListRes response = new OrderActiveListRes();
        List<OrderActiveResponse> orders = new ArrayList<>();
        deliveryOrder.forEach(order -> {
            OrderActiveResponse res = new OrderActiveResponse();
            res.setIdOrder(order.getIdOrder());
            res.setActive(order.getActive());
            orders.add(res);
        });
        response.setData(orders);

        return response;
    }
    @ApiMethod(name = "orderCustomerFinalized", path = "/customerEndpoint/orderCustomerFinalized", httpMethod = ApiMethod.HttpMethod.POST)
    public OrderFinalizedCustomerRes orderCustomerFinalized(FinalizedOrderCustomer finalizedOrderCustomer) {
        try {
            if (finalizedOrderCustomer == null || finalizedOrderCustomer.getIdOrder() <= 0)
                return new OrderFinalizedCustomerRes(false, Constants.ORDER_NO_EXISTE);

            JSONObject customerJson = new JSONObject();
            customerJson.put("orderId", finalizedOrderCustomer.getIdOrder());
            DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("idOrder", finalizedOrderCustomer.getIdOrder()).first().now();
            if (deliveryOrder != null) {
                deliveryOrder.setLastStatus(ORDER_FINALIZED);
                deliveryOrder.setActive(false);
                ofy().save().entities(deliveryOrder);
//                log.info("delivery order finalizaed" + deliveryOrder);
                return new OrderFinalizedCustomerRes(true, Constants.ORDER_ACTIVE_MODIFIED);

            } else{
                return new OrderFinalizedCustomerRes(false, Constants.ORDER_NO_EXISTE);
            }

        } catch(Exception e){
            log.warning("Error en orderCustomerFinalized. Mensaje: " + e.getMessage() );
            return new OrderFinalizedCustomerRes(false, Constants.ERROR_FINALIZED_ORDER);

        }
    }

    private Boolean isCalLCenter(final String phone, final String email) throws IOException {
        Boolean isCallCenter = false;

        CustomerNewLoginReq request = new CustomerNewLoginReq();
        request.setPhone(phone);
        request.setEmail(email);

        List<CustomerNewLoginRes> listCustomers = new ArrayList<>();
        List<CustomerDataLoginRes> dataLoginRes = new ArrayList<>();

        listCustomers = ApiGatewayService.get().getDataForLogin(request);

        if (Objects.nonNull(listCustomers.get(0))) {
//            log.info("listCustomers [0] -> " + listCustomers.get(0));
            CustomerNewLoginRes customerNewLoginRes = listCustomers.get(0);
            User user = null;
            try {
                user = users.findUserByIdCustomer(customerNewLoginRes.getIdCustomer().intValue());
            } catch (Exception e) {
                log.warning(Constants.USER_NOT_FOUND);
            }

            if (user != null) {
                CustomerOnlyData customerOnlyData = null;
                try {
                    customerOnlyData = this.customers.setCustomerOnlyData(user, 26, true);
                }catch (Exception e){
                    log.warning("isCalLCenter() Ocurrio un error al consultar el cliente.");
                }

                if (Objects.isNull(customerOnlyData)) {
                    log.warning(Constants.USER_NOT_FOUND);
                }

                if (customerOnlyData.getRegisteredBy().equals("CALLCENTER")) {
                    isCallCenter = true;
                }
            }
        }

        return isCallCenter;
    }

    /**
     * set best flash offers
     *
     * @param dynamicSectionAux
     * @throws IOException
     */
    private void setFlashOfferItems(DynamicSection dynamicSectionAux) throws IOException {


        List<ItemAlgolia> listAlgolia = APIAlgolia.getFlashOffers();
        List<Element> elementList = new ArrayList<>();
        if (listAlgolia != null && !listAlgolia.isEmpty()) {
            listAlgolia.forEach(itemAlgolia -> {

                List<Item> listItem = new ArrayList<>();
                Item item = APIAlgolia.getItemToItemAlgolia(new Item(), itemAlgolia);
                listItem.add(item);

                Element elementAux = new Element();
                elementAux.setId(itemAlgolia.getId());
                elementAux.setProduct(listItem);
                elementAux.setType(ProductTypeEnum.UNIQUE);

                elementList.add(elementAux);
            });

            dynamicSectionAux.setList(elementList);

        }

    }

    @ApiMethod(name = "getCustomerPrimeCart", path = "/customerEndpoint/{customerId}/prime-cart", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerResponseCart GetCustomerPrimeCart(
            @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Named("customerId") Long customerId,
            @Named("deliveryType") String deliveryType,
            HttpServletRequest httpServletRequest) throws IOException, BadRequestException, ConflictException {
//        log.info("deliveryType: " + deliveryType);

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        if (customerId == null) {
            throw new BadRequestException("BadRequest [request is null]");
        }

        if (deliveryType == null) {
            throw new BadRequestException("BadRequest [deliveryType is null]");
        }

        CustomerResponseCart customerResponseCart = new CustomerResponseCart();
        customerResponseCart.setMissing_purchase(0.0);
        customerResponseCart.setMinimum_purchase(0.0);
        if (!deliveryType.trim().equalsIgnoreCase("EXPRESS")) {
            return customerResponseCart;
        }
        try {
            Key<Customer> customerKey = Key.create(idCustomerWebSafe);
            customerResponseCart = ApiGatewayService.get().getCustomerCreditCardPrimeData(customerId);
            boolean isActive = customerResponseCart.isActive();

            String deliveryMinAmount = obtainMinAmount();
            if (deliveryMinAmount == null || deliveryMinAmount.equals("")) {
                throw new BadRequestException("BadRequest [deliveryMinAmount is null]");
            }
            double deliveryMinAmountDouble = Double.parseDouble(deliveryMinAmount);
            PrimeCartDomain primeCartDomain = getSaving(idCustomerWebSafe, httpServletRequest, customerId, deliveryType, isActive, deliveryMinAmountDouble);

            customerResponseCart.setSavings(primeCartDomain.getSavings());
            customerResponseCart.setMissing_purchase(primeCartDomain.getMissings());
            customerResponseCart.setMinimum_purchase(deliveryMinAmountDouble);

        } catch (Exception e) {
            log.warning("Error en el servicio {}" + e);
            throw new BadRequestException(Constants.CUSTOMER_NOT_FOUND);
        }

        return customerResponseCart;
    }


    private long getIdItemPrime(DeliveryOrderItem deliveryOrderItemListr) {
        AtomicLong idItemPrime = new AtomicLong();

        if ( primeUtil.isItemPrime(deliveryOrderItemListr.getId())) {
            idItemPrime.set(deliveryOrderItemListr.getId());
        }
        //log.info("idItemPrime -> " + idItemPrime.get());
        return idItemPrime.get();
    }

    private double obtainMissingPurchases(String idCustomerWebSafe, double deliveryMinAmountDouble, boolean isActive, String deliveryType, HttpServletRequest httpServletRequest, Long customerId) throws IOException {
        RequestSourceEnum sourceEnum = ftdUtilities.getSourceFromRequestHeaderForPays(httpServletRequest);
        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        DeliveryOrder deliveryOrderSavedShoppingCart = ofy()
                .load()
                .type(DeliveryOrder.class)
                .filter("currentStatus", 1)
                .ancestor(Ref.create(customerKey))
                .first()
                .now();

        if (deliveryOrderSavedShoppingCart == null)
            return 0.0;

        boolean isScanAndGoFordelivery = isOrdenScanAndGo(deliveryOrderSavedShoppingCart);
        boolean isScanAndGo = false;
        if (isScanAndGoFordelivery && (deliveryOrderSavedShoppingCart.getDeliveryType().getDeliveryType().equalsIgnoreCase(deliveryType))) {
            isScanAndGo = true;
        }

        List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSavedShoppingCart).list();

        if (deliveryOrderItemList == null)
            return 0.0;

        validateDuplicateItems(deliveryOrderItemList);

        List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);

        deliveryOrderItemList = itemsScanAndGo;

        if (deliveryOrderItemList == null)
            return 0.0;

        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        // delete tips in memory for prime
        if (tipConfigOptional.isPresent()) {
            deleteTipsPrimeCalc(deliveryOrderItemList, tipConfigOptional.get());
        }

        double total = 0.0;

        try {
            List<Long> toRemove = new ArrayList<>();
            for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                long id = getIdItemPrime(deliveryOrderItem);
                if (id != 0) {
                    if (deliveryOrderItemList.size() == 1) {
                        return 0.0;
                    }
                    if (deliveryOrderItemList.size() == 2) {
                        if (id == deliveryOrderItem.getId()) {
                            toRemove.add(id);
                        }
                    }
                }
            }
            if (toRemove.size() == 2) {
                deliveryOrderItemList.removeIf(it -> toRemove.contains(it.getId()));
            }
        } catch (Exception e) {
            log.warning("Ocurrío un error obteniendo el id item de prime");
        }

        List<DeliveryOrderProvider> deliveryOrderProvidersList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderSavedShoppingCart).list();
        if (deliveryOrderProvidersList != null && !deliveryOrderProvidersList.isEmpty()) {
            deliveryOrderItemList.removeIf(it ->
                    deliveryOrderProvidersList.stream().
                            anyMatch(it2 -> it2.getItemList().stream().anyMatch(it3 -> it3.getId() == it.getId())));
        }

        if (deliveryOrderItemList == null || deliveryOrderItemList.isEmpty()) {
            return 0.0;
        }

        OrderJson valuesShopping = shoppingCart(deliveryOrderItemList, customerId, sourceEnum, deliveryType, customerKey, idCustomerWebSafe);
        if (valuesShopping != null) {
            total = valuesShopping.getSubTotalPrice();
        }

        if (total >= deliveryMinAmountDouble)
            return 0.0;
        else
            return deliveryMinAmountDouble - total;
    }

    private PrimeCartDomain getSaving(String idCustomerWebSafe, HttpServletRequest httpServletRequest, long customerId,
                             String deliveryType, boolean isActive, double deliveryMinAmountDoubleMissings) throws BadRequestException, IOException {

        RequestSourceEnum sourceEnum = ftdUtilities.getSourceFromRequestHeaderForPays(httpServletRequest);
        Key<Customer> customerKey = Key.create(idCustomerWebSafe);
        DeliveryOrder deliveryOrderSavedShoppingCart = ofy()
                .load()
                .type(DeliveryOrder.class)
                .filter("currentStatus", 1)
                .ancestor(Ref.create(customerKey))
                .first()
                .now();
        PrimeCartDomain primeCartDomain = new PrimeCartDomain();
        primeCartDomain.setSavings(0.0);
        primeCartDomain.setMissings(0.0);

        double subTotalOrder = 0.0;
        if (deliveryOrderSavedShoppingCart == null) {
            return primeCartDomain;
        }

        double deliveryValue = Objects.nonNull(deliveryOrderSavedShoppingCart.getDeliveryPrice()) ? deliveryOrderSavedShoppingCart.getDeliveryPrice() : 0D;

        boolean isScanAndGo = isOrdenScanAndGo(deliveryOrderSavedShoppingCart);

        //log.info("isScanAndGo" + isScanAndGo);

        List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrderSavedShoppingCart).list();

        if (deliveryOrderItemList == null) {
            return primeCartDomain;
        }
        validateDuplicateItems(deliveryOrderItemList);

        List<DeliveryOrderItem> itemsScanAndGo = new ArrayList<>(deliveryOrderItemList);
        deliveryOrderItemList = itemsScanAndGo;

        List<DeliveryOrderProvider> deliveryOrderProvidersList = ofy().load().type(DeliveryOrderProvider.class).ancestor(deliveryOrderSavedShoppingCart).list();
        if (deliveryOrderProvidersList != null && !deliveryOrderProvidersList.isEmpty()) {
            deliveryOrderItemList.removeIf(it ->
                    deliveryOrderProvidersList.stream().
                            anyMatch(it2 -> it2.getItemList().stream().anyMatch(it3 -> it3.getId() == it.getId())));
        }

        if (deliveryOrderItemList == null) {
            return primeCartDomain;
        }

        removeScanAndGoItems(deliveryOrderItemList);

        Optional<TipConfig> tipConfigOptional = GrowthBookConfigLoader.getTipsConfig(Constants.CUSTOMER_ANONYMOUS);

        if (tipConfigOptional.isPresent()) {
            deleteTipsPrimeCalc(deliveryOrderItemList, tipConfigOptional.get());
        }

        List<Long> toRemove = new ArrayList<>();
        for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {

            long id = getIdItemPrime(deliveryOrderItem);
            if (id != 0) {
                if (deliveryOrderItemList.size() == 1) {
                    return primeCartDomain;
                }
                if (deliveryOrderItemList.size() == 2) {
                    if (id == deliveryOrderItem.getId()) {
                        toRemove.add(id);
                    }
                }
            }
        }
        if (toRemove.size() == 2) {
            deliveryOrderItemList.removeIf(it -> toRemove.contains(it.getId()));
        }

        List<ItemAlgolia> itemsDiscountTalon = new ArrayList<>();

        ValidateOrderReq validateOrderReq = new ValidateOrderReq();
        validateOrderReq.setSource(sourceEnum.toString());
        validateOrderReq.setCustomerId((int) customerId);
        validateOrderReq.setStoreId(26);
        validateOrderReq.setDeliveryType(deliveryType);
        validateOrderReq.setItems(new ArrayList<>());
        //enviar deliveryOrderItemList por  items
        for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
            ValidateOrderReq.Item item = new ValidateOrderReq().new Item();
            item.setItemId((int) deliveryOrderItem.getId());
            item.setQuantityRequested(deliveryOrderItem.getQuantitySold());
            validateOrderReq.getItems().add(item);
        }
        validateOrderReq.setIdCustomerWebSafe(idCustomerWebSafe);
        Response<ValidateOrderBackend3> response = ApiGatewayService.get().validateOrder(validateOrderReq, "");
        if (response != null && response.code() == 200) {
            ValidateOrderBackend3 validateOrderBackend3 = response.body();
            if (validateOrderBackend3 != null && validateOrderBackend3.getData() != null) {
                subTotalOrder = validateOrderBackend3.getData().getSubTotalPrice();
                deliveryValue = validateOrderBackend3.getData().getDeliveryValue();
                itemsDiscountTalon = validateOrderBackend3.getData().getItems();
            }
        }


        if (!itemsDiscountTalon.isEmpty()) {
            itemsDiscountTalon = itemsDiscountTalon.stream().filter(item -> item.isTalonDiscount()).collect(Collectors.toList());
        }

        double totalPrime = 0.0;
        double total = 0.0;
        double totalTalon = 0.0;
        boolean allItemsDiscountTalon = true;
        try {
            Map<String, ItemAlgolia> algoliaItemsMap = APIAlgolia.getItemsMultiquery(
                    deliveryOrderItemList.stream().map(DeliveryOrderItem::getId).collect(Collectors.toList()), 26L)
                    .stream().collect(Collectors.toMap(ItemAlgolia::getId, item -> item));

            for (int i = 0; i < deliveryOrderItemList.size(); i++) {
                DeliveryOrderItem item = deliveryOrderItemList.get(i);
                if (item != null) {

                    Optional<ItemAlgolia> itemTalon = itemsDiscountTalon.stream().filter(itemTalonOne -> itemTalonOne.getId().equals(String.valueOf(item.getId()))).findFirst();

                    if (!itemTalon.isPresent()) {
                        allItemsDiscountTalon = false;
                        ItemAlgolia itemAlgolia = algoliaItemsMap.get(String.valueOf(item.getId()));
                        if (Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getPrimePrice()) && Objects.nonNull(itemAlgolia.getFullPrice()) && itemAlgolia.getPrimePrice().doubleValue() > 0.0) {
                            double amount = item.getQuantitySold() * itemAlgolia.getPrimePrice();

                            double fullAmount;
                            if (Objects.nonNull(itemAlgolia.getOfferPrice()) && itemAlgolia.getOfferPrice() > 0.0) {
                                fullAmount = item.getQuantitySold() * itemAlgolia.getOfferPrice();

                            } else {
                                fullAmount = item.getQuantitySold() * itemAlgolia.getFullPrice();
                            }
                            totalPrime = totalPrime + amount;
                            total = total + fullAmount;
                        } else if (Objects.nonNull(itemAlgolia) && Objects.nonNull(itemAlgolia.getFullPrice())) {

                            double amount;
                            double fullAmount;
                            if (Objects.nonNull(itemAlgolia.getOfferPrice()) && itemAlgolia.getOfferPrice() > 0.0) {
                                amount = item.getQuantitySold() * itemAlgolia.getOfferPrice();
                                fullAmount = item.getQuantitySold() * itemAlgolia.getOfferPrice();
                            } else {
                                amount = item.getQuantitySold() * itemAlgolia.getFullPrice();
                                fullAmount = item.getQuantitySold() * itemAlgolia.getFullPrice();
                            }
                            totalPrime = totalPrime + amount;
                            total = total + fullAmount;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warning("error: " + e);
        }

        if (deliveryOrderItemList == null || deliveryOrderItemList.isEmpty()) {
            return primeCartDomain;
        }

        if (allItemsDiscountTalon == true) {
            totalTalon = totalTalon + itemsDiscountTalon.stream().mapToDouble(ItemAlgolia::getDiscount).sum();
        }

        if (!deliveryType.equals("EXPRESS")) {
            deliveryValue = 0.0;
        }
        double saving = total - totalPrime;
        try {
            String deliveryMinAmount = obtainMinAmount();
            if (deliveryMinAmount == null || deliveryMinAmount.equals("")) {
                throw new BadRequestException("BadRequest [deliveryMinAmount is null]");
            }
            double deliveryMinAmountDouble = Double.parseDouble(deliveryMinAmount);

            if (!isActive) {
                saving = saving + deliveryValue;
            } else if (total >= deliveryMinAmountDouble) {
                saving = saving + deliveryValue;
            } else if (totalTalon >= deliveryMinAmountDouble) {
                saving = deliveryValue;
            }
            primeCartDomain.setSavings(saving);
        } catch (Exception e) {
            log.warning("Error al obtener el deliveryMinAmount" + e.getMessage());
            return primeCartDomain;
        }
        //log.info("subTotalOrder: " + subTotalOrder);

        if (subTotalOrder >= deliveryMinAmountDoubleMissings)
            primeCartDomain.setMissings(0.0);
        else
            primeCartDomain.setMissings(deliveryMinAmountDoubleMissings - subTotalOrder);

        log.info("primeCartDomain: " + primeCartDomain.toString());
        return primeCartDomain;
    }

    private void removeScanAndGoItems(List<DeliveryOrderItem> deliveryOrderItemList) {
        OrderService.removeItemsScanAndGo(deliveryOrderItemList);
    }

    private Boolean isOrdenScanAndGo(DeliveryOrder order) {
        return Objects.nonNull(order) && Objects.nonNull(order.getDeliveryType()) && isScanAndGo(order.getDeliveryType().getDeliveryType());
    }
    private CustomerCoupon obtainCustomerCoupon(final Key<Customer> customerKey) {
        try {
//            log.info("obtainCustomerCoupon(" + customerKey.toString() + ")");
            final List<CustomerCoupon> customerCoupons = ofy().load().type(CustomerCoupon.class).filter("customerKey", customerKey).orderKey(false).list();
//            log.info("IF(customerCoupons!=null && !customerCoupons.isEmpty()) : [" + (customerCoupons != null && !customerCoupons.isEmpty()) + "]");
            if (customerCoupons != null && !customerCoupons.isEmpty()) {
                customerCoupons.sort(Comparator.comparing(a -> a.getUseTime()));
                final int positionLastCupon = customerCoupons.size() - 1;
                final CustomerCoupon couponToRedim = customerCoupons.get(positionLastCupon);
                if (couponToRedim != null) {
//                    log.info("obtainCustomerCoupon cupon encontrado(" + couponToRedim.getCustomerCouponId() + ")");
                    return couponToRedim;
                }
                return null;
            }
            return null;
        } catch (Exception e) {
            log.warning("Error al obtener cupon cuando el cliente elimina todo el carrito... Mensaje: " + e.getMessage());
            return null;
        }

    }
    private OrderJson shoppingCart(List<DeliveryOrderItem> item, long customerId, RequestSourceEnum sourceEnum, String deliveryType, Key<Customer> customerKey, String idCustomerWebSafe) throws IOException {
        OrderJson orderJson = null;
        long value = 0;
        ValidateOrderReq validateOrderReq = new ValidateOrderReq();
        validateOrderReq.setSource(sourceEnum.toString());
        validateOrderReq.setCustomerId((int) customerId);
        validateOrderReq.setStoreId(26);
        validateOrderReq.setDeliveryType(deliveryType);
        validateOrderReq.setItems(new ArrayList<>());
        for(DeliveryOrderItem items : item) {
            ValidateOrderReq.Item itemReq = new ValidateOrderReq().new Item();
            itemReq.setItemId((int) items.getId());
            itemReq.setQuantityRequested(items.getQuantitySold());
            validateOrderReq.getItems().add(itemReq);
        }
        CustomerCoupon couponInfo = obtainCustomerCoupon(customerKey);
        if (Objects.nonNull(couponInfo) && Objects.nonNull(couponInfo.getCouponId()) && Objects.nonNull(couponInfo.getCouponId().get())) {
            if(couponInfo.getCouponId().get().getOfferId() != null &&
                    couponInfo.getCouponId().get().getCouponType() != null) {
                validateOrderReq.setCoupons(new ArrayList<>());
                ValidateOrderReq.Coupon coupon = new ValidateOrderReq().new Coupon();
                try {
                    coupon.setOfferId(Math.toIntExact(couponInfo.getCouponId().get().getOfferId()));
                }catch (Exception e){
                    log.warning("Fallo seteo setOfferId Coupon Customer --> " + customerId +  " coupon couponId -->" +  couponInfo.getCouponId());
                    coupon.setOfferId(0);
                }
                coupon.setCouponType(couponInfo.getCouponId().get().getCouponType().getCouponType());
                validateOrderReq.getCoupons().add(coupon);
            }
        }
        validateOrderReq.setIdCustomerWebSafe(idCustomerWebSafe);
        Response<ValidateOrderBackend3> response = ApiGatewayService.get().validateOrder(validateOrderReq, "");
        if (response != null && response.code() == 200) {
            ValidateOrderBackend3 validateOrderBackend3 = response.body();
            if (validateOrderBackend3 != null && validateOrderBackend3.getData() != null && validateOrderBackend3.getData().getItems() != null && !validateOrderBackend3.getData().getItems().isEmpty()) {
                orderJson = validateOrderBackend3.getData();
            }
        }
//        log.info("shoppingCart: " + value);
        return orderJson;

    }


    private Boolean isScanAndGo(final String deliveryType) {
//        log.info("method  isScanAndGo  deliveryType not null ->  " + Objects.nonNull(deliveryType));
        return Objects.nonNull(deliveryType) && !deliveryType.isEmpty() && DeliveryType.SCANANDGO.getDeliveryType().equals(deliveryType);
    }

    private void validateDuplicateItems(List<DeliveryOrderItem> deliveryOrderItemList) {
        if (Objects.nonNull(deliveryOrderItemList) && !deliveryOrderItemList.isEmpty()) {
            Map<Long, DeliveryOrderItem> listItemResult = new HashMap<>();
            deliveryOrderItemList.stream().filter(item -> Objects.nonNull(item)).forEach(item -> {
                if (listItemResult.containsKey(item.getId())) {
                    if (listItemResult.get(item.getId()).getQuantitySold() < item.getQuantitySold()) {
                        listItemResult.get(item.getId()).setQuantitySold(item.getQuantitySold());
                    }
                    ofy().delete().entities(item);
                } else {
                    listItemResult.put(item.getId(), item);
                }
            });
            deliveryOrderItemList = listItemResult.values().stream().collect(Collectors.toList());
        }
    }

    private void deleteTipsPrimeCalc(List<DeliveryOrderItem> deliveryOrderItemList, TipConfig tipConfig) {

        deliveryOrderItemList.removeIf( item -> {

            if (tipConfig == null || tipConfig.getItemTips() == null ){
                return false;
            }
            return tipConfig.getItemTips()
                    .stream()
                    .anyMatch(itemTip -> itemTip.getItemId() != null && itemTip.getItemId().longValue() == item.getId());
        });

    }

    private void deleteItemInCart(DeliveryOrder deliveryOrder, long itemId, Key<Customer> customerKey) {

        if (deliveryOrder == null || itemId <= 0){
            return;
        }

        Key<DeliveryOrder> deliveryOrderKey = Key.create(customerKey, DeliveryOrder.class, deliveryOrder.getIdDeliveryOrder());

        DeliveryOrderItem deliveryOrderItem = ofy()
                .load()
                .type(DeliveryOrderItem.class)
                .filter("idItem", Key.create(Item.class, String.valueOf(itemId)))
                .ancestor(Ref.create(deliveryOrderKey))
                .first().now();

        if (deliveryOrderItem == null){
            log.warning("No se encuentra el item a eliminar , ITEM: " + itemId);
            return;
        }

//        log.info("ITEM a eliminar -> " + itemId);

        ofy().delete().entity(deliveryOrderItem).now();

    }

    @ApiMethod(name = "brazeNotifications", path = "/customerEndpoint/brazeNotifications", httpMethod = ApiMethod.HttpMethod.PUT)
    public GenericResponse<String> brazeNotificationsPreferences(
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            NotificationAndEmailBrazeRequest notificationAndEmailBrazeRequest) throws ConflictException, BadRequestException, IOException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        if (Objects.isNull(idCustomerWebSafe))
            throw new ConflictException(Constants.CUSTOMER_WEB_SAFE_ID_NULL);

        User user = null;
        Key<User> userKey = Key.create(idCustomerWebSafe);
        user = users.findUserByKey(userKey);

        GenericResponse<String> response = new GenericResponse<>();

        if (user != null && user.getId() > 0) {
            Optional<CustomerJSON> customerJSON = ApiGatewayService.get().getCustomerById(user.getId());
            if(customerJSON.isPresent() && customerJSON.get().getEmail() != null && !customerJSON.get().getEmail().isEmpty()) {
                notificationAndEmailBrazeRequest.setEmail(customerJSON.get().getEmail());
                Boolean brazeResponse = ApiGatewayService.get().updateBrazeNotificationPreferences(notificationAndEmailBrazeRequest);
                if(brazeResponse) {
                    response.setData("Usuario actualizado exitosamente");
                    response.setCode("200");
                    response.setMessage("Usuario actualizado exitosamente en braze");
                    return response;
                } else {
                    response.setData("Ocurrio un error en utilities al actualizar las preferencias");
                    response.setCode("500");
                    response.setMessage("Ocurrio error en utilities al actualizar las preferencias");
                    return response;
                }

            }
        }
        response.setData("Ocurrio un error al actualizar las preferencias");
        response.setCode("500");
        response.setMessage("Ocurrio un error al actualizar las preferencias");
        return response;
    }

    @ApiMethod(name = "getBrazeNotificationsPreferences", path = "/customerEndpoint/brazeNotifications", httpMethod = ApiMethod.HttpMethod.GET)
    public NotificationBrazeRequest getBrazeNotificationsPreferences(
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Named("idCustomerWebSafe") final String idCustomerWebSafe) throws ConflictException, BadRequestException, IOException {

        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        if (Objects.isNull(idCustomerWebSafe))
            throw new ConflictException(Constants.CUSTOMER_WEB_SAFE_ID_NULL);

        User user = null;
        Key<User> userKey = Key.create(idCustomerWebSafe);
        user = users.findUserByKey(userKey);

        NotificationAndEmailBrazeRequest notificationAndEmailBrazeRequest = new NotificationAndEmailBrazeRequest();

        if (user != null && user.getId() > 0) {
            Optional<CustomerJSON> customerJSON = ApiGatewayService.get().getCustomerById(user.getId());
            if (customerJSON.isPresent() && customerJSON.get().getEmail() != null && !customerJSON.get().getEmail().isEmpty()) {
                notificationAndEmailBrazeRequest.setEmail(customerJSON.get().getEmail());
                NotificationBrazeRequest brazeResponse = ApiGatewayService.get().getBrazeNotificationPreferences(notificationAndEmailBrazeRequest);
                if (brazeResponse != null) {
                    return brazeResponse;
                } else {
                    throw new ConflictException("Ocurrio un error al encontrar las preferencias");
                }

            }
        } else {
            throw new ConflictException("Ocurrio un error al encontrar las preferencias");
        }
        throw new ConflictException("Ocurrio un error al encontrar las preferencias");
    }

    @ApiMethod(name = "getCustomerWebSafe", path = "/customerEndpoint/getCustomerWebSafe", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerDataLoginRes getCustomerWebSafe(@Named("email") final String email) throws ConflictException {
        CustomerResponse customerResponse = null;
        try {
            customerResponse = ApiGatewayService.get().getCustomerByEmail(email.toLowerCase());
            CustomerDataLoginRes customerDataTokens = getCustomerKeysFromId(String.valueOf(customerResponse.getId()));
            return customerDataTokens;
        } catch (Exception e) {
            log.warning("method getCustomerWebSafe() -> Error -> " + e.getMessage());
            throw new ConflictException("Ocurrio un error.");
        }
    }

    @ApiMethod(name = "getIdStoreGroupByEmail", path = "/customerEndpoint/getIdStoreGroup", httpMethod = ApiMethod.HttpMethod.POST)
    public IdStoreGroupForTemplate getIdStoreGroupByEmail(EmailFromTemplate emailFromTemplate) throws ConflictException {
        CustomerResponse customerResponse = null;
        try {
            customerResponse = ApiGatewayService.get().getCustomerByEmail(emailFromTemplate.getEmail().toLowerCase());
            Optional<CustomerJSON> customerJSON = ApiGatewayService.get().getCustomerById(customerResponse.getId());
            if (customerJSON.isPresent()) {
                return new IdStoreGroupForTemplate(customerJSON.get().getIdStoreGroup());
            } else {
                return new IdStoreGroupForTemplate(26);
            }
        } catch (Exception e) {
            log.warning("method getIdStoreGroupByEmail() -> Error -> " + e.getMessage());
            throw new ConflictException("Ocurrio un error.");
        }
    }

    private CustomerNewLoginDataRes addCallLoginCodeMethod(CustomerDataLoginRes customer) {
        CustomerNewLoginDataRes customerNewLoginDataRes = new CustomerNewLoginDataRes();
        customerNewLoginDataRes.setType("call");
        customer.getList().forEach(data -> {
            if(data.getType().equals("phone")) {
                customerNewLoginDataRes.setData(data.getData());
            }
        });
        return customerNewLoginDataRes;
    }

    private CustomerNewLoginDataRes addWhatsappLoginCodeMethod(CustomerDataLoginRes customer) {
        CustomerNewLoginDataRes customerNewLoginDataRes = new CustomerNewLoginDataRes();
        customerNewLoginDataRes.setType("whatsapp");
        customer.getList().forEach(data -> {
            if(data.getType().equals("phone")) {
                customerNewLoginDataRes.setData(data.getData());
            }
        });
        return customerNewLoginDataRes;
    }


    @ApiMethod(name = "getCustomerLoginV3", path = "/customerEndpoint/getCustomerLoginV3", httpMethod = ApiMethod.HttpMethod.POST)
    public CustomerNewLoginListRes getCustomerLoginV3(final CustomerNewLoginReq request, final HttpServletRequest req) throws ConflictException, NotFoundException {

        if (Objects.isNull(request))
            throw new ConflictException(Constants.CUSTOMER_INITIALIZATION);

        RequestSourceEnum source = ftdUtilities.getSourceFromRequestHeader(req);

        CustomerNewLoginListRes response = new CustomerNewLoginListRes();
        List<CustomerNewLoginRes> listCustomers = new ArrayList<>();
        List<CustomerDataLoginRes> responseCustomerLogin = new ArrayList<>();

        listCustomers = ApiGatewayService.get().getDataForLogin(request);

        if (Objects.isNull(listCustomers) || listCustomers.isEmpty())
            throw new ConflictException(Constants.USER_NOT_FOUND_TO_PARAMETER);

        if (Objects.nonNull(listCustomers) || !listCustomers.isEmpty()) {

            listCustomers.forEach(customerNewLoginRes -> {

                if (customerNewLoginRes.getOrigin().equals("EMAIL")) {
                    CustomerDataLoginRes customer;
                    customer = buildCustomerDataLoginRes(customerNewLoginRes);
                    log.info("customer: " + customer);
                    try {
                        CustomerNewLoginDataRes customerNewLoginDataRes = addCallLoginCodeMethod(customer);
                        CustomerNewLoginDataRes customerNewLoginDataResWhatsapp = addWhatsappLoginCodeMethod(customer);
                        customer.getList().add(customerNewLoginDataRes);
                        customer.getList().add(customerNewLoginDataResWhatsapp);
                    } catch (Exception e) {
                        log.warning("No se pudo agregar telefono");
                    }
                    if (Objects.nonNull(customer.getIdCustomerWebSafe()))
                        responseCustomerLogin.add(customer);
                }

                if (customerNewLoginRes.getOrigin().equals("GOOGLE")) {
                    CustomerDataLoginRes customer;
                    customer = getCustomerKeysGoogle(customerNewLoginRes);
                    try {
                        CustomerNewLoginDataRes customerNewLoginDataRes = addCallLoginCodeMethod(customer);
                        CustomerNewLoginDataRes customerNewLoginDataResWhatsapp = addWhatsappLoginCodeMethod(customer);
                        customer.getList().add(customerNewLoginDataRes);
                        customer.getList().add(customerNewLoginDataResWhatsapp);
                    } catch (Exception e) {
                        log.warning("No se pudo agregar telefono");
                    }
                    if (Objects.nonNull(customer.getIdCustomerWebSafe()))
                        responseCustomerLogin.add(customer);
                }

                if (customerNewLoginRes.getOrigin().equals("FACEBOOK")) {
                    CustomerDataLoginRes customer;
                    customer = getCustomerKeysFacebook(customerNewLoginRes);
                    try {
                        CustomerNewLoginDataRes customerNewLoginDataRes = addCallLoginCodeMethod(customer);
                        CustomerNewLoginDataRes customerNewLoginDataResWhatsapp = addWhatsappLoginCodeMethod(customer);
                        customer.getList().add(customerNewLoginDataRes);
                        customer.getList().add(customerNewLoginDataResWhatsapp);
                    } catch (Exception e) {
                        log.warning("No se pudo agregar telefono");
                    }
                    if (Objects.nonNull(customer.getIdCustomerWebSafe()))
                        responseCustomerLogin.add(customer);
                }

                if (customerNewLoginRes.getOrigin().equals("APPLE")) {
                    CustomerDataLoginRes customer;
                    customer = getCustomerKeysApple(customerNewLoginRes);
                    try {
                        CustomerNewLoginDataRes customerNewLoginDataRes = addCallLoginCodeMethod(customer);
                        CustomerNewLoginDataRes customerNewLoginDataResWhatsapp = addWhatsappLoginCodeMethod(customer);
                        customer.getList().add(customerNewLoginDataRes);
                        customer.getList().add(customerNewLoginDataResWhatsapp);
                    } catch (Exception e) {
                        log.warning("No se pudo agregar telefono");
                    }
                    if (Objects.nonNull(customer.getIdCustomerWebSafe()))
                        responseCustomerLogin.add(customer);
                }

                if (customerNewLoginRes.getOrigin().equals("CALLCENTER")) {
                    CustomerDataLoginRes customer;
                    customer = buildCustomerDataLoginRes(customerNewLoginRes);
                    try {
                        CustomerNewLoginDataRes customerNewLoginDataRes = addCallLoginCodeMethod(customer);
                        CustomerNewLoginDataRes customerNewLoginDataResWhatsapp = addWhatsappLoginCodeMethod(customer);
                        customer.getList().add(customerNewLoginDataRes);
                        customer.getList().add(customerNewLoginDataResWhatsapp);
                    } catch (Exception e) {
                        log.warning("No se pudo agregar telefono");
                    }
                    if (Objects.nonNull(customer.getIdCustomerWebSafe()))
                        responseCustomerLogin.add(customer);
                }
            });
        }


        response.setCustomers(responseCustomerLogin);


        if (source.equals(RequestSourceEnum.ANDROID) || source.equals(RequestSourceEnum.IOS)){
            return response;
        }

        // validate feature-flag security

        boolean webSecurityIsEnabled = webSecurityIsEnabled();

        if (!webSecurityIsEnabled){
            return response;
        }

        //-- Security --

//        SE ELIMINA LOS TOKENS POR PROBLEMAS DE SEGURIDAD. !! POR AHORA SOLO EN WEB o
//        cualquier otro q no sea APPS
        clearSecurityData(responseCustomerLogin);

        return response;
    }

    @ApiMethod(name = "loginSelfCheckout", path = "/customerEndpoint/login/selfCheckout", httpMethod = ApiMethod.HttpMethod.POST)
    public CustomerJSON loginPostSelfCheckout(final SelfCheckout selfCheckout, final HttpServletRequest request)
            throws UnauthorizedException, BadRequestException, ConflictException, IOException, InternalServerErrorException, NotFoundException {
        return loginMethodSelfCheckout(selfCheckout);
    }

    private CustomerJSON loginMethodSelfCheckout(final SelfCheckout selfCheckout)
            throws UnauthorizedException, BadRequestException, ConflictException, IOException, InternalServerErrorException, NotFoundException {
        //log.info("method: login()");

        if (selfCheckout.getDocumentNumber() == null) {
            throw new BadRequestException("El número de identidad es requerido.");
        }

        if (selfCheckout.getIdStore() == null) {
            throw new BadRequestException("El ID de tienda es requerido.");
        }

        if (selfCheckout.getIdBox() == null) {
            throw new BadRequestException("El ID de caja es requerido.");
        }
        CustomerJSON customerJSON = ApiGatewayService.get().customerLoginDocument(URLConnections.URL_CRM_CUSTOMER_LOGIN_DOCUMENT, selfCheckout);

        if (customerJSON.getEmail() != null && !customerJSON.getEmail().isEmpty()) {
            customerJSON.setAnalyticsUUID(ApiGatewayService.get().getUUIDFromBraze(customerJSON.getEmail()).orElse(null));
        }

        User user = null;
        Credential credential = null;
        Key<User> keyUser = null;

        if (customerJSON.getRegisteredBy().equals("EMAIL")){
            credential = getCredencial(customerJSON.getEmail().toLowerCase());
            if ( credential != null) {
                user = getUser(customerJSON.getId());
                if (user == null){
                    Customer customer = new Customer();
                    customer.setEmail(customerJSON.getEmail());
                    customer.setTokenFacebook(null);
                    customer.setTokenGoogle(null);
                    return createCustomerSelf(customer, customerJSON, false, customerJSON.getCity(), null, null, 26);
                } else {
                keyUser = Key.create(User.class, user.getIdUser());}
            } else {
                if (Objects.nonNull(customerJSON)) {
                    {
                        Customer customer = new Customer();
                        customer.setEmail(customerJSON.getEmail());
                        customer.setTokenFacebook(null);
                        customer.setTokenGoogle(null);
                        return createCustomerSelf(customer, customerJSON, false, customerJSON.getCity(), null, null, 26);
                    }
                }
                throw new BadRequestException(Constants.EMAIL_EXISTS);
            }
        } else {
            user = ofy().load().type(User.class).filter("id", customerJSON.getId()).first().now();
            keyUser = Key.create(User.class, user.getIdUser());
        }

        if (!user.getRole().equals("Customer"))
            throw new ConflictException(Constants.USER_TYPE_ERROR);

        // Succeeded in login security

        Token tokenTransport = generateToken();
        Token tokenClient = new Token();
        tokenClient.setRefreshToken(tokenTransport.getRefreshToken());
        tokenClient.setToken(tokenTransport.getToken());
        encryptToken(tokenClient);
        tokenClient.setTokenId(UUID.randomUUID().toString());
        tokenClient.setOwner(Ref.create(keyUser));
        tokenClient.setTokenExpDate(tokenTransport.getTokenExp());

        Key<Token> keyToken = ofy().save().entity(tokenClient).now();
        user.setIdUserWebSafe(keyUser.toWebSafeString());
        tokenTransport.setTokenIdWebSafe(keyToken.toWebSafeString());
        user.setToken(tokenTransport);
        user.setLastLogin(new Date().getTime());

        ofy().save().entity(user);

        customerJSON.setToken(tokenTransport);
        customerJSON.setIdCustomerWebSafe(user.getIdUserWebSafe());
        customerJSON.setBanners(null);
        customerJSON.setSuggestedProducts(null);
        customerJSON.setPreviousItems(null);
        customerJSON.setHighlightedItems(null);
        if (customerJSON.getAddresses() != null && !customerJSON.getAddresses().isEmpty()) {
            for (Address address : customerJSON.getAddresses()) {
                address.setLatitude(4.6730450);
                address.setLongitude(-74.0583310);
            }
        }
        int shoppingCartNumber = 0;
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(user).first().now();
        if (deliveryOrder != null) {
            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();
            if (deliveryOrderItemList != null) {
                for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                    shoppingCartNumber += deliveryOrderItem.getQuantitySold();
                }

                //shoppingCartNumber = deliveryOrderItemList.size();
                customerJSON.setShopingCartNumber(shoppingCartNumber);
            }
        }

        return customerJSON;
    }

    private Credential getCredencial(String email) {
        Credential credential = null;
        if (email != null){
            List<Credential> credentials = users.findUserByEmailList(email);
            for (Credential i : credentials ){
                User user = users.findUserByKey(i.getOwner().getKey());
                if (user == null){
                    users.deleteCredencialDataStore(i);
                }else {
                    credential = i;
                }
            }
        }
        return credential;
    }

    private User getUserAndDelete(Integer userId) {
        // Verifica si el userId existe
        if (userId == null) {
            return null;
        }
        // Busca los usuarios por userId en DS
        List<User> usersList = users.findUserByKeyList(userId);
        User lastNonDeletedUser = null;

        for (User user : usersList) {
            // genera data para el usuario para el datastore
            UUID userUUID = UUID.fromString(user.getIdUser());
            Key<User> userKey = Key.create(User.class, userUUID.toString());

            // Verifica si el usuario tiene credenciales asociadas.
            if (!hasCredentials(userKey)) {
                // Elimina el usuario si no tiene credenciales.
                users.deleteUserDataStore(user);
            } else {
                // Guarda el último usuario no eliminado.
                lastNonDeletedUser = user;
            }
        }

        return lastNonDeletedUser;
    }

    private boolean hasCredentials(Key<User> userKey) {
        Ref<User> userRef = Ref.create(userKey);
        Credential credential = users.findCredentialByKey(userRef);
        return credential != null;
    }

    private User getUser(Integer userId) {
        User userReturn = null;
        if (userId != null) {
            return users.findUserByIdCustomer(userId);
        }
        return userReturn;
    }


    @ApiMethod(name = "getCustomerPrimeSubscription", path = "/customerEndpoint/{customerId}/prime-subscription", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerPrimeSubscriptionDomainData getCustomerPrimeSubscription(@Named("customerId") Long customerId) throws IOException, BadRequestException {
        if (customerId == null) {
            throw new BadRequestException("BadRequest [request is null]");
        }
        return ApiGatewayService.get().getCustomerPrimeSubscription(customerId);
    }


    @ApiMethod(name = "postSubscriptionFreeDays", path = "/customerEndpoint/prime-subscription", httpMethod = ApiMethod.HttpMethod.POST)
    public GenericResponse postSubscriptionFreeDays (@RequestBody CustomerPrimeFreeDays request)
            throws BadRequestException, ConflictException, UnauthorizedException {

        if (request == null)
            throw new ConflictException(Constants.INVALID_TOKEN);
        final String token = request.getToken();
        final String tokenIdWebSafe = request.getTokenIdWebSafe();
        final String idCustomerWebSafe = request.getIdCustomerWebSafe();

        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);
//        log.info("user id-------------------> " + user.getId());
        try {
            GenericResponse response = ApiGatewayService.get().subscribePrimeFreeDays(user.getId(), request);
//            log.info("response-------------------> " + new Gson().toJson(response));
            return response;
        } catch (SocketTimeoutException e) {
            throw new ConflictException("SocketTimeoutException@SubcribePrimePromo -> " + e.getMessage());
        } catch (Exception e) {
            throw new ConflictException("Error@SubcribePrimePromo -> " + e.getMessage());
        }
    }

    private String obtainMinAmount() throws BadRequestException {
        Optional<DeliveryFree> deliveryFree = APIAlgolia.getFreeDelivery();
        if (!deliveryFree.isPresent()) {
            throw new BadRequestException("BadRequest [deliveryFree is null]");
        }
        if (deliveryFree.get().getCampaigns() == null || deliveryFree.get().getCampaigns().isEmpty()) {
            throw new BadRequestException("BadRequest [deliveryFree.campaigns is null or empty]");
        }
        for (CampaignFree campaignFree : deliveryFree.get().getCampaigns()) {
            if (validateCampaign(campaignFree)) {
                if(campaignFree.getVariables()==null || campaignFree.getVariables().isEmpty()){
                    throw new BadRequestException("BadRequest [deliveryFree.campaigns.variables is null or empty]");
                }
                for(VariablesFree variablesFree : campaignFree.getVariables()) {
                  if(!validateMinAmount(variablesFree).equalsIgnoreCase("")){
                      return validateMinAmount(variablesFree);
                  }
                }


            }
        }
        return null;
    }
    //validate campaing
    private boolean validateCampaign(CampaignFree campaign) {
        if (campaign == null) {
            return false;
        }
       if (campaign.getCombinationToApply() == null || campaign.getCombinationToApply().isEmpty()) {
           return false;
       }
       if (campaign.getVariables() == null || campaign.getVariables().isEmpty()) {
           return false;
       }
        return campaign.getCombinationToApply().trim().equalsIgnoreCase("FREE_DELIVERY_FOR_CUSTOMER_PRIME");
    }

    private String validateMinAmount(VariablesFree variables) {
        if (variables == null) {
            log.info("variables is null");
            return "";
        }
        if (variables.getKey() == null || variables.getKey().isEmpty()) {
            log.info("variables.key is null");
            return "";
        }
        if (variables.getValues() == null || variables.getValues().isEmpty()) {
            log.info("variables.values is null");
            return "";
        }
        if(variables.getKey().trim().equalsIgnoreCase("MIN_AMOUNT")){
            if(variables.getValues().get(0) != null && !variables.getValues().get(0).isEmpty()){
//                log.info("variables.values.get(0) = " + variables.getValues().get(0));
                return variables.getValues().get(0);
            }
        }
        return "";
    }
    private void addNewDynamicSession(List<DynamicSection> dynamicSections, CustomerResponseCart customerResponseCart)  {
        if (customerResponseCart != null ) {
            if(customerResponseCart.getSavingUserPrime() < 4000){
                customerResponseCart.setSavingUserPrime(0);
            }
            isPrimeBannerDynamic(dynamicSections,customerResponseCart.isActive());
            Optional<DynamicSection> dynamicSection = dynamicSections.stream().filter(dynamicSection1 ->
                    Objects.equals(dynamicSection1.getComponentType(), ComponentTypeEnum.PRIME_SAVINGS)).findFirst();
            dynamicSection.ifPresent(section -> section.setPrime(customerResponseCart.isActive()));
            dynamicSection.ifPresent(section -> section.setTotal_saved( customerResponseCart.getSavingUserPrime()));
        }
    }


    private void isPrimeBannerDynamicSection(List<DynamicSection> dynamicSectionList, boolean isPrime) {
        if (dynamicSectionList != null) {
            if (isPrime) {
                dynamicSectionList.removeIf(dynamicSection ->
                        dynamicSection.getLabel() != null &&
                                dynamicSection.getLabel().equals("OLD_BANNER_NOT_PRIME"));
            } else {
                dynamicSectionList.removeIf(dynamicSection ->
                        dynamicSection.getLabel() != null &&
                                dynamicSection.getLabel().equals("OLD_BANNER_PRIME"));
            }
        }

    }
    private void isPrimeBannerDynamic(List<DynamicSection> dynamicSectionList, boolean isPrime) {

        if (dynamicSectionList != null) {
            if (isPrime) {
                dynamicSectionList.removeIf(dynamicSection ->
                        dynamicSection.getUserType()!= null &&
                                dynamicSection.getUserType().equals("USER_NOT_PRIME"));
            } else {
                dynamicSectionList.removeIf(dynamicSection ->
                        dynamicSection.getUserType() != null &&
                                dynamicSection.getUserType().equals("USER_PRIME"));
            }
        }

    }


    private boolean isActiveAlgolia() {
        Optional<BannerPrimeConfig> bannerPrimeConfig = APIAlgolia.getBannerPrimeConfig();

        if (!bannerPrimeConfig.isPresent()) {
            return false;
        }
        return bannerPrimeConfig.get().isEnable();
    }

    private CustomerResponseCart isCustomerPrime(int userId)  {
        CustomerResponseCart customerResponseCart = new CustomerResponseCart();
        if(userId == 0){
            customerResponseCart.setActive(false);
            customerResponseCart.setSavingUserPrime(0);
            return customerResponseCart;
        }
        try {
            customerResponseCart= ApiGatewayService.get().getCustomerCreditCardPrimeData((long) userId);
//            log.info("JSOnResponse"+new Gson().toJson(customerResponseCart));
            return customerResponseCart;
        } catch (Exception e) {
            log.warning("Error CUSTOMER PRIME  " + e);
        }

        return customerResponseCart;
    }
    private void SavingCustomerNoPrime(CustomerResponseCart customer,int userId){
        if(!customer.isActive()&&userId!=0){
            SavingsPrimeGeneral savingPrimeGeneral = ofy().load().type(SavingsPrimeGeneral.class).id(userId).now();
            if(savingPrimeGeneral!=null){
                customer.setSavingUserPrime(savingPrimeGeneral.getPrimeSaving());
            }
        }
    }


    private List<SuggestedObject> getSuggestedPersonalizedInOffer(HomeInfoConfig homeInfoConfig) throws ConflictException, UnauthorizedException, BadRequestException, InternalServerErrorException, ServiceUnavailableException {
        try {
            Optional<RecommendConfig> recommendConfig = APIAlgolia.getAlgoliaRecommendConfig();
            if(recommendConfig.isPresent() && recommendConfig.get().isSuggestedItemsFlag()) {
                if (Objects.nonNull(homeInfoConfig.getHomeRequest()) && Objects.nonNull(homeInfoConfig.getHomeRequest().getToken()) && Objects.nonNull(homeInfoConfig.getHomeRequest().getTokenIdWebSafe()) && Objects.nonNull(homeInfoConfig.getHomeRequest().getIdCustomerWebSafe())) {
//                    Key<Customer> customerKey = Key.create(homeInfoConfig.getHomeRequest().getIdCustomerWebSafe());
//                    List<Favorite> favorites = ofy().load().type(Favorite.class).filter("customerKey", customerKey).list();
//                    int storeIdGroup = homeInfoConfig.getIdStoreGroup() > 0 ? homeInfoConfig.getIdStoreGroup() : 26;
                    List<ItemAlgolia> itemAlgoliaList = homeInfoConfig.getCarrouselItemListAsync().getFavorites()
                            .stream().filter(i -> Objects.nonNull(i) && Objects.nonNull(i.getOfferPrice()) && i.getOfferPrice() > 0)
                            .collect(Collectors.toList());
                    CustomerOnlyData customer = homeInfoConfig.getCustomerOnlyData();
                    if (customer != null && customer.getId() > 0) {
                        Optional<AlgoliaReminder> algoliaReminders = APIAlgolia.getRemindersByUserId(customer.getId().toString());
                        List<ItemAlgolia> itemAlgoliaListReminders = algoliaReminders.isPresent() ? APIAlgolia.getItemListAlgoliaFromStringList(algoliaReminders.get().getReminders().stream()
                                .filter(i -> i.getExpiration().after(Calendar.getInstance().getTime()))
                                .map(i -> String.valueOf(i.getItemId()) + String.valueOf(homeInfoConfig.getIdStoreGroup())).collect(Collectors.toList())) : new ArrayList<>();
                        itemAlgoliaList.addAll(itemAlgoliaListReminders);
                    }
                    if (customer != null && customer.getId() > 0) {
                        List<ItemAlgolia> itemsViewed = !homeInfoConfig.getCarrouselItemListAsync().getViewed().isEmpty() ?
                                homeInfoConfig.getCarrouselItemListAsync().getViewed().stream()
                                .filter(i -> Objects.nonNull(i) && Objects.nonNull(i.getOfferPrice()) && i.getOfferPrice() > 0)
                                .collect(Collectors.toList()) : new ArrayList<>();
//                        log.info("Items viewed are " + itemsViewed.size());
                        itemAlgoliaList.addAll(itemsViewed);
                    }
                    if(customer != null && Objects.nonNull(customer.getPurchases()) && !customer.getPurchases().isEmpty()) {
                        List<ItemAlgolia> itemsPurchased = new ArrayList<>();
                        try {
                            itemsPurchased = homeInfoConfig.getCarrouselItemListAsync().getPurchases().stream()
                                    .filter(i -> Objects.nonNull(i) && Objects.nonNull(i.getOfferPrice()) && i.getOfferPrice() > 0)
                                    .collect(Collectors.toList());
                        } catch (Exception e) {
                            itemsPurchased = new ArrayList<>();
                        }
                        itemAlgoliaList.addAll(itemsPurchased);
                    }


                    final int[] n = {0};
                    HashSet<String> idsSuggested = new HashSet<>();
                    return itemAlgoliaList.stream()
                        .filter(item -> Objects.nonNull(item.getTotalStock()) && item.getTotalStock() > 10)
                        .filter(item -> idsSuggested.add(item.getId())).map(ia -> {
                        SuggestedObject suggestedObject = new SuggestedObject();
                        suggestedObject.setStartDate(ia.getOfferStartDate());
                        suggestedObject.setEndDate(ia.getOfferEndDate());
                        suggestedObject.setId(ia.getId());
                        suggestedObject.setOfferText("Aprovecha!");
                        suggestedObject.setType("UNIQUE");
                        suggestedObject.setPosition(n[0]);
                        suggestedObject.setProduct(Arrays.asList(APIAlgolia.getItemToItemAlgolia(new Item(), ia)));
                        n[0] = n[0] + 1;
                        return suggestedObject;
                    }).limit(6).collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            // Ocurrio un error
        }
        return new ArrayList<>();
    }
    //Micro charge
    @ApiMethod(name = "validAntifraud", path = "/customerEndpoint/antifraud/validate/", httpMethod = ApiMethod.HttpMethod.GET)
    public GenericResponse<Boolean> validAntifraud(@Named("token") final String token,
                                  @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                  @Named("idCustomerWebSafe") final String idCustomerWebSafe, @Named("numberCard") Long numberCard,
                                                   final HttpServletRequest request)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException {
        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);
//        log.info("user id-------------------> " + user.getId());
        try {
            String source = request.getHeader("source");
            return ApiGatewayService.get().validAntifraud((long)(user.getId()),numberCard,source);
        } catch (SocketTimeoutException e) {
            throw new ConflictException("SocketTimeoutException@creditCard -> " + e.getMessage());
        } catch (Exception e) {
            throw new ConflictException("Error@creditCard -> " + e.getMessage());
        }
    }

    @ApiMethod(name = "generateMicroCharge", path = "/customerEndpoint/micro-charge/generate", httpMethod = ApiMethod.HttpMethod.POST)
    public GenericResponse<PayMicroCharge> generateMicroCharge(@Named("token") final String token,
                                                   @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                                   @Named("idCustomerWebSafe") final String idCustomerWebSafe, MicroCharge microCharge)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException {
        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);
//        log.info("user id-------------------> " + user.getId());
        try {
            return ApiGatewayService.get().generateMicroCharge(microCharge);
        } catch (SocketTimeoutException e) {
            throw new ConflictException("SocketTimeoutException@creditCard -> " + e.getMessage());
        } catch (Exception e) {
            throw new ConflictException("Error@creditCard -> " + e.getMessage());
        }
    }

    @ApiMethod(name = "validateMicroCharge", path = "/customerEndpoint/micro-charge/validate", httpMethod = ApiMethod.HttpMethod.POST)
    public GenericResponse<Boolean> validateMicroCharge(@Named("token") final String token,
                                       @Named("tokenIdWebSafe") final String tokenIdWebSafe,
                                       @Named("idCustomerWebSafe") final String idCustomerWebSafe, MicroCharge microCharge)
            throws UnauthorizedException, BadRequestException,
            ConflictException, IOException {
        if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
                (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
            throw new ConflictException(Constants.INVALID_TOKEN);
        if (!authenticate.isValidToken(token, tokenIdWebSafe))
            throw new ConflictException(Constants.INVALID_TOKEN);

        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new UnauthorizedException(Constants.USER_NOT_FOUND);
//        log.info("user id-------------------> " + user.getId());
        try {
            return ApiGatewayService.get().validateMicroCharge(microCharge);
        } catch (SocketTimeoutException e) {
            throw new ConflictException("SocketTimeoutException@creditCard -> " + e.getMessage());
        } catch (Exception e) {
            throw new ConflictException("Error@creditCard -> " + e.getMessage());
        }
    }

    @ApiMethod(name = "getValidationTokenPhoneV2", path = "/customerEndpoint/getValidationTokenPhoneV2", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer getValidationTokenPhoneV2(@Named("call") @Nullable String byCall, final SendCondeLoginReq request) throws ConflictException {

        // Generacion del codigo
        final String code = Integer.toString((int) (Math.random() * 9000) + 1000);

        if (Objects.isNull(request.getPhone()))
            throw new ConflictException("Phone is required");

//        log.info(byCall);
        FirebaseService.get().notifyNewCodeLoginV2(new NotifyCodeLogin("register_"+String.valueOf(request.getPhone()), code));

        try {


            if( Objects.nonNull(byCall) && !byCall.isEmpty() && (byCall.equals("true") || byCall.equals("TRUE")) ) {

                SendSMSCloudFunctionReq req = new SendSMSCloudFunctionReq();
                req.setTo(String.valueOf(request.getPhone()));
                req.setBody(code);
                CloudFunctionsService.get().postSendCodeByCall(req);

            } else {
                final String finalMessage = msgUtilities.obtainMsgAlgolia(MsgSmsEnum.MESSAGE_VALIDATION_TOKEN_PHONE).replace("{CODE}", code);
                final SendSMSCloudFunctionReq requestCLF = new SendSMSCloudFunctionReq(String.valueOf(request.getPhone()), finalMessage);
                CloudFunctionsService.get().postSendSms(requestCLF);

                // Envio Whatsapp
                try {
                    SendWhatsappCloudFunctionReq requestW = new SendWhatsappCloudFunctionReq(String.valueOf(request.getPhone()), finalMessage);
                    CloudFunctionsService.get().postSendWhatsappV2(requestW);
                } catch (Exception e) {
                    log.warning("El api de whatsappV2 tiene problemas. Mensaje: " + (e.getMessage() != null ? e.getMessage() : ""));
                }
            }

            // Envio SMS

        } catch (Exception e) {
            log.warning("ERROR enviando SMS o Whatsapp. Mensaje: " + e.getMessage());
        }
        Answer answer = new Answer();
        answer.setConfirmation(true);
        answer.setTokenFarmatodo("Sent successfully");
        return answer;
    }


    @ApiMethod(name = "getValidationTokenPhoneV3", path = "/customerEndpoint/v2/getValidationTokenPhoneV2", httpMethod = ApiMethod.HttpMethod.POST)
    public Answer getValidationTokenPhoneV3(final SendCondeLoginReq request) throws ConflictException {

        Answer answer = new Answer();
        answer.setConfirmation(false);
        // Generacion del codigo
        final String code = Integer.toString((int) (Math.random() * 9000) + 1000);

        if (Objects.isNull(request.getPhone()))
            throw new ConflictException("Phone is required");

        FirebaseService.get().notifyNewCodeLoginV2(new NotifyCodeLogin("register_" + String.valueOf(request.getPhone()), code));

        if (Objects.nonNull(request.getByWhatsapp()) && request.getByWhatsapp().equals(Boolean.TRUE)) {
            try {
                if (Objects.nonNull(request.getPhone())) {
                    //log.info("Envio de codigo por Whatsapp");
                    WhatsAapSendMessageConfig whatsaapSendMessageConfig = getWhatsappConfigMessage();
                    if (Objects.nonNull(whatsaapSendMessageConfig) && Objects.nonNull(whatsaapSendMessageConfig.getTemplateNameLogin())
                            && Objects.nonNull(whatsaapSendMessageConfig.getTemplateNamespace())){

                        SendWhatsappCloudFunctionCodeReq req = new SendWhatsappCloudFunctionCodeReq();

                        String phone = String.valueOf(request.getPhone());
                        String countryCode = phone.substring(0, 2);
                        if (countryCode.equals("58") && Objects.equals(String.valueOf(phone.charAt(2)), "0")) {
                            phone = phone.replaceFirst("0", "");
                        }
                        String phonePrefix = "+" + phone;
                        req.setPhone(phonePrefix);
                        req.setCode(code);
                        req.setTemplateName(whatsaapSendMessageConfig.getTemplateNameLogin());
                        req.setTemplateNamespace(whatsaapSendMessageConfig.getTemplateNamespace());
                        //log.info("req whtasapp: " + req);
                        SendWhatsappCloudFunctionCodeRes responseCloud = CloudFunctionsService.get().sendWhatsappCode(req);

                        log.info("method getValidationTokenPhoneV3: response cloudF: " + responseCloud.toString());
                    }
                    answer.setConfirmation(true);
                    answer.setMessage("success");
                }
            } catch (Exception e) {
                log.warning("method getValidationTokenPhoneV3: Error to send Whatsapp: " + e.getMessage());
                return answer;
            }
        }
        if (Objects.nonNull(request.getByPhone()) && request.getByPhone().equals(Boolean.TRUE)) {
            try {
                if (Objects.nonNull(request.getPhone())) {

                    String phone = String.valueOf(request.getPhone());
                    String countryCode = phone.substring(0, 2);
                    if (countryCode.equals("58") && Objects.equals(String.valueOf(phone.charAt(2)), "0")) {
                        phone = phone.replaceFirst("0", "");
                    }
                    if (request.getRegister() == null || request.getRegister()) {
                        final String finalMessage = msgUtilities.obtainMsgAlgolia(MsgSmsEnum.MESSAGE_VALIDATION_TOKEN_PHONE).replace("{CODE}", code);
                        final SendSMSCloudFunctionReq requestCLF = new SendSMSCloudFunctionReq(phone, finalMessage);
                        CloudFunctionsService.get().postSendSms(requestCLF);
                        answer.setConfirmation(true);
                        answer.setMessage("success");
                    } else if (!request.getRegister()) {
                        final String finalMessage = msgUtilities.obtainMsgAlgolia(MsgSmsEnum.MESSAGE_CHANGE_NUMBER_PHONE).replace("{CODE}", code);
                        final SendSMSCloudFunctionReq requestCLF = new SendSMSCloudFunctionReq(phone, finalMessage);
                        CloudFunctionsService.get().postSendSms(requestCLF);
                        answer.setConfirmation(true);
                        answer.setMessage("success");
                    }
                }
            }catch(Exception e){
                log.warning("method getValidationTokenPhoneV3: Error to send SMS:" + e.getMessage());
                return answer;
            }

        }
        if (Objects.nonNull(request.getByCall()) && request.getByCall().equals(Boolean.TRUE)) {
            try {
                if (Objects.nonNull(request.getPhone())) {
                    SendSMSCloudFunctionReq req = new SendSMSCloudFunctionReq();
                    req.setTo(String.valueOf(request.getPhone()));
                    req.setBody(code);
                    CloudFunctionsService.get().postSendCodeByCall(req);
                    answer.setConfirmation(true);
                    answer.setMessage("success");
                }
            }catch(Exception e){
                log.warning("method getValidationTokenPhoneV3: Error to send SMS:" + e.getMessage());
                return answer;
            }
        }

        return answer;
        }

    @ApiMethod(name = "validateCodeRegister", path = "/customerEndpoint/validateCodeRegister", httpMethod = ApiMethod.HttpMethod.POST)
    public ValidateCodeLoginResponse validateCodeRegister(final ValidateCodeLoginReq request) throws ConflictException {
//        log.info("Number -> " + (Objects.nonNull(request.getPhone()) ? request.getPhone() : "NoNumber") + " Codigo Enviado -> " + (Objects.nonNull(request.getCode()) ? request.getCode() : "NoNumber") );
        ValidateCodeLoginResponse response = new ValidateCodeLoginResponse();
        response.setConfirmation(false);
        if (Objects.isNull(request)) {
            response.setMessage("request is null");
            return response;
        } else if (Objects.isNull(request.getCode())) {
            response.setMessage("request.code is null");
            return response;
        } else if (Objects.isNull(request.getPhone())) {
            response.setMessage("request.phone is null");
            return response;
        }
        String msgException = "Has superado los intentos permitidos, intenta de nuevo en 5 minutos.";
        try {
            final String code = request.getCode();
//            final String idCustomer = request.getIdCustomer();
            final String codeFirebase = FirebaseService.get().getLoginCodeV2(code, "register_" + request.getPhone());
            if (CachedDataManager.checkTriesLoginAndRegisterInCache("register_" + request.getPhone())) {
                response.setConfirmation(false);
                response.setMessage(msgException);
//                log.info("Se supero numero de intentos");
                return response;
            }

            if (code.equals(codeFirebase)) {
                response.setConfirmation(true);
                response.setMessage("The code is valid");
                FirebaseService.get().deleteLoginCodeV2("register_" + request.getPhone());

//                CustomerDataLoginRes customerDataTokens = getCustomerKeysFromId(idCustomer);
//
//                response.setCustomerData(customerDataTokens);

            } else {
                response.setMessage("The code isn't valid");
            }
            return response;
        } catch (Exception e) {
            log.warning("method dataNewLogin() -> Error -> " + e.getMessage());
            throw new ConflictException(e.getMessage().equals(msgException) ? msgException : "Ocurrio un error.");
        }
    }
    private void addNonStockItemInBraze(Integer itemId, String store, int userId) {

        if (itemId == null || itemId == 0 || store == null || store.isEmpty() || userId == 0) {
            return;
        }
        final String objectID = itemId + store;
        ItemAlgolia itemFromAlgolia = APIAlgolia.getItemAlgolia(objectID);
        if (itemFromAlgolia == null || itemFromAlgolia.getMediaDescription() == null || itemFromAlgolia.getMediaDescription().isEmpty()) {
            return;
        }

        if (itemFromAlgolia.getTotalStock() == 0 && !itemFromAlgolia.getMediaDescription().isEmpty()) {
            Optional<CustomerJSON> customerJSON = ApiGatewayService.get().getCustomerById(userId);
            if(customerJSON.isPresent() && customerJSON.get().getEmail() != null && !customerJSON.get().getEmail().isEmpty()) {
                ApiGatewayService.get().addNonStockItemInBraze(customerJSON.get().getEmail(), String.valueOf(itemId));
            }
        }
    }


    private void sendNonStockItemInBraze(Integer itemId, String store, String idCustomerWebSafe) {
        try{
            Key<User> userKey = Key.create(idCustomerWebSafe);
            User user = users.findUserByKey(userKey);

            if(user == null || user.getId() == 0){
                return;
            }
            addNonStockItemInBraze(itemId, store, user.getId());
        }  catch (Exception e) {
            log.warning("No se pudo agregar item sin stock en braze");
        }
    }

    private void generateCarrouselsAsync(HomeInfoConfig homeInfoConfig) throws ExecutionException, InterruptedException {
        CustomerOnlyData customerOnlyData = homeInfoConfig.getCustomerOnlyData();
        DatasourcesIds datasourcesIds = new DatasourcesIds();
        datasourcesIds.setPurchasesIds(homeInfoConfig.getCustomerOnlyData().getPurchases()
                .stream().map(p -> String.valueOf(p.getItem()) + String.valueOf(homeInfoConfig.getIdStoreGroup())).collect(Collectors.toList()));
        Optional<HistoryUser> optionalHistoryUser = APIAlgolia.getHistoryByUserId(customerOnlyData.getId().toString());
        if (optionalHistoryUser.isPresent() && Objects.requireNonNull(optionalHistoryUser.get().getItems()).size() > 0) {
            datasourcesIds.setViewedIds(
                    optionalHistoryUser.get().getItems().stream().map(i -> i.toString() + String.valueOf(homeInfoConfig.getIdStoreGroup())).collect(Collectors.toList())
            );
        }
        Key<Customer> customerKey = Key.create(homeInfoConfig.getHomeRequest().getIdCustomerWebSafe());
        List<Favorite> favorites = ofy().load().type(Favorite.class).filter("customerKey", customerKey).list();
        if(favorites!= null && !favorites.isEmpty()) {
            datasourcesIds.setFavoriteIds(
                    favorites.stream().map(f -> String.valueOf(f.getItemId())  + String.valueOf(homeInfoConfig.getIdStoreGroup())).collect(Collectors.toList())
                    );
        }
        externalDataAsync.getCarrouselsAsync(homeInfoConfig,datasourcesIds);
    }

    private void setMostSalesLabelPersonalization(Component sectionAlgolia, DynamicSection dynamicSectionAux, HomeInfoConfig homeInfoConfig) {
        Optional<RecommendConfig> config = APIAlgolia.getAlgoliaRecommendConfig();
        if(config.isPresent() && config.get().isDepartmentsAfinity()) {
            if ((Objects.equals(sectionAlgolia.getLabel(), "Belleza") || Objects.equals(sectionAlgolia.getLabel(), "Salud y medicamentos")) && Objects.nonNull(homeInfoConfig.getCustomerOnlyData()) && Objects.nonNull(homeInfoConfig.getCustomerOnlyData().getAnalyticsUUID())) {
                String departmentPersonalization = algoliaRecommendManager.getFavoriteDepartmentByLabel(homeInfoConfig.getCustomerOnlyData().getAnalyticsUUID(), sectionAlgolia.getLabel());
//                log.info("Department replaced " + dynamicSectionAux.getLabel() + " by " + departmentPersonalization);
                if (Objects.nonNull(dynamicSectionAux.getLabelWeb())) {
                    dynamicSectionAux.setLabel(departmentPersonalization);
                    dynamicSectionAux.setLabelWeb(getLabelWebByDepartment(departmentPersonalization));
                }
            }
        }
    }

    private String getLabelWebByDepartment(String department) {
        String defaultLabel = "<span style='border-bottom: 0.5rem solid {color}; position: absolute;'>{department}</span>";
        DepartmentColor departmentColor = new DepartmentColor(department);
        return defaultLabel.replace("{color}", departmentColor.getColor()).replace("{department}",department);
    }

    private void deleteBannerBelleza(DynamicResponse dynamicResponse) {
        if (dynamicResponse.getHomeSections() != null) {
            boolean isBelleza = dynamicResponse.getHomeSections().stream().anyMatch
                    (homeSection -> homeSection.getComponentType() != null && homeSection.getComponentType().getComponentType() != null && homeSection.getComponentType().getComponentType().equals("HTML_LABEL") &&
                            homeSection.getLabel() != null && homeSection.getLabel().equals("Belleza"));
            if (!isBelleza)
                dynamicResponse.getHomeSections().removeIf(homeSection -> homeSection.getComponentType() != null && homeSection.getComponentType().getComponentType() != null &&
                        (homeSection.getComponentType().getComponentType().equals("RECT_BANNER") || homeSection.getComponentType().getComponentType().equals("SHORTCUT_BANNER")));
        }
    }

    @ApiMethod(name = "deleteShoppingCartAndCoupon", path = "/customerEndpoint/customer/deleteShoppingCartAndCoupon", httpMethod = ApiMethod.HttpMethod.GET)
    public AnswerDeleteCart deleteShoppingCartAndCoupon(
            @Named("keyClient") final String keyClient,
            @Named("idCustomer") final int idCustomer
    ) throws UnauthorizedException {
        User user = users.findUserByIdCustomer(idCustomer);
        AnswerDeleteCart answer = new AnswerDeleteCart();
        if (Objects.nonNull(user)) {
            if (isCouponInCurrentCart(user)) {
                customerCouponManager.deleteCouponByCustomerID(idCustomer);
            }
            answer = deleteShoppingCart(keyClient, idCustomer);
        }
        return answer;
    }

    private boolean isCouponInCurrentCart(User user) {
        boolean result = false;
        Key<User> userKey = customerCouponManager.getKeyUser(user);
        DeliveryOrder deliveryOrder = ofy().load().type(DeliveryOrder.class).filter("currentStatus", 1).ancestor(Ref.create(userKey)).first().now();
        if (Objects.nonNull(deliveryOrder)) {
            List<DeliveryOrderItem> deliveryOrderItemList = ofy().load().type(DeliveryOrderItem.class).ancestor(deliveryOrder).list();

            if (Objects.nonNull(deliveryOrderItemList)) {
                for (DeliveryOrderItem deliveryOrderItem : deliveryOrderItemList) {
                    if (deliveryOrderItem.getCoupon() != null && deliveryOrderItem.getCoupon()) {
                        log.info("El cupon: " + deliveryOrderItem.getId() + " ya fue redimido en la orden: " + deliveryOrder.getIdDeliveryOrder());
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    @ApiMethod(name = "findCustomerDaneCodeCityByIdCity", path = "/customerEndpoint/findCustomerDaneCodeCityByIdCity/", httpMethod = ApiMethod.HttpMethod.GET)
    public GenericResponse<String> findCustomerDaneCodeCityByIdCity(
            @Named("token") final String token,
            @Named("tokenIdWebSafe") final String tokenIdWebSafe,
            @Named("idCustomerWebSafe") final String idCustomerWebSafe,
            @Named("idCity") final String idCity
    ) throws UnauthorizedException, ConflictException{

        try{
            log.info("CustomerEndpoint -> findCustomerDaneCodeCityByIdCity -> " + token + " " + tokenIdWebSafe + " " + idCustomerWebSafe + " " + idCity);
            if ((Objects.isNull(token) || Objects.isNull(tokenIdWebSafe) || Objects.isNull(idCustomerWebSafe)) ||
            (token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty()))
                throw new ConflictException(Constants.INVALID_TOKEN);
            if (!authenticate.isValidToken(token, tokenIdWebSafe))
                throw new ConflictException(Constants.INVALID_TOKEN);

            return ApiGatewayService.get().findCustomerDaneCodeCityByIdCity(idCity);
        }catch (Exception e){
            log.info("method findCustomerDaneCodeCityByIdCity() -> Error -> " + e.getMessage());
            throw new ConflictException(e.getMessage());
        }

    }


    @ApiMethod(name = "getCustomerPrimeSubscriptionSecure", path = "/customerEndpoint/v2/prime-subscription", httpMethod = ApiMethod.HttpMethod.GET)
    public CustomerPrimeSubscriptionDomainData getCustomerPrimeSubscriptionSecure(@Named("idCustomerWebSafe") String idCustomerWebSafe) throws IOException, BadRequestException, ConflictException {
        if(idCustomerWebSafe == null || idCustomerWebSafe.isEmpty())
            throw new BadRequestException("BadRequest [idCustomerWebSafe is not valid]");
        Key<User> userKey = Key.create(idCustomerWebSafe);
        User user = users.findUserByKey(userKey);
        if (user == null)
            throw new ConflictException(Constants.USER_NOT_FOUND);
        else

            return ApiGatewayService.get().getCustomerPrimeSubscription(Long.valueOf(user.getId()));
    }
}



