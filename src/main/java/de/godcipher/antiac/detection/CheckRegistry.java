package de.godcipher.antiac.detection;

import de.godcipher.antiac.config.Configuration;
import de.godcipher.antiac.detection.violation.ViolationTracker;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
public class CheckRegistry {

  private final Set<Check> checks = new HashSet<>();

  private final ViolationTracker violationTracker;
  private final Configuration configuration;

  public void registerCheck(Check check) {
    checks.add(check);
    check.load();
  }

  public void unregisterCheck(Check check) {
    checks.remove(check);
    check.unload();
  }

  public void performChecks(Player player) {
    checks.stream()
        .filter(this::isCheckActiveAndLoaded)
        .forEach(check -> processCheck(player, check));
  }

  private boolean isCheckActiveAndLoaded(Check check) {
    return check.isActivated() && check.isLoaded();
  }

  private void processCheck(Player player, Check check) {
    if (check.check(player)) {
      if (isViolationTrackingEnabled()) {
        handleViolation(player, check);
      } else {
        check.onFlag(player);
      }
    }
  }

  private boolean isViolationTrackingEnabled() {
    return (Boolean) configuration.getConfigOption("violations").getValue();
  }

  private void handleViolation(Player player, Check check) {
    violationTracker.addViolation(player.getUniqueId());
    if (violationTracker.getViolationCount(player.getUniqueId()) >= getMaxViolations()) {
      violationTracker.resetViolation(player.getUniqueId());
      check.onFlag(player);
    }
  }

  private int getMaxViolations() {
    return (Integer) configuration.getConfigOption("max-allowed-violations").getValue();
  }
}
