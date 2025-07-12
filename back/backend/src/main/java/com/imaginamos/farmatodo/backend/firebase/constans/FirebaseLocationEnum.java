package com.imaginamos.farmatodo.backend.firebase.constans;

public enum FirebaseLocationEnum {
    LOGIN_CODE_LOCATION ("https://stunning-base-login-users.firebaseio.com",
            "https://stunning-base-login-users.firebaseio.com/users.json",
            ""),
    LOGIN_GET_LOCATION("https://stunning-base-login-users.firebaseio.com",
            "https://stunning-base-login-users.firebaseio.com/users/{idCustomer}.json",
            "{idCustomer}"
            ),
    GET_TRACKING_ORDER("https://stunning-base-delivery-tracking.firebaseio.com",
            "https://stunning-base-delivery-tracking.firebaseio.com/server/order/tracking/{idOrder}.json",
            "{idOrder}"
    ),
    SET_ORDER_PRIME_MIXED("https://stunning-base-delivery-tracking.firebaseio.com",
            "https://stunning-base-pse.firebaseio.com/orders/{idOrder}.json",
            "{idOrder}"
    ),
    AUTH_FIREBASE ("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword",
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword",
            ""),
    LOGIN_CODE_LOCATION_SECURED ("https://stunning-base-164402-login-secured.firebaseio.com",
            "https://stunning-base-164402-login-secured.firebaseio.com/users.json",
            ""),

    LOGIN_GET_LOCATION_SECURED("https://stunning-base-164402-login-secured.firebaseio.com",
            "https://stunning-base-164402-login-secured.firebaseio.com/users/{idCustomer}.json",
            "{idCustomer}"
    ),

    ;

    private String baseUrl;
    private String location;
    private String keyToReplace;

    FirebaseLocationEnum(final String baseUrl, final String location, String keyToReplace) {
        this.baseUrl = baseUrl;
        this.location = location;
        this.keyToReplace = keyToReplace;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String location() {
        return location;
    }

    public String keyToReplace() {
        return keyToReplace;
    }
}
