package de.godcipher.antiac.hibernate.repository.impl;

import de.godcipher.antiac.hibernate.HibernateUtil;
import de.godcipher.antiac.hibernate.cache.CacheUpdater;
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
  private final CacheUpdater cacheUpdater = new CacheUpdater(this, cacheService);

  public void startCacheUpdater() {
    cacheUpdater.start();
  }

  public void shutdownCacheUpdater() {
    cacheUpdater.stop();
  }

  @Override
  public void save(LogEntry logEntry) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.openSession()) {
      transaction = session.beginTransaction();
      session.merge(logEntry);
      transaction.commit();
      cacheService.save(logEntry);
    } catch (Exception e) {
      if (transaction != null && transaction.getStatus().canRollback()) {
        try {
          transaction.rollback();
        } catch (Exception rollbackException) {
          log.error("Failed to rollback transaction", rollbackException);
        }
      }
      log.error("Failed to save log entry", e);
    }
  }

  @Override
  public LogEntry findById(long id) {
    LogEntry logEntry = cacheService.findById(id);
    if (logEntry == null) {
      try (Session session = HibernateUtil.openSession()) {
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
    return cacheService.findAll();
  }

  @Override
  public void delete(LogEntry logEntry) {
    Transaction transaction = null;
    try (Session session = HibernateUtil.openSession()) {
      transaction = session.beginTransaction();
      session.remove(logEntry);
      transaction.commit();
      cacheService.delete(logEntry);
    } catch (Exception e) {
      if (transaction != null && transaction.getStatus().canRollback()) {
        try {
          transaction.rollback();
        } catch (Exception rollbackException) {
          log.error("Failed to rollback transaction", rollbackException);
        }
      }
      log.error("Failed to delete log entry", e);
    }
  }
}