package de.godcipher.antiac.detection;

import de.godcipher.antiac.config.Configuration;
import de.godcipher.antiac.config.ConfigurationOption;
import lombok.Getter;

@Getter
public class CheckConfiguration extends Configuration {

  public CheckConfiguration(String checkName) {
    super();
    setupFile(checkName + ".yml", "checks");
    setConfigOption("activated", new ConfigurationOption<>(true, "Should the check be active?"));
    saveConfiguration();
  }

  public boolean isActivated() {
    return (Boolean) getConfigOption("activated").getValue();
  }
}
