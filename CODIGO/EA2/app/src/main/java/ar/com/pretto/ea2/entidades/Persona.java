package ar.com.pretto.ea2.entidades;

public class Persona {

    private String Name;
    private String LastName;
    private Integer DNI;
    private Usuario usuario;
    private Integer commission;

    public Persona(String name, String lastName, Integer DNI, Usuario usuario, Integer commission) {
        Name = name;
        LastName = lastName;
        this.DNI = DNI;
        this.usuario = usuario;
        this.commission = commission;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public Integer getDNI() {
        return DNI;
    }

    public void setDNI(Integer DNI) {
        this.DNI = DNI;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Integer getCommission() {
        return commission;
    }

    public void setCommission(Integer commission) {
        this.commission = commission;
    }
}