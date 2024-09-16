package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.detection.Check;
import de.godcipher.antiac.value.ClickTracker;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/**
 * ClickLimitCheck checks for a players CPS and compares it to the limit set in the configuration.
 */
@Slf4j
public class ClickLimitCheck extends Check {

  private int clickLimit;

  public ClickLimitCheck(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  protected void onLoad() {
    setupDefaults();

    clickLimit = (Integer) getConfiguration().getConfigOption("limit").getValue();
  }

  @Override
  protected void onUnload() {}

  @Override
  public boolean check(Player player) {
    return clickTracker.getLatestCPS(player.getUniqueId()).getCPS() >= clickLimit;
  }

  private void setupDefaults() {
    getConfiguration()
        .addConfigOption("limit", ConfigurationOption.ofInteger(40, "The upper limit of the CPS"));
  }
}
