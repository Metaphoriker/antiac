package de.godcipher.antiac.detection.checks;

import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.detection.Check;
import de.godcipher.antiac.click.CPS;
import de.godcipher.antiac.click.Click;
import de.godcipher.antiac.click.ClickTracker;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

/**
 * ClickDelaySpanCheck checks for a players click delay span and compares it to the limit set in the
 * configuration.
 */
@Slf4j
public class ClickDelaySpanCheck extends Check {

  private int span;

  public ClickDelaySpanCheck(ClickTracker clickTracker) {
    super(clickTracker);
  }

  @Override
  protected void onLoad() {
    setupDefaults();

    span = (Integer) getConfiguration().getConfigOption("span").getValue();
  }

  @Override
  protected void onUnload() {}

  @Override
  public boolean check(Player player) {
    CPS cps = clickTracker.getLatestCPS(player.getUniqueId());
    if (cps.isEmpty()) return false;

    return cps.getClicks().stream()
            .mapToLong(Click::getTime)
            .reduce((first, second) -> second - first)
            .orElse(0)
        <= span;
  }

  private void setupDefaults() {
    getConfiguration()
        .addConfigOption(
            "span", ConfigurationOption.ofInteger(5, "The span of the click delay, +-"));
  }
}
