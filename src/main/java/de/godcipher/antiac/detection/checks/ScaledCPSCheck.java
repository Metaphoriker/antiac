package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.Check;
import de.godcipher.comet.ConfigurationOption;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/**
 * ScaledCPSCheck checks whether the player's click pattern exceeds the expected delay range when
 * scaled according to their clicks per second (CPS). If their actual click delay is too short for
 * the CPS, they are flagged as suspicious.
 */
@Slf4j
public class ScaledCPSCheck extends Check {

  private static final String TOTAL_DELAY_MS_CONFIG = "total-delay-ms";
  private static final String MIN_CLICKS_CONFIG = "min-clicks";

  private int totalDelayMs; // Total delay in milliseconds expected for the 0-100 clicks
  private int minClicks; // Minimum number of clicks to analyze

  public ScaledCPSCheck(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  protected void onLoad() {
    setupDefaults();
    setConfigValues();
  }

  private void setConfigValues() {
    totalDelayMs = (int) getCheckConfiguration().getConfigOption(TOTAL_DELAY_MS_CONFIG).getValue();
    minClicks = (int) getCheckConfiguration().getConfigOption(MIN_CLICKS_CONFIG).getValue();
  }

  @Override
  protected void onUnload() {}

  @Override
  public boolean check(Player player) {
    CPS cps = clickTracker.getLatestCPS(player.getUniqueId());
    if (isInvalidClickData(cps)) return false;

    double actualTotalDelay = calculateTotalDelay(cps.getClicks());
    double expectedScaledDelay = scaleCPSDelay(cps.getCPS());

    if (actualTotalDelay < expectedScaledDelay) {
      log.debug(
          "Player: {} flagged for low total click delay. Actual: {}, Expected: {}",
          player.getName(),
          actualTotalDelay,
          expectedScaledDelay);
      onFlag(player);
      return true;
    }

    return false;
  }

  private boolean isInvalidClickData(CPS cps) {
    return cps.isEmpty() || cps.getClicks().size() < minClicks;
  }

  /**
   * Calculate the total delay (in milliseconds) between all player clicks.
   *
   * @param clicks the list of clicks
   * @return the total delay between clicks
   */
  private double calculateTotalDelay(List<Click> clicks) {
    if (clicks.size() < 2) return Double.MAX_VALUE; // Not enough clicks to calculate delay

    long totalDelay = 0;
    for (int i = 1; i < clicks.size(); i++) {
      totalDelay += clicks.get(i).getTime() - clicks.get(i - 1).getTime();
    }

    return (double) totalDelay;
  }

  /**
   * Scale the total expected delay based on the player's CPS. If the player's CPS is lower than the
   * total 100 clicks range, this will scale down the delay accordingly.
   *
   * @param cps the player's clicks per second
   * @return the expected scaled delay for the given CPS
   */
  private double scaleCPSDelay(int cps) {
    double scalingFactor = (double) cps / 100;
    return scalingFactor * totalDelayMs;
  }

  private void setupDefaults() {
    getCheckConfiguration()
        .setConfigOption(
            TOTAL_DELAY_MS_CONFIG,
            new ConfigurationOption<>(2500, "The total delay in milliseconds for 0-100 clicks."));
    getCheckConfiguration()
        .setConfigOption(
            MIN_CLICKS_CONFIG,
            new ConfigurationOption<>(
                10, "The minimum number of clicks required to perform the check."));
    saveConfiguration();
  }
}
