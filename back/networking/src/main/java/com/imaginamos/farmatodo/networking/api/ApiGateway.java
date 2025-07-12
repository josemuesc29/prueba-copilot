package com.imaginamos.farmatodo.networking.api;

import com.imaginamos.farmatodo.model.OptimalRoute.*;
import com.imaginamos.farmatodo.model.algolia.ItemAlgolia;
import com.imaginamos.farmatodo.model.algolia.RecommendResponse;
import com.imaginamos.farmatodo.model.braze.BrazeEventCreate;
import com.imaginamos.farmatodo.model.categories.Department;
import com.imaginamos.farmatodo.model.categories.Shortcut;
import com.imaginamos.farmatodo.model.city.CityJSON;
import com.imaginamos.farmatodo.model.coupon.CouponValidation;
import com.imaginamos.farmatodo.model.coupon.ValidFirstCouponResData;
import com.imaginamos.farmatodo.model.customer.SelfCheckout;
import com.imaginamos.farmatodo.model.customer.*;
import com.imaginamos.farmatodo.model.firebase.AddFirebaseCodeLoginRequest;
import com.imaginamos.farmatodo.model.firebase.FirebaseLoginCodeResponse;
import com.imaginamos.farmatodo.model.home.BannersDTFRes;
import com.imaginamos.farmatodo.model.item.ItemReq;
import com.imaginamos.farmatodo.model.item.ItemRes;
import com.imaginamos.farmatodo.model.microCharge.MicroCharge;
import com.imaginamos.farmatodo.model.offer.Offer;
import com.imaginamos.farmatodo.model.order.*;
import com.imaginamos.farmatodo.model.payment.OrderChargeRes;
import com.imaginamos.farmatodo.model.payment.PaymentMethodV2DTFRequest;
import com.imaginamos.farmatodo.model.payment.PaymentMethodV2FTDResponse;
import com.imaginamos.farmatodo.model.product.Highlight;
import com.imaginamos.farmatodo.model.product.Item;
import com.imaginamos.farmatodo.model.product.ItemCross;
import com.imaginamos.farmatodo.model.product.ItemStock;
import com.imaginamos.farmatodo.model.provider.ProviderCreate;
import com.imaginamos.farmatodo.model.store.Store;
import com.imaginamos.farmatodo.model.store.StoreJSON;
import com.imaginamos.farmatodo.model.talonone.CustomerSessionExternalRequest;
import com.imaginamos.farmatodo.model.talonone.DiscountTalon;
import com.imaginamos.farmatodo.model.user.GoogleAuth;
import com.imaginamos.farmatodo.networking.models.addresses.*;
import com.imaginamos.farmatodo.networking.models.amplitude.*;
import com.imaginamos.farmatodo.networking.models.authentication.LoginFirebaseReq;
import com.imaginamos.farmatodo.networking.models.authentication.LoginFirebaseRes;
import com.imaginamos.farmatodo.networking.models.braze.*;
import com.imaginamos.farmatodo.networking.models.mail.SendBrazeEmailResp;
import com.imaginamos.farmatodo.networking.models.mail.SendMailReq;
import com.imaginamos.farmatodo.networking.talonone.model.TrackEventItemPurchasedRequest;
import com.imaginamos.farmatodo.networking.talonone.model.TrackEventRequest;
import com.imaginamos.farmatodo.networking.talonone.model.TrackEventResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ApiGateway {

    @GET
    Call<DiscountSASByCustomerResponse> getDiscountByCustomer(@Url String url);

    @POST
    Call<CreateOrderResponseCore> createOrder(@Url String url, @Body CreateOrderRequestCore requestCore);


    @POST
    Call<CreateOrderResponseBackend3> createOrderBck3(@Url String url, @Body CreateOrderRequestCore requestCore, @Header("x-cloud-trace-context") String traceId);

    @GET
    Call<CustomerAddressResponse> getCustomerByAddressId(@Url String url);

    @GET
    Call<GetOrderStopsResponse> getOrderStops(@Url String url);

    @GET
    Call<GetUnbilledItemsByOrderResponse> getUnbilledItemsByOrder(@Url String url);

    @POST
    Call<OptimalRouteCheckoutOmsRes> getOptimalRouteInCheckoutOms(@Url String url, @Body OptimalRouteCheckoutOmsReq requestOms, @Header("x-cloud-trace-context") String traceId);

    @POST
    Call<OptimalRoutePopUpResponseDomain> getOptimalRoutePopUp(@Url String url, @Body OptimalRoutePopUpRequestDomain requestDomain, @Header("x-cloud-trace-context") String traceId);

    @POST
    Call<OrderChargeRes> chargeOrderOms(@Url String url, @Body OrderCharge requestOms);

    @POST
    Call<ValidateOrderBackend3> priceDeliveryOrder(@Url String url, @Body ValidateOrderReq requestCore, @Header("x-cloud-trace-context") String traceId);

    @GET
    Call<CustomerPhotoDataResponse> getCustomerPhotos(@Url String url);

    @GET
    Call<GetCustomerAndStoresCoordinatesByOrderResponse> getOrderCoordinatesByOrder(@Url String url);

    @POST
    Call<ValidateFreeDeliveryByCartResponse> validateFreeDeliveryByCart(@Url String url, @Body FreeDeliverySimpleCart cart);

    @POST
    Call<OrderRelationPrimeResponse> saveRelationOrderPrime(@Url String url, @Body OrderRelationPrimeRequest body);

    @GET
    Call<GetCustomerResponse<CustomerResponse>> getCustomerByEmail(@Url String url);

    @GET
    Call<GetCustomerResponse<ValidateCustomerEmail>> getCustomerByEmailLowerCase(@Url String url);

    @GET
    Call<GetOrderSumary> getSummaryByOrderId(@Url String url);

    @GET
    Call<GetLastStatusOrderProvider> getLastStatusOrderProvider(@Url String url);

    @GET
    Call<OrderInfoDataResponse> getTrackingOrder(@Url String url);

    @GET
    Call<RatingResponse> getRatings(@Url String url);

    @GET
    Call<ReadOrderResponseBackend3> getReadOrder(@Url String url);

    @GET
    Call<ValidateCustomerData> validateCustomerEmail(@Url String url);

    @GET
    Call<ValidateCustomerResponse> validateCustomerDocumentNumber(@Url String url);

    @GET
    Call<CustomerCreditCard> getAllCustomerCreditCard(@Url String url);

    @GET
    Call<PSEResponseCode> getAllPse(@Url String url);

    @POST
    Call<PaymentMethodV2FTDResponse> paymentMethodsV2(@Url String url, @Body PaymentMethodV2DTFRequest request);

    @GET
    Call<CreditCard> getAllCustomerCreditCardV2(@Url String url);

    @GET
    Call<UpdateEmailCustomerResponse> updateEmailCustomer(@Url String url, @Query("customerId") Long customerId, @Query("email") String email);

    @POST
    Call<ValidateStockRouteRes> validateStock(@Url String url, @Body ValidateStockRouteReq validateStockRouteReq);

    @POST
    Call<CustomersByIdResponse> getCustomerListByIds(@Url String url, @Body CustomerByIdRequest request);

    @POST
    Call<CustomerResetPasswordRes> customerResetPassword(@Url String url, @Body CustomerResetPasswordReq request);

    @PUT
    Call<CustomerResetPasswordRes> customerChangePassword(@Url String url, @Body CustomerResetPasswordReq request);

    @POST
    Call<CreateOrderSubscribeResponse> createOrderPAS(@Url String url, @Body CreateOrderSubscribeReq orderSubscribeReq);

    @GET
    Call<ValidateGeneralBool> validateCreditCardForDelete(@Url String url);

    @POST
    Call<ValidateCustomerPhoneResp> validateCustomerPhone(@Url String urlDtfPostValidateStock, @Body ValidateCustomerPhoneReq validateCustomerPhoneReq);

    @POST
    Call<AddressResponse> validateCustomerAddress(@Url String url, @Body CustomerAddresReq customerAddresReq);

    @PUT
    Call<ValidateGeneralBool> updateCustomerAddress(@Url String url, @Body CustomerAddresReq customerAddresReq);

    @GET
    Call<GetCustomerResponse<CustomerOnlyData>> getCustomerOnlyById(@Url String url);

    @GET
    Call<MessengerNameResponse> getMessengerNameByOrderId(@Url String url);

    @GET
    Call<GetCustomerResponse<AddressesRes>> getAddressByCustomerId(@Url String url);

    @POST
    Call<CustomerBackend3> createCustomerCallCenter(@Url String url, @Body CustomerRequest customerRequest, @Header("x-cloud-trace-context") String traceId);

    @POST
    Call<CustomerCallResponse> getCustomerCallCenter(@Url String url, @Body CustomerCallReq customerCallCenterReq);

    @POST
    Call<AddressResponse> createCustomerAddress(@Url String url, @Body CreateAddresReq createAddresReq, @Header("x-cloud-trace-context") String traceId);

    @PUT
    Call<AddressResponse> updateCustomerAddress(@Url String url, @Body CreateAddresReq createAddresReq);

    @POST
    Call<CustomerRes> createBasicCustomer(@Url String url, @Body CustomerRequest customerRequest, @Header("x-cloud-trace-context") String traceId);

    @GET
    Call<GetCustomerResponse<CustomerJSON>> getCustomer(@Url String url);

    @POST
    Call<GetCustomerResponse<CustomerJSON>> customerLoginEmail(@Url String url, @Body CustomerLoginReq customer, @Header("x-cloud-trace-context") String traceId);

    @POST
    Call<GetCustomerResponse<CustomerJSON>> customerLoginDocument(@Url String url, @Body SelfCheckout selfCheckout);

    @POST
    Call<GetCustomerResponse<CreditCard>> createCustomerCreditCar(@Url String url, @Body CreditCardReq creditCardReq, @Header("x-cloud-trace-context") String traceId);

    @DELETE
    Call<CustomerAddressResponse> deleteCustomerAddressById(@Url String url);

    @GET
    Call<GetCustomerPrimeCartResponse> getCustomerPrimeCart(@Url String url);

    @GET
    Call<CustomerPrimeSubscriptionDomainRes> getCustomerPrimeSubscription(@Url String url);

    @PUT
    Call<GenericResponse> updateCustomerSaving(@Url String url, @Body SavingCustomer savingCustomer);


    @GET
    Call<ValidFirstCouponResData> validFirstCoupon(@Url String url);

    @DELETE
    Call<CustomerCreditCard> deleteCreditCardByIdAndCustomerId(@Url String url);

    @GET
    Call<CustomerResult<CustomerCreditCardToken>> tokenCreditCardByIdAndCustomerId(@Url String url);

    @GET
    Call<CustomerResult<CustomerCreditCardGateway>> gatewayActive(@Url String url);
    @PUT
    Call<CustomerCreditCard> defaultCreditCard(@Url String url, @Body CreditCardDefaultReq req, @Header("x-cloud-trace-context") String traceId);

    @POST
    Call<GetCustomerResponse<CustomerJSON>> handleCustomerLifeMiles(@Url String url, @Body CustomerLifeMilesReq req);

    @POST
    Call<GetCustomerResponse<CustomerLifeMileJSON>> customerLifeMiles(@Url String url, @Body CustomerLifeMilesReq req);

    @POST
    Call<GetCustomerResponse<CustomerLifeMileJSON>> inactiveCustomerLifeMiles(@Url String url, @Body CustomerLifeMilesReq req);

    @POST
    Call<GetCustomerResponse<AnswerGetUserOrigin>> getOrigin(@Url String url, @Body CustomerOriginReq request);

    @PUT
    Call<CustomerRes> updateCustomer(@Url String url, @Body CustomerRequest request);

    @GET
    Call<GetCustomerResponse<List<SuggestedObject>>> getSuggested(@Url String url);

    @POST
    Call<GetCustomerResponse<CustomerJSON>> getCustomerMonitor(@Url String url, @Body CustomerData request);

    @POST
    Call<GetCustomerResponse> deleteLogicCustomer(@Url String url);

    @PUT
    Call<OrderEditRes> putEditOrder(@Url String url, @Body OrderEdit orderEdit);
    @PUT
    Call<OrderCourier> putEditOrderCourier(@Url String url, @Body OrderEdit orderEdit);

    @GET
    Call<OrderInfoStatus> getOrderTrackingInfo(@Url String url);

    @GET
    Call<Bck3EventResponse> pingRMS(@Url String url);

    @POST
    Call<Bck3EventResponse> createFulfilOrdColDesc(@Url String url, @Body FulfilOrdColDescDomain request);

    @GET
    Call<Bck3EventResponse> pingSIM(@Url String url);

    @PUT
    Call<GetCustomerResponse> orderQualify(@Url String url, @Body Qualification qualification);

    @GET
    Call<GenericResponse<List<CourierRes>>> getCourierAll(@Url String url);

    @PUT
    Call<GenericResponse> putOrderStatusUpdate(@Url String url, @Body DeliveryOrderStatus deliveryOrderStatus);

    @PUT
    Call<GenericResponse> putOrderPickingDateUpdate(@Url String url, @Body UpdatePickingDateReq updatePickingDateReq);

    @POST
    Call<GenericResponse> postOrderProviderStatusUpdate(@Url String url, @Header("token") String token, @Body OrderProviderStatus orderProviderStatus);

    @GET
    Call<GenericResponse<List<Object>>> getOrderProvider(@Url String url, @Header("token") String token);

    @POST
    Call<GenericResponse> orderProviderStockUpdate(@Url String url, @Header("token") String token, @Body ItemStock itemStock);

    @POST
    Call<GenericResponse> cancelOrderToCourier(@Url String url, @Body SendOrder sendOrder);

    @GET
    Call<GenericResponse<List<PaymentMethodRes>>> getPaymentMethodActive(@Url String url);

    @PUT
    Call<GenericResponse> updateOrderPaymentMethod(@Url String url, @Body DeliveryOrderStatus orderRequest);

    @PUT
    Call<GenericResponse> sendMailSubscribeAndSaveCendisBack3(@Url String url);

    @GET
    Call<OrderInfoDataResponseMonitor> getOrderInfoTracingBck3(@Url String url);

    @POST
    Call<CustomerLoginFinalRes> getCustomers(@Url String url, @Body CustomerNewLoginReq customerNewLoginReq);

    @POST
    Call<SendMailCodeLoginRes> sendMailCode(@Url String url, @Body SendMailCodeLoginReq sendMailCodeLoginReq);

    @GET
    Call<CustomerPhoneNumberRes> customerPhoneNumber(@Url String url);

    @GET
    Call<ReadOrderResponseBackend3> getActiveOrder(@Url String url);

    @GET
    Call<GenericResponse<List<StoreJSON>>> getStoreActive(@Url String url);

    @GET
    Call<GenericResponse<List<CityJSON>>> getCityActive(@Url String url);

    @POST
    Call<GenericResponse<List<Item>>> postGetItems(@Url String url, @Body ItemReq itemReq);

    @POST
    Call<GenericResponse<ItemRes>> postItemStart(@Url String url, @Body ItemReq itemReq);

    @GET
    Call<GenericResponse<List<Department>>> getCategoryActive(@Url String url);

    @GET
    Call<GenericResponse<List<ItemCross>>> getCrossSales(@Url String url);

    @GET
    Call<GenericResponse<List<Highlight>>> getHighlightActive(@Url String url);

    @GET
    Call<GenericResponse<List<Offer>>> getOfferActive(@Url String url);

    @GET
    Call<GenericResponse<Store>> getStoreCloserCoordinates(@Url String url);

    @GET
    Call<GenericResponse<List<Shortcut>>> getShortcutActive(@Url String url);

    @PUT
    Call<GenericResponse<ClientResponse>> getTokenByClientIdAndClientSecret(@Url String url, @Header("client_id") String client_id, @Header("client_secret") String client_secret);

    @POST
    Call<GenericResponse<ClientResponse>> postProviderCreate(@Url String url, @Body ProviderCreate providerCreate);

    @POST
    Call<GenericResponse<Item>> getItemByBarcodeAndStore(@Url String url, @Body ItemReq itemReq);

    @GET
    Call<GenericResponse<Item>> getItemById(@Url String url);

    @GET
    Call<GenericResponse<OrderQuantityItemResp>> getQuantityItemsByIdOrder(@Url String url);

    @GET
    Call<GenericResponse<List<ItemsOrderDomain>>> getInfoItemsByIdOrder(@Url String url);

    @POST
    Call<SendBrazeEmailResp> sendMailBraze(@Url String url, @Body SendMailReq request);

    @GET
    Call<GenericResponse<ItemAlgolia>> createItemAlgolia(@Url String url);

    @POST
    Call<CreateUserOnBrazeResponse> createUserBrazeByEmail(@Url String url, @Body CreateUserOnBrazeRequest request);

    @POST
    Call<GetUserByEmailBrazeResponse> getUserIdBrazeByEmail(@Url String url, @Body CreateUserOnBrazeRequest request);


    @POST
    Call<GeoCoderResponse> geoCoder(@Url String url,@Body AddressPredictionReq addressPredictionReq);

    @POST
    Call<ReverseGeoRes> geoInverse(@Url String url,@Body ReverseGeoReq reverseGeoReq);

    @POST
    Call<AutocompleteLupapRes> lupapAutocomplete(@Url String url,@Body() AutocompleteLupapReq lupapRequest);

    @GET
    Call<GeoCoderResponse> geoCoderLupapPlaceId(@Url String url);

    @GET
    Call<GenericResponse<String>>  updateStratumBraze(@Url String url);

    @POST
    Call<CancelOrderCourierRes> cancelOrderCourier(@Url String url,@Body OrderCourierCancelReq orderCourierCancelReq);

    @POST
    Call<Void> cancelStatusRx(@Url String url, @Body CancelStatusRx request);

    @POST
    Call<LoginFirebaseRes> loginFirebaseByUid(@Url String url, @Body() LoginFirebaseReq request, @Header("x-cloud-trace-context") String traceId);

    @POST
    Call<GenericResponse<String>> createEventProductBought(@Url String url, @Body EventRequest request);

    @POST
    Call<GenericResponse<String>> createEventOrderComplete(@Url String url, @Body EventRequest request);
    @GET
    Call<BannersDTFRes> getBannerV2(@Url String url);//,String email,String type,Integer category ,String city, Boolean isMobile

    @GET
    Call<BannersDTFRes> getBannerMinLefV2(@Url String url);//,String email,String type,Integer category ,String city, Boolean isMobile

    @GET
    Call<OrderInfoResponse> getOrderInfo(@Url String url);

    @GET
    Call<OrderInfoAmpOMSResponse> getOrderInfoAmplitudeOMS(@Url String url);

    @GET
    Call<OrderInfoAmplitudeBraze> getOrderInfoAmplitudeBraze(@Url String url);

    @GET
    Call<ClassificationBusiness<BusinessItem>> classificationBusinessItem(@Url String url);

    @POST
    Call<ClassificationBusiness<BusinessOrder>> classificationBusinessOrder(@Url String url, @Body BusinessOrderRequest request);

    @POST
    Call<GenericResponse<String>> saveAmplitudeSessionId (@Url String url, @Body AmplitudeSessionRequest request);

    @POST
    Call<Boolean> addNonStockItemUserBraze(@Url String url, @Body AddNonStockItemBrazeRequest request);

    @PUT
    Call<GenericResponse<Boolean>> updateBrazeNotificationsPreferences(@Url String url, @Body NotificationAndEmailBrazeRequest request);

    @PUT
    Call<GenericResponse<Boolean>> updateBrazeUserProfile(@Url String url, @Body UpdateUserOnBrazeRequest request);

    @POST
    Call<NotificationBrazeRequest> getBrazeNotificationsPreferences(@Url String url, @Body NotificationAndEmailBrazeRequest request);

    @POST
    Call<GenericResponse<Object>> sendPushNotificationBraze(@Url String url, @Body PushNotificationRequest request);


    @POST
    Call<GenericResponse> validateCoupon(@Url String url, @Body CouponValidation requestCore);

    @GET
    Call<RecommendResponse> getTrendingItemsByDepartment(@Url String url);

    @POST
    Call<GenericResponse> sendEventCreate(@Url String url, @Body BrazeEventCreate request);


    @POST
    Call<GenericResponse> updateCustomerAtom(@Url String url, @Body DataAtomUtilities request);

    @GET
    Call<GetCustomerResponse<CustomerFraudResponse>> searchFraudCustomer(@Url String url);



    //Micro-charge
    @GET
    Call<GenericResponse<Boolean>> getAntifraudValiate(@Url String url,@Header("source") String source);

    @POST
    Call<GenericResponse<PayMicroCharge>> getGenerateMicroCharge(@Url String url,@Body MicroCharge microCharge);

    @POST
    Call<GenericResponse<Boolean>> getValidateMicroCharge(@Url String url,@Body MicroCharge microCharge);

    @GET
    Call<GetCustomerResponse<CustomerFraudResponse>> antifraudCreditCard(@Url String url);


    @GET
    @Headers({"FTD-Authorization: ERthEryoKaTerThrE"})
    Call<FirebaseLoginCodeResponse> getCodeLoginSecure(@Url String url);

    @POST
    @Headers({"FTD-Authorization: ERthEryoKaTerThrE"})
    Call<FirebaseLoginCodeResponse> addCodeLoginSecure(@Url String url, @Body AddFirebaseCodeLoginRequest addFirebaseCodeLoginRequest);

    @DELETE
    @Headers({"FTD-Authorization: ERthEryoKaTerThrE"})
    Call<FirebaseLoginCodeResponse> deleteCodeLoginSecure(@Url String url);

    @POST
    Call<Void> addDeliveryOrderItemAsync(@Url String url);

    @GET
    Call<GenericResponse<Long>> validateOrderLastStatusOracle(@Url String url);

    @POST
    Call<GenericResponse> subscribePrimeFreeDays(@Url String url, @Body SubscribePrimeFreeDaysRequest request);

    @PUT
    Call<List<DiscountTalon>> updateCustomerSession(@Url String url, @Body CustomerSessionExternalRequest customerSessionRequest);

    @POST
    Call<TrackEventResponse> getTrackEventCustom(@Url String url,@Body TrackEventRequest trackEventRequest);

    @POST
    Call<TrackEventResponse> getTrackEventItemPurchased(@Url String url,@Body TrackEventItemPurchasedRequest trackEventRequest);

    @POST
    Call<AmplitudeEventsResponse> orderCompletedV2(@Url String url, @Body OrderCompletedV2Request request);

    @GET
    Call<GenericResponse<String>> getTokenFirebase(@Url String url);

    @GET
    Call<GetCustomerResponse<ValidateCustomerOracle>> getCustomerOracle(@Url String url);

    @POST
    Call<ValidatePasswordDataBaseResponse> iSamePasswordDataBase(@Url String url, @Body ValidatePasswordDataBase validatePasswordDataBase);

    @POST
    Call<CustomerGoogle> validateEmailGoogle(@Url String url, @Body GoogleAuth googleAuth);

    @GET
    Call<GenericResponse<String>> findCustomerDaneCodeCityByIdCity(@Url String idCity);

    @GET
    Call<String> getCourierUuid(@Url String url);

    @GET
    Call<GenericResponse<DeliveryOrderOms>> getOrderOMS(@Url String url);

    @POST
    Call<GeoCoderResponse> validateGeoZone(@Url String url,@Body ValidateGeoZoneReq addressPredictionReq);

    @POST
    Call<GenericResponse<GetOrdersOMSResponse>> postReleaseOrders(@Url String url, @Body GetOrdersPayloadOMS payload);

}
