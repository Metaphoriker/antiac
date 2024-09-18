package de.godcipher.antiac.detection.violation;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ViolationTracker {

  private final Map<UUID, Violation> violations = new ConcurrentHashMap<>();

  public synchronized void addViolation(UUID playerId) {
    Violation violation = violations.getOrDefault(playerId, new Violation());
    violation.incrementCount();
    violations.put(playerId, violation);
  }

  public synchronized void resetViolation(UUID playerId) {
    violations.remove(playerId);
  }

  public int getViolationCount(UUID playerId) {
    Violation violation = violations.get(playerId);
    return violation != null ? violation.getCount() : 0;
  }

  public Instant getLastViolationTime(UUID playerId) {
    Violation violation = violations.get(playerId);
    return violation != null ? violation.getLastModified() : null;
  }

  public synchronized void removePlayer(UUID playerId) {
    violations.remove(playerId);
  }

  public boolean hasViolations(UUID playerId) {
    Violation violation = violations.get(playerId);
    return violation != null && violation.getCount() > 0;
  }

  public synchronized void clearAllViolations() {
    violations.clear();
  }

  public Map<UUID, Violation> getAllViolations() {
    return new ConcurrentHashMap<>(violations);
  }
}
