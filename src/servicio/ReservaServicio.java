package servicio;

import conexion.ConexionBD;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservaServicio {

    /**
     * Apartado 6: Genera un informe mensual estadístico en formato CSV.
     * Utiliza una única consulta con JOIN, GROUP BY y funciones de agregado.
     */
    public void generarInformeMensual(int mes, int anio, String rutaFichero) {
        // Consulta única SQL con JOIN, GROUP BY y funciones de agregado agrupadas por sala
        String sql = "SELECT s.nombre AS sala_nombre, s.tematica, " +
                "COUNT(r.id) AS num_reservas, " +
                "SUM(r.num_jugadores) AS total_jugadores, " +
                "SUM(r.num_jugadores * s.precio_persona) AS ingresos_totales, " +
                "AVG(r.num_jugadores * 100.0 / s.aforo_max) AS ocupacion_media " +
                "FROM reservas r " +
                "INNER JOIN salas s ON r.sala_id = s.id " +
                "WHERE MONTH(r.fecha_hora) = ? AND YEAR(r.fecha_hora) = ? " +
                "GROUP BY s.id, s.nombre, s.tematica " +
                "ORDER BY ingresos_totales DESC";

        // Variables para acumular los totales de la última fila
        int sumaReservas = 0;
        int sumaJugadores = 0;
        double sumaIngresos = 0.0;

        // Forzamos codificación UTF-8 para evitar problemas con tildes (como en 'Histórica')
        try (Connection con = conexion.ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new java.io.FileOutputStream(rutaFichero), java.nio.charset.StandardCharsets.UTF_8))) {

            ps.setInt(1, mes);
            ps.setInt(2, anio);

            try (ResultSet rs = ps.executeQuery()) {
                // 1. Escribir la cabecera del informe con el formato solicitado
                bw.write("INFORME ENIGMA - " + mes + "/" + anio);
                bw.newLine();
                bw.write("sala;tematica;num_reservas;total_jugadores;ingresos_totales;ocupacion_media");
                bw.newLine();

                // 2. Procesar cada fila de la consulta
                while (rs.next()) {
                    String sala = rs.getString("sala_nombre");
                    String tematica = rs.getString("tematica");
                    int numReservas = rs.getInt("num_reservas");
                    int totalJugadores = rs.getInt("total_jugadores");
                    double ingresosTotales = rs.getDouble("ingresos_totales");
                    double ocupacionMedia = rs.getDouble("ocupacion_media");

                    // Acumulamos para la fila de totales finales
                    sumaReservas += numReservas;
                    sumaJugadores += totalJugadores;
                    sumaIngresos += ingresosTotales;

                    // Formateamos los números según los requisitos:
                    // Usamos la localización de España (es) o reemplazo directo para asegurar la coma decimal (,)
                    String ingresosFormateados = String.format(java.util.Locale.GERMAN, "%.2f", ingresosTotales);
                    String ocupacionFormateada = String.format(java.util.Locale.GERMAN, "%.2f", ocupacionMedia);

                    // Construimos la línea usando ';' como separador
                    String linea = String.format("%s;%s;%d;%d;%s;%s",
                            sala, tematica, numReservas, totalJugadores, ingresosFormateados, ocupacionFormateada);

                    bw.write(linea);
                    bw.newLine();
                }

                // 3. Escribir la última línea de TOTALES
                // Estructura requerida: TOTALES;;<suma_reservas>;<suma_jugadores>;<suma_ingresos>;
                // Nota el doble punto y coma tras TOTALES y el punto y coma final vacío para ocupación media.
                String ingresosSumadosFormateados = String.format(java.util.Locale.GERMAN, "%.2f", sumaIngresos);
                String lineaTotales = String.format("TOTALES;;%d;%d;%s;",
                        sumaReservas, sumaJugadores, ingresosSumadosFormateados);

                bw.write(lineaTotales);
                bw.newLine();

                System.out.println("Informe mensual generado con éxito en: " + rutaFichero);
            }

        } catch (SQLException e) {
            System.err.println("Error de base de datos al generar el informe: " + e.getMessage());
        } catch (java.io.IOException e) {
            System.err.println("Error de archivo al escribir el informe CSV: " + e.getMessage());
        }
    }

    /**
     * Muestra todas las reservas de una fecha dada incluyendo el nombre y la temática de la sala.
     * Resuelto con una única consulta usando INNER JOIN.
     */

    public void mostrarReservasConSala(LocalDate fecha) {
        // Seleccionamos los datos de la reserva y los datos específicos de la sala que nos piden
        String sql = "SELECT r.id, r.fecha_hora, r.nombre_grupo, r.num_jugadores, r.completada, " +
                "s.nombre AS sala_nombre, s.tematica " +
                "FROM reservas r " +
                "INNER JOIN salas s ON r.sala_id = s.id " +
                "WHERE DATE(r.fecha_hora) = ?";

        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("=== Reservas para el día: " + fecha + " ===");
                boolean tieneReservas = false;

                while (rs.next()) {
                    tieneReservas = true;
                    int id = rs.getInt("id");
                    LocalDateTime fechaHora = rs.getTimestamp("fecha_hora").toLocalDateTime();
                    String grupo = rs.getString("nombre_grupo");
                    int jugadores = rs.getInt("num_jugadores");
                    String sala = rs.getString("sala_nombre");
                    String tematica = rs.getString("tematica");

                    System.out.printf("Reserva ID: %d | Hora: %s | Grupo: %s (%d jug.) | Sala: %s [%s]%n",
                            id, fechaHora.toLocalTime(), grupo, jugadores, sala, tematica);
                }

                if (!tieneReservas) {
                    System.out.println("No hay reservas registradas para esta fecha.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al mostrar las reservas con sala: " + e.getMessage());
        }
    }

    /**
     * Realiza una reserva bajo una única transacción controlada.
     * Aplica validaciones de existencia, aforo y solapamiento horario (±90 min).
     */
    public boolean realizarReserva(int salaId, LocalDateTime fechaHora, String grupo, int numJugadores) {
        String sqlBuscarSala = "SELECT activa, aforo_max FROM salas WHERE id = ?";

        // Contamos cuántas reservas hay en la misma sala en el rango de (fechaHora - 90 min) hasta (fechaHora + 90 min)
        String sqlVerificarHorario = "SELECT COUNT(*) FROM reservas " +
                "WHERE sala_id = ? " +
                "AND fecha_hora BETWEEN ? AND ?";

        String sqlInsertar = "INSERT INTO reservas (sala_id, fecha_hora, nombre_grupo, num_jugadores, completada) " +
                "VALUES (?, ?, ?, ?, FALSE)";

        Connection con = null;

        try {
            con = ConexionBD.getConnection();
            // 1. Iniciamos la transacción desactivando el autoCommit
            con.setAutoCommit(false);

            // --- REQUISITO A y B: Comprobar que la sala existe, está activa y tiene aforo ---
            try (PreparedStatement psSala = con.prepareStatement(sqlBuscarSala)) {
                psSala.setInt(1, salaId);
                try (ResultSet rsSala = psSala.executeQuery()) {
                    if (!rsSala.next()) {
                        System.out.println("[Validación Fallida] La sala no existe.");
                        con.rollback(); // Cancelamos
                        return false;
                    }

                    boolean activa = rsSala.getBoolean("activa");
                    int aforoMax = rsSala.getInt("aforo_max");

                    if (!activa) {
                        System.out.println("[Validación Fallida] La sala no está activa.");
                        con.rollback();
                        return false;
                    }

                    if (numJugadores > aforoMax) {
                        System.out.println("[Validación Fallida] El número de jugadores excede el aforo máximo (" + aforoMax + ").");
                        con.rollback();
                        return false;
                    }
                }
            }

            // --- REQUISITO C: Comprobar margen de ±90 minutos ---
            // Calculamos los límites de tiempo en Java de forma limpia
            LocalDateTime limiteInferior = fechaHora.minusMinutes(90);
            LocalDateTime limiteSuperior = fechaHora.plusMinutes(90);

            try (PreparedStatement psHorario = con.prepareStatement(sqlVerificarHorario)) {
                psHorario.setInt(1, salaId);
                psHorario.setTimestamp(2, Timestamp.valueOf(limiteInferior));
                psHorario.setTimestamp(3, Timestamp.valueOf(limiteSuperior));

                try (ResultSet rsHorario = psHorario.executeQuery()) {
                    if (rsHorario.next() && rsHorario.getInt(1) > 0) {
                        System.out.println("[Validación Fallida] Ya existe una reserva en esa sala dentro del margen de ±90 minutos.");
                        con.rollback();
                        return false;
                    }
                }
            }

            // --- SI TODO ES CORRECTO: Insertar ---
            try (PreparedStatement psInsertar = con.prepareStatement(sqlInsertar)) {
                psInsertar.setInt(1, salaId);
                psInsertar.setTimestamp(2, Timestamp.valueOf(fechaHora));
                psInsertar.setString(3, grupo);
                psInsertar.setInt(4, numJugadores);

                psInsertar.executeUpdate();
            }

            // 2. Si llegamos aquí sin errores, consolidamos los cambios en la BD
            con.commit();
            System.out.println("¡Reserva realizada con éxito!");
            return true;

        } catch (SQLException e) {
            System.err.println("Error en la transacción. Ejecutando rollback... Detalle: " + e.getMessage());
            // Si hubo un error SQL intermedio, nos aseguramos de hacer rollback
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al ejecutar el rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            // 3. REQUISITO CRÍTICO: El bloque finally siempre se ejecuta, garantizando restaurar el autoCommit
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close(); // Buena práctica: cerrar la conexión si no usas pool de conexiones externo
                } catch (SQLException ex) {
                    System.err.println("Error al restaurar el autoCommit o cerrar conexión: " + ex.getMessage());
                }
            }
        }
    }

}