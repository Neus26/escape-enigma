package dao;

import conexion.ConexionBD;
import modelo.Sala;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaDAO {

    public Sala buscarPorId(int id) {
        String sql = "SELECT * FROM salas WHERE id = ?";
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Sala(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("tematica"),
                            rs.getInt("dificultad"),
                            rs.getInt("aforo_max"),
                            rs.getBigDecimal("precio_persona"),
                            rs.getBoolean("activa")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar sala por ID: " + e.getMessage());
        }
        return null;
    }

    public List<Sala> listarActivas() {
        List<Sala> lista = new ArrayList<>();
        String sql = "SELECT * FROM salas WHERE activa = TRUE";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Sala sala = new Sala(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("tematica"),
                        rs.getInt("dificultad"),
                        rs.getInt("aforo_max"),
                        rs.getBigDecimal("precio_persona"),
                        rs.getBoolean("activa")
                );
                lista.add(sala);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar salas activas: " + e.getMessage());
        }
        return lista;
    }
}