package de.godcipher.antiac.hibernate;

import lombok.Getter;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {

  @Getter private static SessionFactory sessionFactory;

  public static void setupHibernate() {
    try {
      Configuration configuration = HibernateConfig.getHibernateConfiguration();
      ServiceRegistry serviceRegistry =
          new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
      sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError("Initial SessionFactory creation failed" + e);
    }
  }

  public static void shutdown() {
    getSessionFactory().close();
  }
}
