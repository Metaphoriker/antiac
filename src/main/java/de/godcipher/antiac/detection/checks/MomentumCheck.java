package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.Check;
import de.godcipher.antiac.detection.checks.configs.MomentumCheckConfig;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/**
 * MomentumCheck is a detection mechanism designed to analyze the rate of change in player click
 * patterns over a period of CPS. It measures the percentage slope (or steepness) of CPS activity to
 * detect potential irregularities using a rolling slope method.
 */
@Slf4j
public class MomentumCheck extends Check<MomentumCheckConfig> {

  public MomentumCheck(ClickTracker clickTracker) {
    super(clickTracker, new MomentumCheckConfig());
  }

  @Override
  public boolean check(Player player) {
    List<CPS> playerCps = clickTracker.getCPSList(player.getUniqueId());
    List<CPS> setToProcess = trimList(playerCps, getConfiguration().getCpsThreshold());

    if (setToProcess.size() < getConfiguration().getCpsThreshold()
        || getConfiguration().getWindowSize() < 2) {
      return false;
    }

    double rollingSlope = calculateRollingSlope(setToProcess);
    double slopePercentage = Math.abs(rollingSlope * 100); // abs for negative slopes

    return slopePercentage > getConfiguration().getPercentageThreshold();
  }

  /**
   * Calculate the rolling slope based on a window of CPS values, and return the average slope
   * across all windows. This reduces sensitivity to noise by averaging the slopes.
   *
   * @param cpsSet the set of CPS instances
   * @return the average slope across all windows
   */
  private double calculateRollingSlope(List<CPS> cpsSet) {
    LinkedList<CPS> validCps =
        cpsSet.stream()
            .filter(cps -> !cps.isEmpty() && cps.getCPS() > 0)
            .collect(Collectors.toCollection(LinkedList::new));

    if (validCps.isEmpty() || validCps.size() < getConfiguration().getWindowSize()) {
      return 0;
    }

    double totalSlope = 0;
    int slopeCount = 0;

    for (int i = 0; i <= validCps.size() - getConfiguration().getWindowSize(); i++) {
      List<CPS> window = validCps.subList(i, i + getConfiguration().getWindowSize());
      double slope = calculateSlope(window);
      totalSlope += slope;
      slopeCount++;
    }

    return totalSlope / slopeCount;
  }

  /**
   * Calculate the slope based on the average CPS value and the time difference between the first
   * and last valid clicks in the CPS window.
   *
   * @param cpsWindow the window of CPS instances
   * @return the calculated slope (rate of change in clicking)
   */
  private double calculateSlope(List<CPS> cpsWindow) {
    LinkedList<Long> timestamps =
        cpsWindow.stream()
            .flatMap(cps -> cps.getClicks().stream())
            .map(Click::getTime)
            .sorted()
            .collect(Collectors.toCollection(LinkedList::new));

    if (timestamps.isEmpty()) {
      return 0;
    }

    long firstClick = timestamps.getFirst();
    long lastClick = timestamps.getLast();

    double timeSpan = (lastClick - firstClick) / 1000.0;

    if (timeSpan == 0) {
      return 0;
    }

    int initialCPS = cpsWindow.getFirst().getCPS();
    double averageCPS = cpsWindow.stream().mapToInt(CPS::getCPS).average().orElse(0);

    return (averageCPS - initialCPS) / timeSpan;
  }
}
