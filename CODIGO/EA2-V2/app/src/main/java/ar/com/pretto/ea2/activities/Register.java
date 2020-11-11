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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ar.com.pretto.ea2.R;
import ar.com.pretto.ea2.entidades.LogServidor;
import ar.com.pretto.ea2.entidades.Sesion;
import ar.com.pretto.ea2.entidades.Usuario;
import ar.com.pretto.ea2.interfaces.ServicioRetrofit;
import ar.com.pretto.ea2.mapeo.Register_Request;
import ar.com.pretto.ea2.mapeo.Register_Response;
import ar.com.pretto.ea2.servicios.SessionService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Register extends AppCompatActivity {

    private EditText txtNombre;
    private EditText txtApellido;
    private EditText txtDNI;
    private EditText txtEmail;
    private EditText txtContrasenia;
    private EditText txtComision;

    private Button btnRegistro;

    private Usuario usuarioNuevo;
    private SessionService sessionService;
    private Boolean isBound = Boolean.FALSE;
    private static String TAG = "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtNombre = (EditText) findViewById(R.id.editTextNombre);
        txtApellido = (EditText) findViewById(R.id.editTextApellido);
        txtDNI = (EditText) findViewById(R.id.editTextDNI);
        txtEmail = (EditText) findViewById(R.id.editTextMail);
         txtContrasenia = (EditText) findViewById(R.id.editTextTextPassword2);
        txtComision = (EditText) findViewById(R.id.editTextComision);
        btnRegistro = (Button) findViewById(R.id.buttonRegisterSend);
        btnRegistro.setOnClickListener(enviar_registro);

        Log.i(TAG,"Ejecuto OnCreate");

    }

    @Override
    protected void onStart() {
        Log.i(TAG,"Ejecuto Onstart");
        Intent intent = new Intent(this, SessionService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onResume() {
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

    private final View.OnClickListener enviar_registro = new View.OnClickListener() {
        public void onClick(View v) {

            enviarRegistro();
        }


    };

    private void enviarRegistro(){

        if(Sesion.estoyOnline(this)) {

            Register_Request request = new Register_Request();

            usuarioNuevo = new Usuario(txtEmail.getText().toString(), txtContrasenia.getText().toString());
            request.setEmail(usuarioNuevo.getMail().toString());
            request.setPassword(usuarioNuevo.getPassword().toString());

            request.setEnv("PROD");
            request.setName(txtNombre.getText().toString());
            request.setLastname(txtApellido.getText().toString());
            request.setDni(Long.parseLong(txtDNI.getText().toString()));
            request.setComission(Long.parseLong(txtComision.getText().toString()));



            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(getString(R.string.retrofit_server))
                    .build();

            ServicioRetrofit servicioRetrofit = retrofit.create(ServicioRetrofit.class);

            Call<Register_Response> call = servicioRetrofit.register(request);

            call.enqueue(new Callback<Register_Response>() {


                @Override
                public void onResponse(Call<Register_Response> call, Response<Register_Response> response) {


                    if (response.isSuccessful()) {
                        //  GUARDAR CREDENCIALES, INICIAR SERVICIO DE REFRESH DE TOKEN, INICIAR ACTIVITY MAIN Y (CREO QUE EN EL MAIN: DESABILITAR ESTA ACTIVITY)

                        Sesion sesion = new Sesion(new Usuario(usuarioNuevo.getMail(), usuarioNuevo.getPassword()), response.body().getToken(), response.body().getToken_refresh());
                        sessionService.setSesionActual(sesion);
                        Intent intent = new Intent(Register.this, Menu.class);
                        if(!LogServidor.send(getString(R.string.login_log),TAG,"Se efectuo Registracion correctamente",sessionService,getApplicationContext())){
                            //se genera un Intent para poder lanzar la activity Sin Internet
                            intent=new Intent(Register.this,SinInternet.class);
                            //se inicia la activity Registrarse
                            startActivity(intent);
                        }
                        //se inicia la activity Menu
                        startActivity(intent);
                        finish();

                    } else {
                        if(usuarioNuevo.getPassword().length()<8)
                        Toast.makeText(getApplicationContext(),"Recuerde que la contraseña debe tener al menos 8 digitos",Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(),"Alguno de los datos ingresados ya se encuentran registrados, puede realizar el log in o registrar un nuevo usuario",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Register_Response> call, Throwable t) {

                }
            });
        }else{
            iniciarActivitySinConexion();
        }
    } // Termina enviar Registro


    private void iniciarActivitySinConexion(){
        //se genera un Intent para poder lanzar la activity Sin Internet
        Intent intent=new Intent(Register.this,SinInternet.class);
        //se inicia la activity Registrarse
        startActivity(intent);

    }

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
}
