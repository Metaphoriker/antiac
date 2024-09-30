package de.godcipher.antiac.detection.checks.configs;

import de.godcipher.antiac.detection.CheckConfiguration;
import de.godcipher.gutil.config.annotation.ConfigValue;
import de.godcipher.gutil.config.annotation.Configuration;
import lombok.Getter;

@Getter
@Configuration(fileName = "double-click-check.yml")
public class DoubleClickCheckConfig extends CheckConfiguration {

    @ConfigValue(
            name = "required-consecutive-suspicious-clicks",
            description = "The number of consecutive suspicious clicks required to flag a player.")
    private int requiredConsecutiveSuspiciousClicks = 3;
}
