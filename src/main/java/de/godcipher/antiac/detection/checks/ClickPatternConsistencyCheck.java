package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.Check;
import de.godcipher.antiac.detection.checks.configs.ClickPatternConsistencyCheckConfig;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/**
 * ClickPatternConsistencyCheck checks for irregularities in a player's click patterns by analyzing
 * delays between clicks and detecting patterns that are too regular or suspiciously repeating.
 */
@Slf4j
public class ClickPatternConsistencyCheck extends Check<ClickPatternConsistencyCheckConfig> {

  public ClickPatternConsistencyCheck(ClickTracker clickTracker) {
    super(clickTracker, new ClickPatternConsistencyCheckConfig());
  }

  @Override
  public boolean check(Player player) {
    CPS cps = clickTracker.getLatestCPS(player.getUniqueId());
    if (isInvalidClickData(cps)) return false;

    double averageDelay =
        calculateAverageDelay(cps.getClicks(), getConfiguration().getClickThreshold());
    double deviation =
        calculateStandardDeviation(cps.getClicks(), getConfiguration().getClickThreshold());

    if (isClickPatternTooRegular(deviation)) {
      logPatternIssue("Click pattern too regular", deviation);
      return true;
    }

    if (isClickPatternSuspicious(cps.getClicks(), averageDelay)) {
      logPatternIssue(
          "Click pattern lacks sufficient randomness or has too many identical delays.");
      return true;
    }

    if (isRepeatingPattern(cps.getClicks())) {
      logPatternIssue("Click pattern is repeating.");
      return true;
    }

    return false;
  }

  private boolean isInvalidClickData(CPS cps) {
    return cps.isEmpty() || cps.getClicks().size() < getConfiguration().getClickThreshold();
  }

  private boolean isClickPatternTooRegular(double deviation) {
    return deviation < getConfiguration().getMinDeviationThreshold();
  }

  private boolean isClickPatternSuspicious(List<Click> clicks, double averageDelay) {
    boolean hasJitter = false;
    int identicalDelaysCount = 0;

    for (int i = 0; i < getConfiguration().getClickThreshold(); i++) {
      Click currentClick = clicks.get(clicks.size() - 1 - i);
      double currentDelay = currentClick.getDelay();

      if (isDelaySimilarToAverage(currentDelay, averageDelay)) {
        identicalDelaysCount++;
      }

      if (i > 0) {
        Click previousClick = clicks.get(clicks.size() - 1 - (i - 1));
        if (isJitterDetected(currentClick, previousClick)) {
          hasJitter = true;
        }
      }
    }

    return !hasJitter || identicalDelaysCount > getConfiguration().getIdenticalDelayThreshold();
  }

  private boolean isDelaySimilarToAverage(double delay, double averageDelay) {
    return Math.abs(delay - averageDelay) < getConfiguration().getSmallWindow();
  }

  private boolean isJitterDetected(Click currentClick, Click previousClick) {
    return Math.abs(currentClick.getDelay() - previousClick.getDelay())
        > getConfiguration().getJitterThreshold();
  }

  private boolean isRepeatingPattern(List<Click> clicks) {
    if (clicks.size() < getConfiguration().getClickThreshold()) return false;

    for (int i = 0; i < getConfiguration().getClickThreshold() / 2; i++) {
      double delay1 = clicks.get(clicks.size() - 1 - i).getDelay();
      double delay2 =
          clicks
              .get(clicks.size() - 1 - (i + getConfiguration().getClickThreshold() / 2))
              .getDelay();

      if (Math.abs(delay1 - delay2) > getConfiguration().getSmallWindow()) {
        return false;
      }
    }

    return true;
  }

  private double calculateAverageDelay(List<Click> clicks, int amount) {
    long totalDelay = 0;
    for (int i = clicks.size() - amount; i < clicks.size() - 1; i++) {
      totalDelay += clicks.get(i + 1).getTime() - clicks.get(i).getTime();
    }
    return (double) totalDelay / (amount - 1);
  }

  private double calculateStandardDeviation(List<Click> clicks, int amount) {
    double averageDelay = calculateAverageDelay(clicks, amount);
    double sumSquaredDeviations = 0.0;

    for (int i = clicks.size() - amount; i < clicks.size() - 1; i++) {
      double delay = clicks.get(i + 1).getTime() - clicks.get(i).getTime();
      sumSquaredDeviations += Math.pow(delay - averageDelay, 2);
    }

    return Math.sqrt(sumSquaredDeviations / (amount - 1));
  }

  private void logPatternIssue(String message, Object... args) {
    log.debug(message, args);
  }
}
