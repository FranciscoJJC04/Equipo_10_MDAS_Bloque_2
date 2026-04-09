package es.uco.pw.pw2526.model.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
        try {
            BufferedReader reader;
            File f = new File(sqlQueriesFileName);
            reader = new BufferedReader(new FileReader(f));
            sqlQueries.load(reader);
        } catch (IOException e) {
            System.err.println("Error creating properties object for SQL queries");
            e.printStackTrace();
        }
    }

}
