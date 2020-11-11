package ar.com.pretto.ea2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import ar.com.pretto.ea2.R;
import ar.com.pretto.ea2.entidades.LogServidor;
import ar.com.pretto.ea2.entidades.Sesion;
import ar.com.pretto.ea2.servicios.SessionService;

public class Menu extends AppCompatActivity {

    private ProgressBar bateriaBar;
    private Button botonPaint;
    private Button botonContador;
    private Button botonLogOut;
    private Button botonClima;
    private TextView nivelBateria;

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
                cerrarSesion();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound=false;
        }
    };

    private static String TAG = "Menu";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        bateriaBar=findViewById(R.id.progressBar2);

        nivelBateria = (TextView)findViewById(R.id.textViewNivelBateria);
        botonPaint = (Button) findViewById(R.id.buttonPaint);
        botonContador = (Button) findViewById(R.id.buttonContador);
        botonLogOut = (Button) findViewById(R.id.buttonLogOut);
        botonClima = (Button)findViewById(R.id.buttonClima);

        botonPaint.setOnClickListener(botonesListeners);
        botonContador.setOnClickListener(botonesListeners);
        botonLogOut.setOnClickListener(botonesListeners);
        botonClima.setOnClickListener(botonesListeners);

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
        mostrarPorcentajeBateria();
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
        detenerServicioSession();
        Log.i(TAG,"Ejecuto OnDestroy");
    }

    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {

        public void onClick(View v)
        {
            if(!Sesion.estoyOnline(getApplicationContext())) {
                //se genera un Intent para poder lanzar la activity Sin Internet
                Intent intent=new Intent(Menu.this,SinInternet.class);
                //se inicia la activity Sin Internet
                startActivity(intent);
                finish();
            }
                //Se determina que componente genero un evento

                switch (v.getId()) {

                    case R.id.buttonPaint:
                        iniciarPaint();
                        LogServidor.send(getString(R.string.evento_log), TAG, "Se inicia Paint", sessionService, getApplicationContext());
                        break;
                    case R.id.buttonContador:
                        iniciarContador();
                        LogServidor.send(getString(R.string.evento_log), TAG, "Se inicia Contador", sessionService, getApplicationContext());
                        break;
                    case R.id.buttonLogOut:
                        cerrarSesion();
                        LogServidor.send(getString(R.string.login_log), TAG, "Se cierra sesion", sessionService, getApplicationContext());
                        break;
                    case R.id.buttonClima:
                        iniciarClima();
                        LogServidor.send(getString(R.string.login_log), TAG, "Se inicia el Clima", sessionService, getApplicationContext());
                        break;

                    default:
                        Toast.makeText(getApplicationContext(), "Error en Listener de botones", Toast.LENGTH_LONG).show();
                        LogServidor.send(getString(R.string.error_log), TAG, "Error en Listener de botones", sessionService, getApplicationContext());
                }


        }
    };


    private void mostrarPorcentajeBateria(){

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int cargaBateria=(level*100/scale);
        String txt ="\t Porcentaje de Bateria: "+cargaBateria+"%";
        nivelBateria.setText(txt);
        bateriaBar.setProgress(cargaBateria);
    }

    private void iniciarClima(){
        //se genera un Intent para poder lanzar la activity Registrarse
        Intent intent =new Intent(Menu.this, Clima.class);
        //se inicia la activity Paint
        startActivity(intent);

    }

    private void iniciarPaint(){
        //se genera un Intent para poder lanzar la activity Paint
        Intent intent =new Intent(Menu.this, CirculoAcelerometro.class);
        //se inicia la activity Paint
        startActivity(intent);

    }

    private void iniciarContador(){
        //se genera un Intent para poder lanzar la activity Contador
        Intent intent=new Intent(Menu.this,ContadorProximidad.class);
        //se inicia la activity Contador
        startActivity(intent);
    }

    private void cerrarSesion(){
        //se genera un Intent para poder lanzar la activity LogIn
        Intent intent =new Intent(Menu.this,LogIn.class);
        detenerServicioSession();
        //se inicia la activity LogIn
        startActivity(intent);
        finish();
    }



    private void detenerServicioSession(){
        stopService(new Intent(this, SessionService.class));
    }

}