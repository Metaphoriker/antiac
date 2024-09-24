package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.Check;
import de.godcipher.comet.ConfigurationOption;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

@Slf4j
public class ClickPatternConsistencyCheck extends Check {

  private static final String CLICK_THRESHOLD_CONFIG = "click-threshold";
  private static final String MIN_DEVIATION_THRESHOLD_CONFIG = "min-deviation-threshold";
  private static final String JITTER_THRESHOLD_CONFIG = "jitter-threshold";
  private static final String SMALL_WINDOW_CONFIG = "small-window";
  private static final String IDENTICAL_DELAY_THRESHOLD_CONFIG = "identical-delay-threshold";

  private int clickThreshold;
  private double minDeviationThreshold;
  private int jitterThreshold;
  private int smallWindow;
  private int identicalDelayThreshold;

  public ClickPatternConsistencyCheck(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  protected void onLoad() {
    setupDefaults();
    setConfigValues();
  }

  @Override
  protected void onUnload() {}

  @Override
  public boolean check(Player player) {
    CPS cps = clickTracker.getLatestCPS(player.getUniqueId());
    if (isInvalidClickData(cps)) return false;

    double averageDelay = calculateAverageDelay(cps.getClicks(), clickThreshold);
    double deviation = calculateStandardDeviation(cps.getClicks(), clickThreshold);

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

  private void setupDefaults() {
    getCheckConfiguration()
        .setConfigOption(
            CLICK_THRESHOLD_CONFIG,
            new ConfigurationOption<>(10, "Minimum number of clicks to analyze."));
    getCheckConfiguration()
        .setConfigOption(
            MIN_DEVIATION_THRESHOLD_CONFIG,
            new ConfigurationOption<>(1.5, "Minimum deviation threshold."));
    getCheckConfiguration()
        .setConfigOption(
            JITTER_THRESHOLD_CONFIG,
            new ConfigurationOption<>(10, "Jitter threshold for click delays."));
    getCheckConfiguration()
        .setConfigOption(
            SMALL_WINDOW_CONFIG,
            new ConfigurationOption<>(5, "Small window for delay comparison."));
    getCheckConfiguration()
        .setConfigOption(
            IDENTICAL_DELAY_THRESHOLD_CONFIG,
            new ConfigurationOption<>(3, "Threshold for identical delays."));
    saveConfiguration();
  }

  private void setConfigValues() {
    clickThreshold =
        (int) getCheckConfiguration().getConfigOption(CLICK_THRESHOLD_CONFIG).getValue();
    minDeviationThreshold =
        (double) getCheckConfiguration().getConfigOption(MIN_DEVIATION_THRESHOLD_CONFIG).getValue();
    jitterThreshold =
        (int) getCheckConfiguration().getConfigOption(JITTER_THRESHOLD_CONFIG).getValue();
    smallWindow = (int) getCheckConfiguration().getConfigOption(SMALL_WINDOW_CONFIG).getValue();
    identicalDelayThreshold =
        (int) getCheckConfiguration().getConfigOption(IDENTICAL_DELAY_THRESHOLD_CONFIG).getValue();
  }

  private boolean isInvalidClickData(CPS cps) {
    return cps.isEmpty() || cps.getClicks().size() < clickThreshold;
  }

  private boolean isClickPatternTooRegular(double deviation) {
    return deviation < minDeviationThreshold;
  }

  private boolean isClickPatternSuspicious(List<Click> clicks, double averageDelay) {
    boolean hasJitter = false;
    int identicalDelaysCount = 0;

    for (int i = 0; i < clickThreshold; i++) {
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

    return !hasJitter || identicalDelaysCount > identicalDelayThreshold;
  }

  private boolean isDelaySimilarToAverage(double delay, double averageDelay) {
    return Math.abs(delay - averageDelay) < smallWindow;
  }

  private boolean isJitterDetected(Click currentClick, Click previousClick) {
    return Math.abs(currentClick.getDelay() - previousClick.getDelay()) > jitterThreshold;
  }

  private boolean isRepeatingPattern(List<Click> clicks) {
    if (clicks.size() < clickThreshold) return false;

    for (int i = 0; i < clickThreshold / 2; i++) {
      double delay1 = clicks.get(clicks.size() - 1 - i).getDelay();
      double delay2 = clicks.get(clicks.size() - 1 - (i + clickThreshold / 2)).getDelay();

      if (Math.abs(delay1 - delay2) > smallWindow) {
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
