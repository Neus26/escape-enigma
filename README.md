# Escape Rooms Enigma

Aplicación de consola en Java para gestionar las salas y las reservas de una empresa de escape rooms.

La hice en 1º de DAM como simulacro del examen final de Programación. La idea era practicar JDBC con MySQL, transacciones y lectura/escritura de ficheros CSV.

## Qué se puede hacer

Al arrancar sale un menú con estas opciones:

1. Listar salas activas
2. Listar reservas de una fecha
3. Hacer una reserva nueva
4. Cancelar una reserva por su id
5. Importar reservas desde un CSV
6. Exportar a CSV las reservas de un mes
7. Generar un informe mensual en CSV
8. Salir

## Cómo está hecho

El código está separado en paquetes: `modelo` para las clases Sala y Reserva, `dao` para las consultas a la base de datos, `servicio` para la lógica, y `vista` con el menú (`Main`). La conexión se lee de un fichero `db.properties`.

Todas las consultas usan PreparedStatement.

Lo más trabajado es hacer una reserva (`realizarReserva` en ReservaServicio). Va dentro de una transacción y antes de insertar comprueba que la sala existe y está activa, que no se pasa del aforo y que no hay otra reserva en esa sala 90 minutos antes o después. Si falla algo hace rollback, y al final siempre vuelve a poner el autoCommit a true.

La importación llama a `realizarReserva` por cada línea del CSV y al terminar dice cuántas han entrado y cuántas no.

El informe mensual sale de una sola consulta con JOIN y GROUP BY, y saca por cada sala el número de reservas, los jugadores, lo que se ha ingresado y la ocupación media.

Los CSV van separados por `;` y en UTF-8.

## Cómo ejecutarlo

Necesitas Java 25, MySQL y el conector MySQL Connector/J.

1. Ejecuta el script `sql/escape_enigma.sql` en MySQL (crea la base de datos y mete datos de prueba).
2. Copia `src/db.properties.example` como `src/db.properties` y pon tu usuario y contraseña.
3. Abre el proyecto en IntelliJ y añade el jar del conector en File > Project Structure > Libraries.
4. Ejecuta `vista.Main`.

Para probar la importación (opción 5) puedes usar `ejemplos/reservas_importar.csv`. Las tres primeras líneas deberían entrar bien y las otras cuatro fallar: una por aforo, una porque la sala está cerrada, otra porque la sala no existe y la última porque se solapa con una reserva que ya hay.

El formato para importar es `sala_id;fecha_hora;nombre_grupo;num_jugadores`, sin cabecera y con la fecha como `2026-06-05 18:00:00`.
