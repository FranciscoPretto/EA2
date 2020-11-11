package ar.com.pretto.ea2.mapeo;

import com.google.gson.annotations.SerializedName;

public class MapeoClima {

    @SerializedName("main")
    private DatosClima datosClima;


    public DatosClima getDatosClima() {
        return datosClima;
    }

    public void setDatosClima(DatosClima datosClima) {
        this.datosClima = datosClima;
    }

}
