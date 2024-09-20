package de.godcipher.antiac.hibernate.repository.impl;

import de.godcipher.antiac.hibernate.HibernateUtil;
import de.godcipher.antiac.hibernate.entity.LogEntry;
import de.godcipher.antiac.hibernate.repository.LogEntryRepository;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class LogEntryRepositoryImpl implements LogEntryRepository {

  @Override
  public void save(LogEntry logEntry) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      session.saveOrUpdate(logEntry);
      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      e.printStackTrace();
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
      return session.createQuery("from LogEntry", LogEntry.class).list();
    }
  }

  @Override
  public void delete(LogEntry logEntry) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      session.delete(logEntry);
      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      e.printStackTrace();
    }
  }
}
