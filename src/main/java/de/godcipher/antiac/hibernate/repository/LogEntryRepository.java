package de.godcipher.antiac.hibernate.repository;

import de.godcipher.antiac.hibernate.entity.LogEntry;
import java.util.List;

public interface LogEntryRepository {
  void save(LogEntry logEntry);

  LogEntry findById(long id);

  List<LogEntry> findAll();

  void delete(LogEntry logEntry);
}
