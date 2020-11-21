package ar.com.pretto.ea2.entidades;

import android.provider.ContactsContract;

import androidx.annotation.NonNull;

public class Usuario {

    private String mail;
    private String password;

    public Usuario(String mail, String password) {
        this.mail = mail;
        this.password = password;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @NonNull
    @Override
    protected Usuario clone()  {
        return new Usuario(this.getMail(),this.getPassword());
    }
}
