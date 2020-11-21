package ar.com.pretto.ea2.servicios;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;

import ar.com.pretto.ea2.R;
import ar.com.pretto.ea2.activities.ContadorProximidad;
import ar.com.pretto.ea2.activities.LogIn;
import ar.com.pretto.ea2.activities.Menu;
import ar.com.pretto.ea2.activities.SinInternet;
import ar.com.pretto.ea2.entidades.LogServidor;
import ar.com.pretto.ea2.entidades.Sesion;
import ar.com.pretto.ea2.entidades.Usuario;
import ar.com.pretto.ea2.interfaces.ServicioRetrofit;
import ar.com.pretto.ea2.mapeo.LogIn_Request;
import ar.com.pretto.ea2.mapeo.LogIn_Response;
import ar.com.pretto.ea2.mapeo.Refresh_Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SessionService extends Service implements Serializable {

    private final String TAG ="Session";
    private final Integer  TIEMPO_REFRESH = 1500000; // 25 minutos
    private Handler refreshHandler = new Handler();
    private Sesion sesionActual;
    private final IBinder binder = new LocalBinder();


   public class LocalBinder extends Binder{
       public SessionService getService(){
           return SessionService.this;
       }
   }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public Sesion getSesionActual(){
       return sesionActual;
    }

    public void setSesionActual(Sesion sesion)  {
        this.sesionActual=sesion.clone();
       ejecutarRefresh();
        Log.i(TAG,"Se seteo una nueva session");
        if(!LogServidor.send(getString(R.string.evento_log),TAG,"Se seteo una nueva session",this,getApplicationContext())){
            //se genera un Intent para poder lanzar la activity Sin Internet
            Intent intent=new Intent(this, SinInternet.class);
            //se inicia la activity Sin Internet
            startActivity(intent);
        }

    }

    private void   ejecutarRefresh(){
       refreshHandler.postDelayed(new Runnable() {
           @Override
           public void run() {
               refreshToken();

               refreshHandler.postDelayed(this,TIEMPO_REFRESH);
           }
       },TIEMPO_REFRESH);
    }

    private void refreshToken(){

        if(Sesion.estoyOnline(this)){

            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(getString(R.string.retrofit_server))
                    .build();

            ServicioRetrofit servicioRetrofit = retrofit.create(ServicioRetrofit.class);

            Call<Refresh_Response> call = servicioRetrofit.refresh("Bearer " + getSesionActual().getTokenRefresh());

            call.enqueue(new Callback<Refresh_Response>() {
                @Override
                public void onResponse(Call<Refresh_Response> call, Response<Refresh_Response> response) {

                    if (response.isSuccessful()) {

                        Sesion sesion = new Sesion(getSesionActual().getUsuarioActual(), response.body().getToken(), response.body().getToken_refresh());
                        setSesionActual(sesion);
                        Log.i(TAG,"Se refresheo el token");

                    } else {
                        //se genera un Intent para poder lanzar la activity LogIn
                        Intent intent=new Intent(SessionService.this,LogIn.class);
                        //se inicia la activity LogIn
                        startActivity(intent);
                        stopSelf();
                    }
                }

                @Override
                public void onFailure(Call<Refresh_Response> call, Throwable t) {
                    //NO DEBERIA ENTRAR ACA, ALGO DE ERROR...
                }
            });
        }else{
            //se genera un Intent para poder lanzar la activity LogIn
            Intent intent=new Intent(SessionService.this,LogIn.class);
            //se inicia la activity LogIn
            startActivity(intent);
        }
    }

}
