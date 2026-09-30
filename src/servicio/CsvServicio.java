package servicio;

import conexion.ConexionBD;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
public class CsvServicio {

    private final ReservaServicio reservaServicio = new ReservaServicio();
    // Formateador para entender las fechas del CSV (ej: "2026-06-15 18:30:00")
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * EXPORTAR: Genera un CSV con las reservas de un mes y año específicos.
     * Calcula el importe_total dinámicamente en la consulta SQL.
     */
    public void exportarReservasMes(int mes, int anio, String rutaFichero) {
        // Consulta única con JOIN que calcula el importe total en caliente (num_jugadores * precio_persona)
        String sql = "SELECT r.id, s.nombre AS sala_nombre, s.tematica, r.fecha_hora, r.nombre_grupo, r.num_jugadores, " +
                "(r.num_jugadores * s.precio_persona) AS importe_total " +
                "FROM reservas r " +
                "INNER JOIN salas s ON r.sala_id = s.id " +
                "WHERE MONTH(r.fecha_hora) = ? AND YEAR(r.fecha_hora) = ?";

        // Forzamos el uso de UTF-8 al escribir el archivo
        try (Connection con = ConexionBD.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rutaFichero), StandardCharsets.UTF_8))) {

            ps.setInt(1, mes);
            ps.setInt(2, anio);

            try (ResultSet rs = ps.executeQuery()) {
                // Escribir la cabecera requerida utilizando ';' como separador
                bw.write("id_reserva;sala_nombre;tematica;fecha_hora;grupo;num_jugadores;importe_total");
                bw.newLine();

                int contador = 0;
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String salaNombre = rs.getString("sala_nombre");
                    String tematica = rs.getString("tematica");
                    LocalDateTime fechaHora = rs.getTimestamp("fecha_hora").toLocalDateTime();
                    String grupo = rs.getString("nombre_grupo");
                    int numJugadores = rs.getInt("num_jugadores");
                    BigDecimal importeTotal = rs.getBigDecimal("importe_total");

                    // Construimos la línea formateada
                    String linea = String.format("%d;%s;%s;%s;%s;%d;%.2f",
                            id, salaNombre, tematica, fechaHora.format(formatter), grupo, numJugadores, importeTotal);

                    // Reemplazamos posibles comas decimales por puntos si tu entorno local cambia el formato de %.2f
                    linea = linea.replace(',', '.');

                    bw.write(linea);
                    bw.newLine();
                    contador++;
                }
                System.out.println("Exportación completada con éxito. Se exportaron " + contador + " registros a: " + rutaFichero);
            }

        } catch (SQLException e) {
            System.err.println("Error en la base de datos al exportar: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error de archivo al escribir el CSV: " + e.getMessage());
        }
    }

    /**
     * IMPORTAR: Lee un CSV sin cabecera e intenta registrar las reservas.
     * Muestra un informe detallado con los éxitos y los motivos específicos de los fallos.
     */
    public void importarReservas(String rutaFichero) {
        int exitos = 0;
        int fallos = 0;

        File fichero = new File(rutaFichero);
        if (!fichero.exists()) {
            System.err.println("El archivo especificado no existe en la ruta: " + rutaFichero);
            return;
        }

        // Forzamos la lectura en UTF-8
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fichero), StandardCharsets.UTF_8))) {
            String linea;
            int nLinea = 0;

            System.out.println("=== Iniciando proceso de importación ===");

            while ((linea = br.readLine()) != null) {
                nLinea++;
                if (linea.trim().isEmpty()) continue; // Saltamos líneas vacías

                // Separamos los datos por punto y coma ';'
                String[] datos = linea.split(";");

                if (datos.length < 4) {
                    System.out.println("[Fila " + nLinea + " - FALLO] Estructura de columnas incorrecta.");
                    fallos++;
                    continue;
                }

                try {
                    // Parseo de los campos del CSV
                    int salaId = Integer.parseInt(datos[0].trim());
                    LocalDateTime fechaHora = LocalDateTime.parse(datos[1].trim(), formatter);
                    String nombreGrupo = datos[2].trim();
                    int numJugadores = Integer.parseInt(datos[3].trim());

                    // Reutilizamos el método realizarReserva del Apartado 3
                    // Nota: Como realizarReserva ya imprime internamente su propio mensaje de "Validación Fallida",
                    // aquí capturamos el booleano resultante para nuestro conteo final.
                    boolean guardado = reservaServicio.realizarReserva(salaId, fechaHora, nombreGrupo, numJugadores);

                    if (guardado) {
                        exitos++;
                        System.out.println("[Fila " + nLinea + " - OK] Reserva del grupo '" + nombreGrupo + "' importada.");
                    } else {
                        fallos++;
                        // El motivo exacto ya fue impreso en consola por la lógica de realizarReserva(...)
                        System.out.println("[Fila " + nLinea + " - FALLO] No se pudo insertar la reserva en la BD.");
                    }

                } catch (Exception e) {
                    System.out.println("[Fila " + nLinea + " - FALLO] Error de formato en los datos. Detalle: " + e.getMessage());
                    fallos++;
                }
            }

            // --- RESUMEN FINAL ---
            System.out.println("\n=================================");
            System.out.println("   RESUMEN DE LA IMPORTACIÓN     ");
            System.out.println("=================================");
            System.out.println("Reservas importadas con éxito: " + exitos);
            System.out.println("Reservas fallidas:             " + fallos);
            System.out.println("Total de filas procesadas:     " + (exitos + fallos));
            System.out.println("=================================");

        } catch (IOException e) {
            System.err.println("Error crítico al leer el archivo CSV: " + e.getMessage());
        }
    }
}