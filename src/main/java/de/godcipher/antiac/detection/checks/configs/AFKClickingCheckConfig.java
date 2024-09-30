package de.godcipher.antiac.detection.checks.configs;

import de.godcipher.antiac.detection.CheckConfiguration;
import de.godcipher.gutil.config.annotation.ConfigValue;
import de.godcipher.gutil.config.annotation.Configuration;
import lombok.Getter;

@Getter
@Configuration(fileName = "afk-clicking.yml")
public class AFKClickingCheckConfig extends CheckConfiguration {

  @ConfigValue(
      name = "afk-after-seconds",
      description = "The amount of seconds a player has to be AFK before the check is triggered")
  private int afkAfterSeconds = 60;
}
