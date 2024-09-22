package de.godcipher.antiac.hibernate.repository.impl;

import de.godcipher.antiac.hibernate.HibernateUtil;
import de.godcipher.antiac.hibernate.cache.LogEntryCacheService;
import de.godcipher.antiac.hibernate.entity.LogEntry;
import de.godcipher.antiac.hibernate.repository.LogEntryRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Slf4j
public class LogEntryRepositoryImpl implements LogEntryRepository {

  private final LogEntryCacheService cacheService = new LogEntryCacheService();

  @Override
  public void save(LogEntry logEntry) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      session.merge(logEntry);
      transaction.commit();
      cacheService.save(logEntry);
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      log.error("Failed to save log entry", e);
    }
  }

  @Override
  public LogEntry findById(long id) {
    LogEntry logEntry = cacheService.findById(id);
    if (logEntry == null) {
      try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        logEntry = session.get(LogEntry.class, id);
        if (logEntry != null) {
          cacheService.save(logEntry);
        }
      }
    }
    return logEntry;
  }

  @Override
  public List<LogEntry> findAll() {
    List<LogEntry> logEntries = cacheService.findAll();
    if (logEntries.isEmpty()) {
      try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        logEntries =
            session
                .createQuery("from de.godcipher.antiac.hibernate.entity.LogEntry", LogEntry.class)
                .list();
        logEntries.forEach(cacheService::save);
      }
    }
    return logEntries;
  }

  @Override
  public void delete(LogEntry logEntry) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      transaction = session.beginTransaction();
      session.remove(logEntry);
      transaction.commit();
      cacheService.delete(logEntry);
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
      }
      log.error("Failed to delete log entry", e);
    }
  }
}
