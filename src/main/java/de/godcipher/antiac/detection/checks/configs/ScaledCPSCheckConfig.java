package de.godcipher.antiac.detection.checks.configs;

import de.godcipher.antiac.detection.CheckConfiguration;
import de.godcipher.gutil.config.annotation.ConfigValue;
import de.godcipher.gutil.config.annotation.Configuration;
import lombok.Getter;

@Getter
@Configuration(fileName = "scaled-cps.yml")
public class ScaledCPSCheckConfig extends CheckConfiguration {

  @ConfigValue(
      name = "max-expected-click-delay-ms",
      description =
          "The maximum expected total delay (in milliseconds) for a player’s clicks from 0 to 100 CPS. If the player's actual click delay is shorter than the scaled delay, it may be flagged as suspicious.")
  private long maxExpectedClickDelayMs = 2500;

  @ConfigValue(
      name = "min-clicks",
      description = "The minimum number of clicks required to perform the check.")
  private int minClicks = 10;
}
