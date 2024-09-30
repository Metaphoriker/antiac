package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.Check;
import de.godcipher.antiac.detection.checks.configs.DoubleClickCheckConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/**
 * DoubleClickCheck checks if the delay between the clicks makes it suspicious of two clicks at a
 * time.
 */
@Slf4j
public class DoubleClickCheck extends Check<DoubleClickCheckConfig> {

  public DoubleClickCheck(ClickTracker clickTracker) {
    super(clickTracker, new DoubleClickCheckConfig());
  }

  @Override
  public boolean check(Player player) {
    CPS cps = clickTracker.getLatestCPS(player.getUniqueId());
    if (cps.isEmpty()) return false;
    List<Long> clicks = cps.getClicks().stream().map(Click::getTime).toList();
    if (clicks.size() < 3) return false;
    return suspiciousClicksExceedsLimit(clicks);
  }

  private boolean suspiciousClicksExceedsLimit(List<Long> clicks) {
    return calculateConsecutiveSuspiciousClicks(clicks)
        >= getConfiguration().getRequiredConsecutiveSuspiciousClicks();
  }

  private int calculateConsecutiveSuspiciousClicks(List<Long> clicks) {
    int consecutiveSuspiciousClicks = 0;
    for (int i = 0; i < clicks.size() - 1; i++) {
      long first = clicks.get(i);
      long second = clicks.get(i + 1);

      if (Math.abs(second - first) <= 1) {
        consecutiveSuspiciousClicks++;
      } else {
        consecutiveSuspiciousClicks = 0;
      }
    }
    return consecutiveSuspiciousClicks;
  }
}
