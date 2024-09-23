package de.godcipher.antiac.hibernate;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.config.Configuration;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

public class HibernateConfig {

  public static StandardServiceRegistry getHibernateConfiguration() {
    Configuration config = AntiAC.getInstance().getConfiguration();
    String url = config.getConfigOption("database-url").asString();
    String user = config.getConfigOption("database-username").asString();
    String password = config.getConfigOption("database-password").asString();
    String driver = config.getConfigOption("database-driver").asString();
    String dialect = config.getConfigOption("database-dialect").asString();

    String driverName = DatabaseDriver.valueOf(driver.toUpperCase()).getDriverClass();
    String dialectName = DatabaseDialect.valueOf(dialect.toUpperCase()).getDialectClass();

    Map<String, Object> settings = new HashMap<>();
    settings.put(Environment.DRIVER, driverName);
    settings.put(Environment.DIALECT, dialectName);
    settings.put(Environment.URL, url);
    settings.put(Environment.USER, user);
    settings.put(Environment.PASS, password);

    StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
    registryBuilder.applySettings(settings);

    return registryBuilder.build();
  }
}
