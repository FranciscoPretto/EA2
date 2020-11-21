package ar.com.pretto.ea2.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import ar.com.pretto.ea2.R;
import ar.com.pretto.ea2.entidades.LogServidor;
import ar.com.pretto.ea2.entidades.Sesion;
import ar.com.pretto.ea2.servicios.SessionService;

public class CirculoAcelerometro extends AppCompatActivity implements SensorEventListener{

    private DrawerLayout paintView;
    private CanvasPintar canvas;
    private TextView datosAcelerometro;
    private int colorActual=Color.BLACK;
    private ImageButton btnRojo;
    private ImageButton btnAzul;
    private ImageButton btnVerde;
    private ImageButton btnNegro;

    private SensorManager sensorManager;
    private Sensor  sensorAcelerometro;
    private  SensorEventListener sensorEventListener;

    private Timer timer;
    private Handler handler;

    private float sensorX =0;
    private float sensorY =0;
    private float sensorZ =0;

    private float xCalibrado;
    private float yCalibrado;
    private float zCalibrado;

    private long lastSensorUpdateTime=0;

    private float circleX;
    private float circleY;
    private int baseCircleRadius = 20;
    private static String TAG = "Paint";

    Boolean isBound = Boolean.FALSE;
    private SessionService sessionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);

        canvas = new CanvasPintar(this);

        setContentView(R.layout.activity_paint_acelerometro);
        paintView = (DrawerLayout) findViewById(R.id.paintView);
        paintView.addView(canvas);

        btnAzul=(ImageButton)findViewById(R.id.imageButtonAzul);
        btnNegro=(ImageButton)findViewById(R.id.imageButtonNegro);
        btnRojo=(ImageButton)findViewById(R.id.imageButtonRojo);
        btnVerde=(ImageButton)findViewById(R.id.imageButtonVerde);

        btnVerde.setOnClickListener(botonesListeners);
        btnNegro.setOnClickListener(botonesListeners);
        btnAzul.setOnClickListener(botonesListeners);
        btnRojo.setOnClickListener(botonesListeners);

        Log.i(TAG,"Ejecuto OnCreate");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int screenWidth = size.x;
        int screenHeight = size.y;

        circleX = screenWidth / 2 - baseCircleRadius;
        circleY = screenHeight / 2 - baseCircleRadius;

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                canvas.invalidate();
            }
        };

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if(sensorX<0){
                    circleX+=5;
                }else{
                    circleX-=5;
                }


                if(sensorY<0){
                    circleY+=5;
                }else{
                    circleY-=5;
                }


                handler.sendEmptyMessage(0);
            }
        },0,200);

    }


    // Metodo que escucha el cambio de sensibilidad de los sensores
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    // Metodo que escucha el cambio de los sensores
    @Override
    public void onSensorChanged(SensorEvent event)
    {
        long currentTime = System.currentTimeMillis();
        datosAcelerometro=(TextView)findViewById(R.id.textViewDatosAcelerometro);
        sensorX =0;
        sensorY =0;
        sensorZ =0;

        synchronized (this){

            sensorX =event.values[0];
            sensorY =event.values[1];
            sensorZ =event.values[2];

        }

        datosAcelerometro.setText("X: " + sensorX + "Y " + sensorY + "Z " + sensorZ);

        if((currentTime-lastSensorUpdateTime)>100){
            lastSensorUpdateTime=currentTime;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"Ejecuto OnResume");
        registrarSensorAcelerometro();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Intent intent = new Intent(this, SessionService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
        Log.i(TAG,"Ejecuto OnRestart");
        registrarSensorAcelerometro();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isBound) {
            unbindService(connection);
            isBound = false;
        }
        Log.i(TAG,"Ejecuto OnStop");
        eliminarRegistroSensorAcelerometro();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"Ejecuto OnDestroy");
        eliminarRegistroSensorAcelerometro();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"Ejecuto OnPause");
        eliminarRegistroSensorAcelerometro();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SessionService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
        Log.i(TAG,"Ejecuto OnPause");

    }

    class CanvasPintar extends View{

        float eje_x;
        float eje_y;
        int opc;

        Paint pen = new Paint();
        Path path = new Path();

        public CanvasPintar(Context context) {

            super(context);

            setFocusable(true);
            pen.setStyle(Paint.Style.FILL);
            pen.setStrokeWidth(5);
            pen.setColor(colorActual);

        }

        protected void onDraw(Canvas screen){
            pen.setColor(colorActual);
            pen.setStyle(Paint.Style.FILL);
            pen.setAntiAlias(true);
            pen.setTextSize(15f);
            screen.drawColor(Color.WHITE);
            screen.drawCircle(circleX,circleY, baseCircleRadius,pen);


        }



    }

    private void registrarSensorAcelerometro(){
        sensorManager.registerListener((SensorEventListener) this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void eliminarRegistroSensorAcelerometro(){
        sensorManager.unregisterListener((SensorEventListener) this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

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
            isBound=false;
        }
    };

    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {

        public void onClick(View v)
        {
            //Se determina que componente genero un evento
            if(!Sesion.estoyOnline(getApplicationContext())){
                //se genera un Intent para poder lanzar la activity Sin Internet
                Intent intent=new Intent(CirculoAcelerometro.this,SinInternet.class);
                //se inicia la activity Registrarse
                startActivity(intent);
                finish();
            }
            switch (v.getId())
            {
                case R.id.imageButtonAzul:
                    colorActual=Color.BLUE;
                    LogServidor.send(getString(R.string.evento_log),TAG,"Se cambio al color Azul",sessionService,getApplicationContext());
                    break;

                case R.id.imageButtonNegro:
                    colorActual=Color.BLACK;
                    LogServidor.send(getString(R.string.evento_log),TAG,"Se cambio al color Negro",sessionService,getApplicationContext());
                    break;

                case R.id.imageButtonRojo:
                    colorActual=Color.RED;
                    LogServidor.send(getString(R.string.evento_log),TAG,"Se cambio al color Rojo",sessionService,getApplicationContext());
                    break;

                case R.id.imageButtonVerde:
                    colorActual=Color.GREEN;
                    LogServidor.send(getString(R.string.evento_log),TAG,"Se cambio al color Verde",sessionService,getApplicationContext());
                    break;

                default:
                    Toast.makeText(getApplicationContext(), "Error en Listener de botones", Toast.LENGTH_LONG).show();
                    LogServidor.send(getString(R.string.error_log),TAG,"Error en Listener de botones",sessionService,getApplicationContext());

            }
        };


    };

}