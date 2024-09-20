package de.godcipher.antiac.hibernate.repository.impl;

import de.godcipher.antiac.hibernate.HibernateUtil;
import de.godcipher.antiac.hibernate.entity.LogEntry;
import de.godcipher.antiac.hibernate.repository.LogEntryRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Slf4j
public class LogEntryRepositoryImpl implements LogEntryRepository {

  @Override
  public void save(LogEntry logEntry) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      session.merge(logEntry);
      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      log.error("Failed to save log entry", e);
    }
  }

  @Override
  public LogEntry findById(long id) {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      return session.get(LogEntry.class, id);
    }
  }

  @Override
  public List<LogEntry> findAll() {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      return session
          .createQuery("from de.godcipher.antiac.hibernate.entity.LogEntry", LogEntry.class)
          .list();
    }
  }

  @Override
  public void delete(LogEntry logEntry) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      session.remove(logEntry);
      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      log.error("Failed to delete log entry", e);
    }
  }
}
