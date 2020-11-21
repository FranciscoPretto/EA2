package ar.com.pretto.ea2.interfaces;

import ar.com.pretto.ea2.mapeo.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ServicioRetrofit {

    @POST("api/register")
    Call<Register_Response> register (@Body Register_Request request);

    @POST("api/login")
    Call<LogIn_Response> login (@Body LogIn_Request request);

    @POST("api/event")
    Call<Log_Response> log (@Header("Authorization") String token,@Body Log_Request request);

    @PUT("api/refresh")
    Call<Refresh_Response> refresh (@Header("Authorization") String tokenRefresh);

    @GET("weather?appid=92756c24107bc39dd0a7541f66ba55c5&units=metric")
    Call<MapeoClima> getWeatherData(@Query("q") String name);

}
