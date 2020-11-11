package ar.com.pretto.ea2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import ar.com.pretto.ea2.R;
import ar.com.pretto.ea2.entidades.LogServidor;
import ar.com.pretto.ea2.entidades.Sesion;
import ar.com.pretto.ea2.servicios.SessionService;

public class SinInternet extends AppCompatActivity {

    private Button btnProbarConexion;

    private static String TAG = "SinInternet";
    private SessionService sessionService;
    private Boolean isBound=Boolean.FALSE;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // Estamos bound Al servicio local, casteamos el IBinder y obtenemos la instancia del Servicio
            SessionService.LocalBinder binder = (SessionService.LocalBinder) service;
            sessionService = binder.getService();
            isBound=Boolean.TRUE;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound=Boolean.FALSE;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sin_internet);

        btnProbarConexion=(Button)findViewById(R.id.buttonProbarConexion);
        btnProbarConexion.setOnClickListener(probarConexion);

        Log.i(TAG,"Ejecuto OnCreate");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = new Intent(this, SessionService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
        Log.i(TAG,"Ejecuto Onstart");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG,"Ejecuto OnResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"Ejecuto OnPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isBound) {
            unbindService(connection);
            isBound = false;
        }
        Log.i(TAG,"Ejecuto OnStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = new Intent(this, SessionService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
        Log.i(TAG,"Ejecuto OnRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"Ejecuto OnDestroy");
    }

    private final View.OnClickListener probarConexion = new View.OnClickListener() {
        public void onClick(View v) {
            testConexion();
        }
    };
    private void testConexion(){

        if (Sesion.estoyOnline(this)) {
            if(!LogServidor.send(getString(R.string.evento_log),TAG,"Se comprueba conexion",sessionService,getApplicationContext())){
                Toast.makeText(getApplicationContext(), "Sigue sin conexion a Internet", Toast.LENGTH_LONG).show();
            }else {
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Sigue sin conexion a Internet", Toast.LENGTH_LONG).show();
        }
    }

}