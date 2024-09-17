package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.detection.Check;
import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.ClickTracker;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/** CPSSpanCheck checks if the player's CPS is within a specified span for a period of time. */
@Slf4j
@Deprecated
public class CPSSpanCheck extends Check {

  private int span = 3;
  private int periodInSeconds = 20;

  public CPSSpanCheck(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  protected void onLoad() {
    setupDefaults();

    periodInSeconds = (Integer) getConfiguration().getConfigOption("period-in-seconds").getValue();
    span = (Integer) getConfiguration().getConfigOption("span").getValue();
  }

  @Override
  protected void onUnload() {}

  @Override
  public boolean check(Player player) {
    List<CPS> cpsSet = clickTracker.getCPSList(player.getUniqueId());
    List<CPS> cpsToProcess = trimList(cpsSet, periodInSeconds);

    if (cpsToProcess.size() < periodInSeconds) {
      return false;
    }

    int spanMid = 0;
    boolean validCPSFound = false;
    int validCPSCount = 0;

    for (CPS cps : cpsToProcess) {
      if (cps.isEmpty()) continue;
      int currentCPS = cps.getCPS();

      if (currentCPS == 0) {
        continue;
      }

      validCPSFound = true;
      validCPSCount++;

      if (spanMid == 0) {
        spanMid = currentCPS;
      }

      if (currentCPS < spanMid - span || currentCPS > spanMid + span) {
        return false;
      }
    }

    return validCPSFound && validCPSCount >= (periodInSeconds / 2);
  }

  private void setupDefaults() {
    getConfiguration()
        .addConfigOption(
            "period-in-seconds",
            ConfigurationOption.ofInteger(5, "The period in seconds to check the CPS span"));
    getConfiguration()
        .addConfigOption("span", ConfigurationOption.ofInteger(3, "The span of the CPS span, +-"));
  }
}
