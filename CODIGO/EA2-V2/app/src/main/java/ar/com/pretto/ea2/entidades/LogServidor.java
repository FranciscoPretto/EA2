package ar.com.pretto.ea2.entidades;

import android.content.Context;

import ar.com.pretto.ea2.interfaces.ServicioRetrofit;
import ar.com.pretto.ea2.mapeo.Log_Request;
import ar.com.pretto.ea2.mapeo.Log_Response;
import ar.com.pretto.ea2.servicios.SessionService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LogServidor {

   public static Boolean send(String type, String activity, String content, SessionService sesion, Context context){

      if(Sesion.estoyOnline(context)) {

         Log_Request request = new Log_Request();
         request.setEnv("PROD");
         request.setType_events(type);
         request.setDescription(activity + " " + content);


         Retrofit retrofit = new Retrofit.Builder()
                 .addConverterFactory(GsonConverterFactory.create())
                 .baseUrl("http://so-unlam.net.ar/api/")
                 .build();

         ServicioRetrofit servicioRetrofit = retrofit.create(ServicioRetrofit.class);

         Call<Log_Response> call = servicioRetrofit.log("Bearer " + sesion.getSesionActual().getToken(), request);

         call.enqueue(new Callback<Log_Response>() {
            @Override
            public void onResponse(Call<Log_Response> call, Response<Log_Response> response) {

               if (response.isSuccessful()) {

               } else {

               }
            }

            @Override
            public void onFailure(Call<Log_Response> call, Throwable t) {

            }
         });

         return Boolean.TRUE; // No hubo problemas de conexion
      }else{
         return Boolean.FALSE; // Hubo problemas de conexion
      }
   }

}
