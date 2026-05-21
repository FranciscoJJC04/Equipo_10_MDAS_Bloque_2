package es.uco.pw.pw2526.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import es.uco.pw.pw2526.model.repository.AbstractRepository;

@Component
public class SqlQueriesInitializer implements ApplicationRunner {

    @Value("${app.sql.queries:db/sql.properties}")
    private String sqlQueriesFileName;

    private final ApplicationContext applicationContext;

    public SqlQueriesInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            Map<String, AbstractRepository> repos = applicationContext.getBeansOfType(AbstractRepository.class);
            for (AbstractRepository repo : repos.values()) {
                repo.setSQLQueriesFileName(sqlQueriesFileName);
            }
        } catch (Exception e) {
            System.err.println("Warning: no se pudieron inicializar automáticamente las SQL properties: " + e.getMessage());
        }
    }
}
