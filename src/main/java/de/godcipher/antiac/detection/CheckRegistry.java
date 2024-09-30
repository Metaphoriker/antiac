package de.godcipher.antiac.detection;

import de.godcipher.antiac.AntiACConfig;
import de.godcipher.antiac.detection.violation.ViolationTracker;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@Getter
@RequiredArgsConstructor
public class CheckRegistry {

  private final Set<Check> checks = new HashSet<>();

  private final ViolationTracker violationTracker;
  private final AntiACConfig configuration;

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

  public Check getCheckByName(String name) {
    return checks.stream()
        .filter(check -> check.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElse(null);
  }

  public void clearChecks() {
    Iterator<Check> iterator = checks.iterator();
    while (iterator.hasNext()) {
      Check check = iterator.next();
      iterator.remove();
      check.unload();
    }
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
    return configuration.isViolations();
  }

  private void handleViolation(Player player, Check check) {
    violationTracker.addViolation(player.getUniqueId());
    flagPlayerWhenViolationsExceeded(player, check);
  }

  private void flagPlayerWhenViolationsExceeded(Player player, Check check) {
    if (violationTracker.getViolationCount(player.getUniqueId()) >= getMaxViolations()) {
      violationTracker.resetViolation(player.getUniqueId());
      check.onFlag(player);
    }
  }

  private int getMaxViolations() {
    return configuration.getMaxAllowedViolations();
  }
}
