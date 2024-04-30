package kz.kaspi.qr.plugin.integration;

import kz.kaspi.qr.plugin.integration.dto.Create;
import kz.kaspi.qr.plugin.integration.dto.DeviceRegistration;
import kz.kaspi.qr.plugin.integration.dto.DeviceToken;
import kz.kaspi.qr.plugin.integration.dto.response.DeleteDeviceResponse;
import kz.kaspi.qr.plugin.integration.dto.response.DeviceTokenResponse;
import kz.kaspi.qr.plugin.integration.dto.response.PaymentCreateLinkResponse;
import kz.kaspi.qr.plugin.integration.dto.response.PaymentCreateResponse;
import kz.kaspi.qr.plugin.integration.dto.response.PaymentDetailsResponse;
import kz.kaspi.qr.plugin.integration.dto.response.PaymentStatusResponse;
import kz.kaspi.qr.plugin.integration.dto.response.ReturnCreateResponse;
import kz.kaspi.qr.plugin.integration.dto.response.TradePointsResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface KaspiQRPayClient {
    @GET("partner/tradepoints")
    Call<TradePointsResponse> getTradePoints();

    @POST("device/register")
    Call<DeviceTokenResponse> register(@Body DeviceRegistration registration);

    @POST("device/delete")
    Call<DeleteDeviceResponse> delete(@Body DeviceToken token);

    @POST("qr/create")
    Call<PaymentCreateResponse> paymentCreate(@Body Create create);

    @POST("qr/create-link")
    Call<PaymentCreateLinkResponse> paymentLinkCreate(@Body Create create);

    @POST("return/create")
    Call<ReturnCreateResponse> returnCreate(@Body Create create);

    @GET("payment/status/{paymentId}")
    Call<PaymentStatusResponse> getPaymentStatus(@Path("paymentId") String paymentId);

    @GET("return/status/{returnId}")
    Call<PaymentStatusResponse> getReturnStatus(@Path("returnId") String returnId);

    @GET("payment/details")
    Call<PaymentDetailsResponse> getDetails(@Query("QrPaymentId") String paymentId, @Query("DeviceToken") String token);
}
