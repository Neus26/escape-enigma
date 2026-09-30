package conexion;

import java.io.*;
import java.sql.*;
import java.util.Properties;
public class ConexionBD {
    private static String url, usuario, clave;
    static {
        try (InputStream is = ConexionBD.class.getResourceAsStream("/db.properties")) {
            Properties props = new Properties();
            props.load(is);
            url = props.getProperty("db.url");
            usuario = props.getProperty("db.usuario");
            clave = props.getProperty("db.clave");
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar db.properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }
}
