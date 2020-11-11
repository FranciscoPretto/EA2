package ar.com.pretto.ea2.entidades;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClima {

    private static Retrofit retrofit = null;

    public static  Retrofit obtenerCliente(){

        if (retrofit == null){

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.openweathermap.org/data/2.5/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }


        return retrofit;

    }

}
