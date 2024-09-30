package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.click.ClickTracker;
import de.godcipher.antiac.detection.Check;
import de.godcipher.antiac.detection.checks.configs.ClickLimitCheckConfig;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/**
 * ClickLimitCheck checks for a players CPS and compares it to the limit set in the configuration.
 */
@Slf4j
public class ClickLimitCheck extends Check<ClickLimitCheckConfig> {

  public ClickLimitCheck(ClickTracker clickTracker) {
    super(clickTracker, new ClickLimitCheckConfig());
  }

  @Override
  public boolean check(Player player) {
    return clickTracker.getLatestCPS(player.getUniqueId()).getCPS()
        >= getConfiguration().getClickLimit();
  }
}
