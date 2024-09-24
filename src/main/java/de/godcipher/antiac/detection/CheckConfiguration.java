package de.godcipher.antiac.detection;

import de.godcipher.antiac.AntiAC;
import de.godcipher.comet.Configuration;
import de.godcipher.comet.ConfigurationOption;
import java.io.File;
import lombok.Getter;

@Getter
public class CheckConfiguration extends Configuration {

  public CheckConfiguration(String checkName) {
    super();
    setupFile(
        new File(
            AntiAC.getInstance().getDataFolder() + File.separator + "checks", checkName + ".yml"));
    setConfigOption("activated", new ConfigurationOption<>(true, "Should the check be active?"));
    saveConfiguration();
  }

  public boolean isActivated() {
    return (Boolean) getConfigOption("activated").getValue();
  }
}
