package de.godcipher.antiac.detection.checks.configs;

import de.godcipher.antiac.detection.CheckConfiguration;
import de.godcipher.gutil.config.annotation.ConfigValue;
import de.godcipher.gutil.config.annotation.Configuration;
import lombok.Getter;

@Getter
@Configuration(fileName = "momentum-check.yml")
public class MomentumCheckConfig extends CheckConfiguration {

  @ConfigValue(
      name = "cps-threshold",
      description = "The number of CPS to check before analyzing the click pattern.")
  private int cpsThreshold = 20;

  @ConfigValue(
      name = "percentage-threshold",
      description = "The maximum percentage slope to trigger a flag.")
  private int percentageThreshold = 75;

  @ConfigValue(
      name = "window-size",
      description = "The window size used for the rolling slope calculation.")
  private int windowSize = 5;
}
