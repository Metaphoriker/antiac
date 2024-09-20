package de.godcipher.antiac.hibernate;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.hibernate.entity.LogEntry;
import org.hibernate.cfg.Configuration;

public class HibernateConfig {

  public static Configuration getHibernateConfiguration() {
    Configuration configuration = new Configuration();

    de.godcipher.antiac.config.Configuration config = AntiAC.getInstance().getConfiguration();
    String url = config.getConfigOption("database.url").asString();
    String user = config.getConfigOption("database.user").asString();
    String password = config.getConfigOption("database.password").asString();
    String driver = config.getConfigOption("database.driver").asString();
    String dialect = config.getConfigOption("database.dialect").asString();

    configuration.setProperty("hibernate.connection.url", url);
    configuration.setProperty("hibernate.connection.username", user);
    configuration.setProperty("hibernate.connection.password", password);
    configuration.setProperty("hibernate.connection.driver_class", driver);
    configuration.setProperty("hibernate.dialect", dialect);

    configuration.addAnnotatedClass(LogEntry.class);

    return configuration;
  }
}
