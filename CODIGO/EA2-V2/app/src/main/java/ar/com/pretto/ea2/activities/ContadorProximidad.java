package ar.com.pretto.ea2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import ar.com.pretto.ea2.R;
import ar.com.pretto.ea2.entidades.LogServidor;
import ar.com.pretto.ea2.servicios.SessionService;

public class ContadorProximidad extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private TextView txtInfoContador;
    private TextView txtInfoProximidad;

    private ImageButton btnReset;

    private Integer contador=0;
    private float valorAnteriorContador;

    private static String TAG = "Contador";

    private Boolean isBound=Boolean.FALSE;
    private SessionService sessionService;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
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
            isBound=Boolean.FALSE;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contador_proximidad);

        txtInfoContador = (TextView) findViewById(R.id.textViewDatosContador);
        txtInfoProximidad = (TextView) findViewById(R.id.textViewDatosSensorProx);
        btnReset = (ImageButton) findViewById(R.id.buttonResetContador);
        btnReset.setOnClickListener(resetContador);

        // Accedemos al servicio de sensores
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Log.i(TAG,"Ejecuto OnCreate");
    }

    // Metodo que escucha el cambio de sensibilidad de los sensores

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Metodo que escucha el cambio de los sensores
    @Override
    public void onSensorChanged(SensorEvent event) {

        float valorSensor;
        synchronized (this) {
            valorSensor=event.values[0];
            if(valorSensor==0 && valorAnteriorContador!=0){
                contador++;
                if(!LogServidor.send(getString(R.string.evento_log),TAG,"El valor del contador es: "+contador,sessionService,getApplicationContext())){
                    //se genera un Intent para poder lanzar la activity Sin Internet
                    Intent intent=new Intent(ContadorProximidad.this,SinInternet.class);
                    //se inicia la activity Registrarse
                    startActivity(intent);
                    finish();
                }
            }
            valorAnteriorContador=valorSensor;
        }
        txtInfoProximidad.setText("Proximidad: "+valorSensor);
        txtInfoContador.setText(contador.toString());

    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"Ejecuto OnResume");
        registrarSensorProximidad();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG,"Ejecuto OnRestart");
        registrarSensorProximidad();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isBound) {
            unbindService(connection);
            isBound = false;
        }
        Log.i(TAG,"Ejecuto OnStop");
        eliminarRegistroSensorProximidad();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"Ejecuto OnDestroy");
        eliminarRegistroSensorProximidad();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"Ejecuto OnPause");
        eliminarRegistroSensorProximidad();
    }

    @Override
    protected void onStart(){
        super.onStart();
        Intent intent = new Intent(this, SessionService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
        Log.i(TAG,"Ejecuto Onstart");
    }


    private void registrarSensorProximidad() {
        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void eliminarRegistroSensorProximidad() {
        mSensorManager.unregisterListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
    }

    private View.OnClickListener resetContador = new View.OnClickListener(){
        public void onClick(View v)
        {
            txtInfoContador.setText("0");
            contador=0;
            if(!LogServidor.send(getString(R.string.evento_log),TAG,"Se reseteo el contador",sessionService,getApplicationContext())){
                //se genera un Intent para poder lanzar la activity Sin Internet
                Intent intent=new Intent(ContadorProximidad.this,SinInternet.class);
                //se inicia la activity Registrarse
                startActivity(intent);
                finish();
            }
        }
    };

}