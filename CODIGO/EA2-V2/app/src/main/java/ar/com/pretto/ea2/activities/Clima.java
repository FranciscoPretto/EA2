package ar.com.pretto.ea2.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ar.com.pretto.ea2.R;
import ar.com.pretto.ea2.entidades.ApiClima;
import ar.com.pretto.ea2.entidades.LogServidor;
import ar.com.pretto.ea2.entidades.Sesion;
import ar.com.pretto.ea2.interfaces.ServicioRetrofit;
import ar.com.pretto.ea2.mapeo.MapeoClima;
import ar.com.pretto.ea2.servicios.SessionService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Clima extends Activity {

    private Button buscarBtn;
    private TextView temperaturaTxtView,sensacionTermicaTxtView,humedadTxtView;
    private EditText locacionEditTxt;

    private boolean isBound = false;
    private SessionService sessionService;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Estamos bound Al servicio local, casteamos el IBinder y obtenemos la instancia del Servicio
            SessionService.LocalBinder binder = (SessionService.LocalBinder) service;
            sessionService = binder.getService();
            if(sessionService!=null) {
                isBound = true;
            }else{
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound=false;
        }
    };

    private static String TAG = "Clima";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clima);

        temperaturaTxtView = (TextView)findViewById(R.id.textViewTemp);
        sensacionTermicaTxtView = (TextView)findViewById(R.id.textViewSensacion);
        humedadTxtView = (TextView)findViewById(R.id.textViewHumedad);
        locacionEditTxt = (EditText)findViewById(R.id.editTextLocacion) ;
        buscarBtn = (Button) findViewById(R.id.buttonBuscar);

        buscarBtn.setOnClickListener(mostrarClima);

        Log.i(TAG,"Ejecuto OnCreate");
    }

    @Override
    protected void onStart()
    {
        Intent intent = new Intent(this, SessionService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
        Log.i(TAG,"Ejecuto Onstart");
        super.onStart();

    }

    @Override
    protected void onResume()
    {

        Log.i(TAG,"Ejecuto OnResume");
        super.onResume();

    }

    @Override
    protected void onPause() {
        Log.i(TAG,"Ejecuto OnPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"Ejecuto OnStop");
        if(isBound) {
            unbindService(connection);
            isBound = false;
        }
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

    private View.OnClickListener mostrarClima = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(Sesion.estoyOnline(getApplicationContext())) {
                mostrarDatosClima(locacionEditTxt.getText().toString().trim());
            }else{
                //se genera un Intent para poder lanzar la activity SinInternet
                Intent intent =new Intent(Clima.this, SinInternet.class);
                //se inicia la activity SinInternet
                startActivity(intent);
            }


        }
    };


    private void mostrarDatosClima(String location){

        ServicioRetrofit servicioRetrofit = ApiClima.obtenerCliente().create(ServicioRetrofit.class);

        Call<MapeoClima> call = servicioRetrofit.getWeatherData(location);

        call.enqueue(new Callback<MapeoClima>() {
            @Override
            public void onResponse(Call<MapeoClima> call, Response<MapeoClima> response) {

                if(response.isSuccessful()){
                    String temp = response.body().getDatosClima().getTemp();
                    String sensacionTermica = response.body().getDatosClima().getFeels_like();
                    String humedad = response.body().getDatosClima().getHumidity();
                    temperaturaTxtView.setText("\tTemperatura"+" "+temp+" C");
                    sensacionTermicaTxtView.setText("\tSensación Termica"+" "+sensacionTermica);
                    humedadTxtView.setText("\tHumedad"+" "+humedad+" %");

                    LogServidor.send(getString(R.string.evento_log),TAG,temp+""+sensacionTermica+""+humedad,sessionService,getApplicationContext());
                }else{
                    LogServidor.send(getString(R.string.error_log),TAG,"No pudo obtener datos del clima",sessionService,getApplicationContext());
                    Toast.makeText(getApplicationContext(),"No pudo obtener datos del clima",Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<MapeoClima> call, Throwable t) {

                LogServidor.send(getString(R.string.error_log),TAG,"No pudo obtener datos del clima",sessionService,getApplicationContext());
            }
        });

    }
}