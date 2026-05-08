package com.sena.springpoo.persistence;

import com.sena.springpoo.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class conexion {

    private static final Logger logger = LoggerFactory.getLogger(conexion.class);

    private static final String URL;
    private static final String USUARIO;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream is = conexion.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            logger.error("No se pudo cargar application.properties", e);
        }

        String host = props.getProperty("spring.datasource.url",
                "jdbc:mysql://localhost:3306/springpoo_db");
        if (!host.contains("?")) {
            host = host + "?connectTimeout=3000&socketTimeout=3000"
                    + "&useSSL=false&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=America/Bogota";
        }
        URL      = host;
        USUARIO  = props.getProperty("spring.datasource.username", "root");
        PASSWORD = props.getProperty("spring.datasource.password", "");
    }

    private static Connection instancia = null;

    private conexion() {}

    public static Connection getInstancia() {
        try {
            if (instancia == null || instancia.isClosed() || !instancia.isValid(2)) {
                instancia = null;
                Class.forName("com.mysql.cj.jdbc.Driver");
                instancia = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                logger.info("Conexión con MySQL establecida correctamente.");
            }
        } catch (ClassNotFoundException e) {
            instancia = null;
            logger.error("Driver de MySQL no encontrado.", e);
            throw new DatabaseException("Driver de MySQL no encontrado: " + e.getMessage(), e);
        } catch (SQLException e) {
            instancia = null;
            logger.error("No se pudo conectar con MySQL. Verifique que el servidor esté activo.", e);
            throw new DatabaseException("No se pudo conectar con MySQL. Verifique que esté activo.", e);
        }
        return instancia;
    }

    public static void cerrar() {
        try {
            if (instancia != null && !instancia.isClosed()) {
                instancia.close();
                logger.info("Conexión con MySQL cerrada.");
            }
        } catch (SQLException e) {
            logger.error("Error al cerrar la conexión con MySQL.", e);
        }
    }
}