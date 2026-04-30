package es.uco.pw.pw2526.model.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Clase base para repositorios que necesitan ejecutar consultas SQL
 * externalizadas en un fichero de propiedades.
 */
public class AbstractRepository {
    protected JdbcTemplate jdbcTemplate;
    protected Properties sqlQueries;
    protected String sqlQueriesFileName;

    /**
     * Establece el nombre (o path) del fichero de properties que contiene las
     * consultas SQL y fuerza la creación/carga de las properties.
     *
     * @param sqlQueriesFileName ruta o nombre del fichero de properties
     */
    public void setSQLQueriesFileName(String sqlQueriesFileName) {
        this.sqlQueriesFileName = sqlQueriesFileName;
        createProperties();
    }

    /**
     * Carga las properties desde el fichero indicado. Actualmente la carga se
     * realiza desde el sistema de ficheros usando la ruta proporcionada.
     */
    private void createProperties() {
        sqlQueries = new Properties();
        boolean loaded = false;
        try {
            File f = new File(sqlQueriesFileName);
            if (f.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                    sqlQueries.load(reader);
                    loaded = true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error creating properties object for SQL queries");
            e.printStackTrace();
        }

        if (!loaded) {
            String resourcePath = sqlQueriesFileName;
            if (resourcePath.startsWith("./src/main/resources/")) {
                resourcePath = resourcePath.substring("./src/main/resources/".length());
            }
            if (resourcePath.startsWith("src/main/resources/")) {
                resourcePath = resourcePath.substring("src/main/resources/".length());
            }
            if (resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring(1);
            }

            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (inputStream != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        sqlQueries.load(reader);
                        loaded = true;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error creating properties object for SQL queries from classpath");
                e.printStackTrace();
            }
        }

        if (!loaded) {
            System.err.println("Error creating properties object for SQL queries: file not found " + sqlQueriesFileName);
        }
    }

}
