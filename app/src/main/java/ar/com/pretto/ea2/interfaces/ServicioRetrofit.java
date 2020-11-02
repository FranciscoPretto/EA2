package ar.com.pretto.ea2.interfaces;

import ar.com.pretto.ea2.mapeo.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ServicioRetrofit {

    @POST("api/register")
    Call<Register_Response> register (@Body Register_Request request);

    @POST("api/login")
    Call<LogIn_Response> login (@Body LogIn_Request request);

    @POST("api/event")
    Call<Log_Response> log (@Header("Authorization") String token,@Body Log_Request request);
}
