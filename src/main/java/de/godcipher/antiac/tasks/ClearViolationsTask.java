package de.godcipher.antiac.tasks;

import de.godcipher.antiac.detection.violation.Violation;
import de.godcipher.antiac.detection.violation.ViolationTracker;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClearViolationsTask implements Runnable {

  private static final long CLEAR_INTERVAL = 60000;

  private final ViolationTracker violationTracker;

  @Override
  public void run() {
    for (Map.Entry<UUID, Violation> entry : violationTracker.getAllViolations().entrySet()) {
      UUID playerId = entry.getKey();
      Violation violation = entry.getValue();
      resetViolationsIfExceeded(violation, playerId);
    }
  }

  private void resetViolationsIfExceeded(Violation violation, UUID playerId) {
    if (violation.getLastModified().plusMillis(CLEAR_INTERVAL).isBefore(Instant.now())) {
      violationTracker.resetViolation(playerId);
    }
  }
}
