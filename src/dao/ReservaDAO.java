package dao;

import conexion.ConexionBD;
import modelo.Reserva;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {

    public int insertar(Reserva r) {
        String sql = "INSERT INTO reservas (sala_id, fecha_hora, nombre_grupo, num_jugadores, completada) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getSalaId());
            // Mapeo de LocalDateTime a Timestamp de SQL para mantener fecha y hora
            ps.setTimestamp(2, Timestamp.valueOf(r.getFechaHora()));
            ps.setString(3, r.getNombreGrupo());
            ps.setInt(4, r.getNumJugadores());
            ps.setBoolean(5, r.isCompletada());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar reserva: " + e.getMessage());
        }
        return -1;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM reservas WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar reserva: " + e.getMessage());
            return false;
        }
    }

    /**
     * Como el enunciado pide listar por 'LocalDate fecha' (solo el día) pero la base de datos
     * guarda un 'DATETIME', filtramos usando la función DATE() de SQL para comparar solo la fecha.
     */
    public List<Reserva> listarPorFecha(LocalDate fecha) {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM reservas WHERE DATE(fecha_hora) = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reserva reserva = new Reserva(
                            rs.getInt("id"),
                            rs.getInt("sala_id"),
                            rs.getTimestamp("fecha_hora").toLocalDateTime(), // Recuperamos como LocalDateTime
                            rs.getString("nombre_grupo"),
                            rs.getInt("num_jugadores"),
                            rs.getBoolean("completada")
                    );
                    lista.add(reserva);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar reservas por fecha: " + e.getMessage());
        }
        return lista;
    }
}
