package vista;

import conexion.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;

import servicio.ReservaServicio;
import servicio.CsvServicio;
import modelo.Reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {

    // Instanciamos los servicios globales que controlan la lógica
    private static final ReservaServicio reservaServicio = new ReservaServicio();
    private static final CsvServicio csvServicio = new CsvServicio();
    private static final Scanner teclado = new Scanner(System.in);

    // Formateadores estándar para interactuar con el usuario
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        int opcion = 0;

        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opción: ");

            switch (opcion) {
                case 1:
                    // El método listarActivas() devuelve una lista, lo recorremos con un stream o foreach
                    System.out.println("\n SALAS ACTIVAS ");
                    // Nota: Si quieres usar directamente el SalaDAO aquí, puedes instanciarlo.
                    // Para mantener la arquitectura, asumimos que se puede listar o mostrar.
                    new dao.SalaDAO().listarActivas().forEach(System.out::println);
                    break;

                case 2:
                    System.out.println("\n LISTAR RESERVAS POR FECHA ");
                    LocalDate fechaBusqueda = leerFecha();
                    reservaServicio.mostrarReservasConSala(fechaBusqueda);
                    break;

                case 3:
                    System.out.println("\n REALIZAR NUEVA RESERVA ");
                    int salaId = leerEntero("ID de la sala: ");
                    LocalDateTime fechaHora = leerFechaHora();
                    System.out.print("Nombre del grupo: ");
                    String grupo = teclado.nextLine();
                    int numJugadores = leerEntero("Número de jugadores: ");

                    reservaServicio.realizarReserva(salaId, fechaHora, grupo, numJugadores);
                    break;

                case 4:
                    System.out.println("\n CANCELAR RESERVA ");
                    int idCancelar = leerEntero("Introduce el ID de la reserva a eliminar: ");
                    boolean eliminado = new dao.ReservaDAO().eliminar(idCancelar);
                    if (eliminado) {
                        System.out.println("Reserva eliminada correctamente.");
                    } else {
                        System.out.println("No se pudo eliminar. Comprueba si el ID realmente existe.");
                    }
                    break;

                case 5:
                    System.out.println("\n IMPORTAR RESERVAS DESDE CSV ");
                    System.out.print("Introduce la ruta del fichero CSV (ej: reservas.csv): ");
                    String rutaImportar = teclado.nextLine();
                    csvServicio.importarReservas(rutaImportar);
                    break;

                case 6:
                    System.out.println("\n EXPORTAR RESERVAS DEL MES A CSV ");
                    int mes = leerEntero("Introduce el número del mes (1-12): ");
                    int anio = leerEntero("Introduce el año (ej: 2026): ");
                    System.out.print("Introduce la ruta donde guardar el archivo (ej: exportacion.csv): ");
                    String rutaExportar = teclado.nextLine();
                    csvServicio.exportarReservasMes(mes, anio, rutaExportar);
                    break;

                case 7:
                    System.out.println("\n GENERAR INFORME MENSUAL (Apartado 6) ");
                    generarInformeMensual();
                    break;

                case 8:
                    System.out.println("Saliendo del sistema...");
                    break;

                default:
                    System.out.println("Opción no válida. Por favor, elija un número del 1 al 8.");
            }
            System.out.println(); // Salto de línea estético entre operaciones

        } while (opcion != 8);
    }

    private static void mostrarMenu() {

        System.out.println("        SISTEMA ESCAPE ENIGMA            ");
        System.out.println("1. Listar salas activas");
        System.out.println("2. Listar reservas de una fecha");
        System.out.println("3. Realizar nueva reserva");
        System.out.println("4. Cancelar reserva (por ID)");
        System.out.println("5. Importar reservas desde CSV");
        System.out.println("6. Exportar reservas del mes a CSV");
        System.out.println("7. Generar informe mensual (Apartado 6)");
        System.out.println("8. Salir");
    }

    /**
     * VALIDACIÓN CRÍTICA: Lee un entero del teclado.
     * Si el usuario escribe letras, captura el error, limpia el buffer y vuelve a preguntar.
     */
    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                int valor = Integer.parseInt(teclado.nextLine().trim());
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes introducir un número entero válido.");
            }
        }
    }

    /**
     * VALIDACIÓN: Fuerza al usuario a introducir una fecha con el formato correcto.
     */
    private static LocalDate leerFecha() {
        while (true) {
            System.out.print("Introduce la fecha (formato AAAA-MM-DD, ej: 2026-06-15): ");
            try {
                return LocalDate.parse(teclado.nextLine().trim(), FORMATO_FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Error: Formato de fecha incorrecto. Inténtalo de nuevo.");
            }
        }
    }

    /**
     * VALIDACIÓN: Fuerza al usuario a introducir fecha y hora con el formato correcto.
     */
    private static LocalDateTime leerFechaHora() {
        while (true) {
            System.out.print("Introduce fecha y hora (formato AAAA-MM-DD HH:MM, ej: 2026-06-15 18:30): ");
            try {
                return LocalDateTime.parse(teclado.nextLine().trim(), FORMATO_FECHA_HORA);
            } catch (DateTimeParseException e) {
                System.out.println("Error: Formato de fecha/hora incorrecto. Inténtalo de nuevo.");
            }
        }
    }

    /**
     * Método comodín para el Apartado 6.
     * Complétalo con la lógica específica que te pida ese punto del examen.
     */
    private static void generarInformeMensual() {
        int mes = leerEntero("Introduce el número del mes (1-12): ");
        int anio = leerEntero("Introduce el año (ej: 2026): ");
        System.out.print("Introduce la ruta y nombre del archivo para el informe (ej: informe_junio.csv): ");
        String rutaFichero = teclado.nextLine();

        // Llamamos al método que acabamos de crear
        reservaServicio.generarInformeMensual(mes, anio, rutaFichero);
    }

}



