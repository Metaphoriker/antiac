package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.Check;
import de.godcipher.comet.ConfigurationOption;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/**
 * ClickLimitCheck checks for a players CPS and compares it to the limit set in the configuration.
 */
@Slf4j
public class ClickLimitCheck extends Check {

  private static final String LIMIT_CONFIG = "limit";

  private int clickLimit;

  public ClickLimitCheck(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  protected void onLoad() {
    setupDefaults();
    setConfigValue();
  }

  private void setConfigValue() {
    clickLimit = (int) getCheckConfiguration().getConfigOption(LIMIT_CONFIG).getValue();
  }

  @Override
  protected void onUnload() {}

  @Override
  public boolean check(Player player) {
    return clickTracker.getLatestCPS(player.getUniqueId()).getCPS() >= clickLimit;
  }

  private void setupDefaults() {
    getCheckConfiguration()
        .setConfigOption(LIMIT_CONFIG, new ConfigurationOption<>(40, "The upper limit of the CPS"));
    saveConfiguration();
  }
}
