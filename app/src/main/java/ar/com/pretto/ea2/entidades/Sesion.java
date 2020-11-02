package ar.com.pretto.ea2.entidades;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import ar.com.pretto.ea2.activities.LogIn;
import ar.com.pretto.ea2.activities.SinInternet;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Sesion {

    private Usuario usuarioActual;
    private String token;
    private String tokenRefresh;

    public Sesion(Usuario usuarioActual, String token, String tokenRefresh) {
        this.usuarioActual = usuarioActual;
        this.token = token;
        this.tokenRefresh = tokenRefresh;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = this.usuarioActual;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenRefresh() {
        return tokenRefresh;
    }

    public void setTokenRefresh(String tokenRefresh) {
        this.tokenRefresh = tokenRefresh;
    }


    public static Boolean estoyOnline(Context context){

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo;
        synchronized (context) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        if(networkInfo!=null && networkInfo.isConnectedOrConnecting())
            return Boolean.TRUE;
        else
            return Boolean.FALSE;

    }

    @NonNull
    @Override
    public Sesion clone()  {
        return new Sesion(this.usuarioActual.clone(),this.token,this.tokenRefresh);
    }


}
