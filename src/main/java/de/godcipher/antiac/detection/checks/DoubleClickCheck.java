package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.detection.Check;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/**
 * DoubleClickCheck checks if the delay between the clicks makes it suspicious of two clicks at a
 * time.
 */
@Slf4j
public class DoubleClickCheck extends Check {

  private static final String REQUIRED_CONSECUTIVE_SUSPICIOUS_CLICKS_CONFIG =
      "required-consecutive-suspicious-clicks";

  private int requiredConsecutiveSuspiciousClicks = 3;

  public DoubleClickCheck(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  protected void onLoad() {
    setupDefaults();

    requiredConsecutiveSuspiciousClicks =
        getCheckConfiguration()
            .getConfigOption(REQUIRED_CONSECUTIVE_SUSPICIOUS_CLICKS_CONFIG)
            .asInteger();
  }

  @Override
  protected void onUnload() {}

  @Override
  public boolean check(Player player) {
    CPS cps = clickTracker.getLatestCPS(player.getUniqueId());
    if (cps.isEmpty()) return false;
    List<Long> clicks = cps.getClicks().stream().map(Click::getTime).toList();

    if (clicks.size() < 3) {
      return false;
    }

    int consecutiveSuspiciousClicks = 0;

    for (int i = 0; i < clicks.size() - 1; i++) {
      long first = clicks.get(i);
      long second = clicks.get(i + 1);

      // if the time difference between consecutive clicks is 0 or 1 ms, it's suspicious
      if (Math.abs(second - first) <= 1) {
        consecutiveSuspiciousClicks++;
        if (consecutiveSuspiciousClicks >= requiredConsecutiveSuspiciousClicks) {
          return true;
        }
      } else {
        consecutiveSuspiciousClicks = 0;
      }
    }

    return false;
  }

  private void setupDefaults() {
    getCheckConfiguration()
        .setConfigOption(
            REQUIRED_CONSECUTIVE_SUSPICIOUS_CLICKS_CONFIG,
            ConfigurationOption.ofInteger(
                3, "The number of consecutive suspicious clicks required to flag"));
  }
}
