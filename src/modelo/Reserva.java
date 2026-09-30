package modelo;

import java.time.LocalDateTime;

public class Reserva {
    private int id;
    private int salaId;
    private LocalDateTime fechaHora; // Cambiado a LocalDateTime por el tipo DATETIME de SQL
    private String nombreGrupo;
    private int numJugadores;
    private boolean completada;

    // Constructor vacío
    public Reserva() {}

    // Constructor completo
    public Reserva(int id, int salaId, LocalDateTime fechaHora, String nombreGrupo, int numJugadores, boolean completada) {
        this.id = id;
        this.salaId = salaId;
        this.fechaHora = fechaHora;
        this.nombreGrupo = nombreGrupo;
        this.numJugadores = numJugadores;
        this.completada = completada;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSalaId() { return salaId; }
    public void setSalaId(int salaId) { this.salaId = salaId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getNombreGrupo() { return nombreGrupo; }
    public void setNombreGrupo(String nombreGrupo) { this.nombreGrupo = nombreGrupo; }

    public int getNumJugadores() { return numJugadores; }
    public void setNumJugadores(int numJugadores) { this.numJugadores = numJugadores; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }

    @Override
    public String toString() {
        return "Reserva{" + "id=" + id + ", salaId=" + salaId + ", fechaHora=" + fechaHora +
                ", nombreGrupo='" + nombreGrupo + '\'' + ", numJugadores=" + numJugadores +
                ", completada=" + completada + '}';
    }
}