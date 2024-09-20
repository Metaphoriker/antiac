package de.godcipher.antiac.hibernate;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

@Slf4j
public class HibernateUtil {

  @Getter private static SessionFactory sessionFactory;

  public static void setupHibernate() {
    try {
      Configuration configuration = HibernateConfig.getHibernateConfiguration();
      ServiceRegistry serviceRegistry =
          new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
      sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    } catch (Exception e) {
      log.error("Initial SessionFactory creation failed", e);
      throw new ExceptionInInitializerError("Initial SessionFactory creation failed" + e);
    }
  }

  public static void shutdown() {
    getSessionFactory().close();
  }
}
