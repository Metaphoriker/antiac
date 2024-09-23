package de.godcipher.antiac.hibernate;

import de.godcipher.antiac.hibernate.entity.LogEntry;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;

@Slf4j
public class HibernateUtil {

  private static SessionFactory sessionFactory;

  public static void setupHibernate() {
    try {
      StandardServiceRegistry registry = HibernateConfig.getHibernateConfiguration();
      MetadataSources sources = new MetadataSources(registry);
      sources.addAnnotatedClass(LogEntry.class);

      sessionFactory = sources.buildMetadata().buildSessionFactory();
    } catch (Exception e) {
      log.error("Initial SessionFactory creation failed", e);
      throw new ExceptionInInitializerError("Initial SessionFactory creation failed" + e);
    }
  }

  public static void shutdown() {
    if (sessionFactory != null) {
      sessionFactory.close();
    }
  }
}
