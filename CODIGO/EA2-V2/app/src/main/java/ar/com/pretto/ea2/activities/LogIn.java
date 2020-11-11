package ar.com.pretto.ea2.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import ar.com.pretto.ea2.R;
import ar.com.pretto.ea2.entidades.LogServidor;
import ar.com.pretto.ea2.entidades.Sesion;
import ar.com.pretto.ea2.entidades.Usuario;
import ar.com.pretto.ea2.interfaces.ServicioRetrofit;
import ar.com.pretto.ea2.mapeo.LogIn_Request;
import ar.com.pretto.ea2.mapeo.LogIn_Response;
import ar.com.pretto.ea2.servicios.SessionService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LogIn extends AppCompatActivity {

    private Button btnLogIn;
    private Button btnRegister;

    private EditText txtMail;
    private EditText txtPassword;

    private Usuario usaurioLogueo;

    private boolean isBound = false;
    private SessionService sessionService;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Estamos bound Al servicio local, casteamos el IBinder y obtenemos la instancia del Servicio
            SessionService.LocalBinder binder = (SessionService.LocalBinder) service;
            sessionService = binder.getService();
            isBound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound=false;
        }
    };

    private static String TAG = "LogIn";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        txtMail = (EditText)findViewById(R.id.editTextMail);
        txtPassword = (EditText)findViewById(R.id.editTextTextPassword);

        btnLogIn = (Button) findViewById(R.id.buttonLogIn);
        btnRegister = (Button) findViewById(R.id.buttonRegistrarse);

        btnLogIn.setOnClickListener(botonesListeners);
        btnRegister.setOnClickListener(botonesListeners);

        iniciarServicioSesion();
    }

    @Override
    protected void onStart()
    {
        Log.i(TAG,"Ejecuto Onstart");
        //Bind a Servicio de Sesión

        Intent intent = new Intent(this, SessionService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);

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



    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty
    private View.OnClickListener botonesListeners = new View.OnClickListener()
    {

        public void onClick(View v)
        {
            //Se determina que componente genero un evento

            switch (v.getId())
            {
                case R.id.buttonRegistrarse:
                    iniciarRegistro();
                    break;

                case R.id.buttonLogIn:
                    realizarLogIn();
                        break;
                default:
                    Toast.makeText(getApplicationContext(), "Error en Listener de botones", Toast.LENGTH_LONG).show();

        }
    };


};

  private void iniciarRegistro(){
      //se genera un Intent para poder lanzar la activity Registrarse
      Intent intent=new Intent(LogIn.this,Register.class);
      //se inicia la activity Registrarse
      startActivity(intent);
  }

  private void realizarLogIn(){

      if(Sesion.estoyOnline(this)){
          LogIn_Request request = new LogIn_Request();

          if(usaurioLogueo == null) {
              usaurioLogueo = new Usuario(txtMail.getText().toString(), txtPassword.getText().toString());
          }
          request.setEmail(usaurioLogueo.getMail());
          request.setPassword(usaurioLogueo.getPassword());


          Retrofit retrofit = new Retrofit.Builder()
                  .addConverterFactory(GsonConverterFactory.create())
                  .baseUrl(getString(R.string.retrofit_server))
                  .build();

          ServicioRetrofit servicioRetrofit = retrofit.create(ServicioRetrofit.class);

          Call<LogIn_Response> call = servicioRetrofit.login(request);

          call.enqueue(new Callback<LogIn_Response>() {
              @Override
              public void onResponse(Call<LogIn_Response> call, Response<LogIn_Response> response) {

                  if (response.isSuccessful()) {

                      Sesion sesion = new Sesion(new Usuario(usaurioLogueo.getMail(), usaurioLogueo.getPassword()), response.body().getToken(), response.body().getToken_refresh());
                      sessionService.setSesionActual(sesion);

                         //se inicia la activity Menu
                         Intent intent = new Intent(LogIn.this, Menu.class);
                         startActivity(intent);
                         finish();


                  } else {
                      Toast.makeText(getApplicationContext(),"Usuario o contraseña incorrecto",Toast.LENGTH_LONG).show();
                      txtPassword.setText("");
                  }
              }

              @Override
              public void onFailure(Call<LogIn_Response> call, Throwable t) {
                  //NO DEBERIA ENTRAR ACA, ALGO DE ERROR...
              }
          });
      }else{
          iniciarActivitySinConexion();
      }
  } // Termina LogIn


  private void iniciarActivitySinConexion(){
      //se genera un Intent para poder lanzar la activity Sin Internet
      Intent intent=new Intent(LogIn.this,SinInternet.class);
      //se inicia la activity Registrarse
      startActivity(intent);

  }


    private void iniciarServicioSesion(){
        Intent intent = new Intent(LogIn.this, SessionService.class);
        new Thread(new Runnable() {
            @Override
           public void run() {
                startService(intent);
           }
        }).start();
    }
}
