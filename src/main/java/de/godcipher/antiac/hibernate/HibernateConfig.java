package de.godcipher.antiac.hibernate;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.hibernate.enums.DatabaseDialect;
import de.godcipher.antiac.hibernate.enums.DatabaseDriver;
import de.godcipher.comet.Configuration;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

public class HibernateConfig {

  private static final String DATABASE_URL = "database-url";
  private static final String DATABASE_USERNAME = "database-username";
  private static final String DATABASE_PASSWORD = "database-password";
  private static final String DATABASE_DRIVER = "database-driver";
  private static final String DATABASE_DIALECT = "database-dialect";

  public static StandardServiceRegistry getHibernateConfiguration() {
    Configuration config = AntiAC.getInstance().getConfiguration();
    Map<String, Object> settings = getHibernateSettings(config);
    return buildServiceRegistry(settings);
  }

  private static Map<String, Object> getHibernateSettings(Configuration config) {
    Map<String, Object> settings = new HashMap<>();
    settings.put(Environment.DRIVER, getDriverClass(config));
    settings.put(Environment.DIALECT, getDialectClass(config));
    settings.put(Environment.URL, config.getConfigOption(DATABASE_URL).getValue());
    settings.put(Environment.USER, config.getConfigOption(DATABASE_USERNAME).getValue());
    settings.put(Environment.PASS, config.getConfigOption(DATABASE_PASSWORD).getValue());
    settings.put(Environment.HBM2DDL_AUTO, "update");
    return settings;
  }

  private static String getDriverClass(Configuration config) {
    String driver = (String) config.getConfigOption(DATABASE_DRIVER).getValue();
    return DatabaseDriver.valueOf(driver.toUpperCase()).getDriverClass();
  }

  private static String getDialectClass(Configuration config) {
    String dialect = (String) config.getConfigOption(DATABASE_DIALECT).getValue();
    return DatabaseDialect.valueOf(dialect.toUpperCase()).getDialectClass();
  }

  private static StandardServiceRegistry buildServiceRegistry(Map<String, Object> settings) {
    StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
    registryBuilder.applySettings(settings);
    return registryBuilder.build();
  }
}
