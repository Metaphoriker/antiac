package de.godcipher.antiac.hibernate.cache;

import de.godcipher.antiac.hibernate.entity.LogEntry;
import de.godcipher.antiac.hibernate.repository.LogEntryRepository;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CacheUpdater {

  private final LogEntryRepository logEntryRepository;
  private final LogEntryCacheService cacheService;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public CacheUpdater(LogEntryRepository logEntryRepository, LogEntryCacheService cacheService) {
    this.logEntryRepository = logEntryRepository;
    this.cacheService = cacheService;
  }

  public void start() {
    scheduler.scheduleAtFixedRate(this::updateCache, 0, 5, TimeUnit.MINUTES);
  }

  private void updateCache() {
    try {
      List<LogEntry> logEntries = logEntryRepository.findAll();
      logEntries.forEach(cacheService::save);
      log.debug("Cache updated with latest log entries");
    } catch (Exception e) {
      log.error("Failed to update cache", e);
    }
  }

  public void stop() {
    scheduler.shutdown();
  }
}
