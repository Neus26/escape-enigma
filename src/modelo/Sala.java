package modelo;

import java.math.BigDecimal;

public class Sala {
    private int id;
    private String nombre;
    private String tematica;
    private int dificultad;
    private int aforoMax;
    private BigDecimal precioPersona;
    private boolean activa;

    public Sala() {}

    public Sala(int id, String nombre, String tematica, int dificultad, int aforoMax, BigDecimal precioPersona, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.tematica = tematica;
        this.dificultad = dificultad;
        this.aforoMax = aforoMax;
        this.precioPersona = precioPersona;
        this.activa = activa;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTematica() { return tematica; }
    public void setTematica(String tematica) { this.tematica = tematica; }

    public int getDificultad() { return dificultad; }
    public void setDificultad(int dificultad) { this.dificultad = dificultad; }

    public int getAforoMax() { return aforoMax; }
    public void setAforoMax(int aforoMax) { this.aforoMax = aforoMax; }

    public BigDecimal getPrecioPersona() { return precioPersona; }
    public void setPrecioPersona(BigDecimal precioPersona) { this.precioPersona = precioPersona; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    @Override
    public String toString() {
        return "Sala{" + "id=" + id + ", nombre='" + nombre + '\'' + ", tematica='" + tematica + '\'' +
                ", dificultad=" + dificultad + ", aforoMax=" + aforoMax + ", precioPersona=" + precioPersona +
                ", activa=" + activa + '}';
    }
}