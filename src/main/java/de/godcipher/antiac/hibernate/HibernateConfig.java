package de.godcipher.antiac.hibernate;

import de.godcipher.antiac.AntiAC;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

import java.util.HashMap;
import java.util.Map;

public class HibernateConfig {

  public static StandardServiceRegistry getHibernateConfiguration() {
    de.godcipher.antiac.config.Configuration config = AntiAC.getInstance().getConfiguration();
    String url = config.getConfigOption("database-url").asString();
    String user = config.getConfigOption("database-username").asString();
    String password = config.getConfigOption("database-password").asString();
    String driver = config.getConfigOption("database-driver").asString();
    String dialect = config.getConfigOption("database-dialect").asString();

    Map<String, Object> settings = new HashMap<>();
    settings.put(Environment.DRIVER, driver);
    settings.put(Environment.URL, url);
    settings.put(Environment.USER, user);
    settings.put(Environment.PASS, password);
    settings.put(Environment.DIALECT, dialect);
    settings.put(Environment.HBM2DDL_AUTO, "update");

    StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
    registryBuilder.applySettings(settings);

    return registryBuilder.build();
  }
}
