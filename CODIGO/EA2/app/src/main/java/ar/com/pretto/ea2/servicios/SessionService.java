package ar.com.pretto.ea2.servicios;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.Serializable;

import ar.com.pretto.ea2.R;
import ar.com.pretto.ea2.activities.ContadorProximidad;
import ar.com.pretto.ea2.activities.SinInternet;
import ar.com.pretto.ea2.entidades.LogServidor;
import ar.com.pretto.ea2.entidades.Sesion;

public class SessionService extends Service implements Serializable {

    private final String TAG ="Servicio de Session";
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
        Log.i(TAG,"Se creo una nueva session");
        if(!LogServidor.send(getString(R.string.evento_log),TAG,"Se creo una nueva session",this,getApplicationContext())){
            //se genera un Intent para poder lanzar la activity Sin Internet
            Intent intent=new Intent(this, SinInternet.class);
            //se inicia la activity Registrarse
            startActivity(intent);
        }

    }



}
