package com.imaginamos.farmatodo.networking.talonone;

import com.imaginamos.farmatodo.model.order.ValidateOrderBackend3;
import com.imaginamos.farmatodo.model.order.ValidateOrderReq;
import com.imaginamos.farmatodo.networking.api.ApiGateway;
import com.imaginamos.farmatodo.networking.services.ApiGatewayService;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ApiGatewayServiceTest {

    private ApiGateway apiGatewayMock;

//    @InjectMocks
    private ApiGatewayService apiGatewayService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldReturnErrorWhenShoppingCartFailed() throws IOException {
        // given
        ValidateOrderReq validateOrderReq = new ValidateOrderReq();
        String traceId = new String();
        Response<ValidateOrderBackend3> responseExpected = Response.error(500, ResponseBody.create(null, ""));
        Call<ValidateOrderBackend3> call = new Call<ValidateOrderBackend3>() {
            @Override
            public Response<ValidateOrderBackend3> execute() throws IOException {
                return Response.error(500, ResponseBody.create(null, ""));
            }

            @Override
            public void enqueue(Callback<ValidateOrderBackend3> callback) {

            }

            @Override
            public boolean isExecuted() {
                return false;
            }

            @Override
            public void cancel() {

            }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @Override
            public Call<ValidateOrderBackend3> clone() {
                return null;
            }

            @Override
            public Request request() {
                return null;
            }
        };
        //when
        doReturn(responseExpected).when(this.apiGatewayMock.priceDeliveryOrder(any(), any(), any())).execute();

        Response<ValidateOrderBackend3> response = apiGatewayService.validateOrder(validateOrderReq, traceId);
        //then
        assertEquals(responseExpected.code(), response.code());
    }
}
