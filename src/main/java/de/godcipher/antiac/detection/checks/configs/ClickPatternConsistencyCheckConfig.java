package de.godcipher.antiac.detection.checks.configs;

import de.godcipher.antiac.detection.CheckConfiguration;
import de.godcipher.gutil.config.annotation.ConfigValue;
import de.godcipher.gutil.config.annotation.Configuration;
import lombok.Getter;

@Getter
@Configuration(fileName = "click-pattern-consistency-check.yml")
public class ClickPatternConsistencyCheckConfig extends CheckConfiguration {

  @ConfigValue(
      name = "click-threshold",
      description = "Minimum number of clicks required to analyze the pattern.")
  private int clickThreshold = 10;

  @ConfigValue(
      name = "min-deviation-threshold",
      description = "The minimum standard deviation threshold for click pattern randomness.")
  private double minDeviationThreshold = 1.5;

  @ConfigValue(
      name = "jitter-threshold",
      description = "The jitter threshold for detecting irregular click delays.")
  private int jitterThreshold = 10;

  @ConfigValue(
      name = "small-window",
      description = "Window size for delay comparison to detect similar delays.")
  private int smallWindow = 5;

  @ConfigValue(
      name = "identical-delay-threshold",
      description = "Threshold for the number of identical delays in click patterns.")
  private int identicalDelayThreshold = 3;
}
