package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.detection.Check;
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
public class MomentumCheck extends Check {

  private static final String PERCENTAGE_THRESHOLD_CONFIG = "percentage-threshold";
  private static final String CPS_THRESHOLD_CONFIG = "cps-threshold";
  private static final String WINDOW_SIZE_CONFIG = "window-size";

  private int CPSThreshold = -1; // Number of CPS to check
  private int percentageThreshold = -1; // Max percentage slope
  private int windowSize = -1; // Size of each rolling window

  public MomentumCheck(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  protected void onLoad() {
    setupDefaults();

    CPSThreshold = getCheckConfiguration().getConfigOption(CPS_THRESHOLD_CONFIG).asInteger();
    percentageThreshold =
        getCheckConfiguration().getConfigOption(PERCENTAGE_THRESHOLD_CONFIG).asInteger();
    windowSize = getCheckConfiguration().getConfigOption(WINDOW_SIZE_CONFIG).asInteger();
  }

  @Override
  protected void onUnload() {}

  @Override
  public boolean check(Player player) {
    List<CPS> playerCps = clickTracker.getCPSList(player.getUniqueId());
    List<CPS> setToProcess = trimList(playerCps, CPSThreshold);

    if (setToProcess.size() < CPSThreshold || windowSize < 2) {
      return false;
    }

    double rollingSlope = calculateRollingSlope(setToProcess);
    double slopePercentage =
        Math.abs(rollingSlope * 100); // Take absolute value to handle negative slopes

    return slopePercentage > percentageThreshold;
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

    if (validCps.isEmpty() || validCps.size() < windowSize) {
      return 0;
    }

    double totalSlope = 0;
    int slopeCount = 0;

    // Calculate the slope for each rolling window
    for (int i = 0; i <= validCps.size() - windowSize; i++) {
      List<CPS> window = validCps.subList(i, i + windowSize);
      double slope = calculateSlope(window);
      totalSlope += slope;
      slopeCount++;
    }

    // Return the average slope across all windows to reduce noise
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

  private void setupDefaults() {
    getCheckConfiguration()
        .setConfigOption(
            CPS_THRESHOLD_CONFIG, ConfigurationOption.ofInteger(20, "The number of CPS to check"));
    getCheckConfiguration()
        .setConfigOption(
            PERCENTAGE_THRESHOLD_CONFIG,
            ConfigurationOption.ofInteger(75, "The maximum percentage slope to trigger on"));
    getCheckConfiguration()
        .setConfigOption(
            WINDOW_SIZE_CONFIG,
            ConfigurationOption.ofInteger(5, "The window size for rolling slope calculation"));
  }
}
