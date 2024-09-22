package de.godcipher.antiac.hibernate.cache;

import de.godcipher.antiac.hibernate.entity.LogEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogEntryCacheService {

  private final Map<Long, LogEntry> cache = new ConcurrentHashMap<>();

  public void save(LogEntry logEntry) {
    cache.put(logEntry.getId(), logEntry);
  }

  public LogEntry findById(long id) {
    return cache.get(id);
  }

  public List<LogEntry> findAll() {
    return new ArrayList<>(cache.values());
  }

  public void delete(LogEntry logEntry) {
    cache.remove(logEntry.getId());
  }
}
