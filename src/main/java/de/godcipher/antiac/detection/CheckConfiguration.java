package de.godcipher.antiac.detection;

import de.godcipher.antiac.AntiAC;
import de.godcipher.gutil.config.BaseConfiguration;
import de.godcipher.gutil.config.annotation.ConfigValue;
import java.io.File;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckConfiguration extends BaseConfiguration {

  @ConfigValue(name = "activated", description = "Whether the check is activated or not")
  private boolean activated = true;

  public CheckConfiguration() {
    super();
    setDirectory(new File(AntiAC.getInstance().getDataFolder(), "checks"));
  }
}
