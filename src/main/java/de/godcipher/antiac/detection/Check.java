package de.godcipher.antiac.detection;

import de.godcipher.antiac.AntiAC;
import de.godcipher.antiac.bstats.BStatsHandler;
import de.godcipher.antiac.config.Configuration;
import de.godcipher.antiac.config.ConfigurationOption;
import de.godcipher.antiac.event.PlayerFlaggedEvent;
import de.godcipher.antiac.value.CPS;
import de.godcipher.antiac.value.ClickTracker;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Slf4j
public abstract class Check {

  @Getter private final String name;
  private final Configuration configuration;

  protected final ClickTracker clickTracker;

  @Getter private boolean activated;
  @Getter private boolean loaded;

  protected Check(ClickTracker clickTracker) {
    this.name = this.getClass().getSimpleName();
    this.clickTracker = clickTracker;

    this.configuration = new Configuration();
    configuration.setupFile(name + ".yml", "checks");
    configuration.addConfigOption(
        "activated", new ConfigurationOption<>(true, "Should the check be active?"));
    configuration.saveConfiguration();
    activated = (Boolean) configuration.getConfigOption("activated").getValue();
  }

  void load() {
    if (isLoaded() || !isActivated()) {
      log.error("Failed loading {}", name);
      return;
    }

    loaded = true;
    onLoad();
    configuration.loadConfig();
  }

  void unload() {
    if (!isLoaded()) {
      log.error("Failed unloading {}", name);
      return;
    }

    loaded = false;
    onUnload();
    saveConfiguration();
  }

  protected List<CPS> trimList(List<CPS> set, int size) {
    return set.stream().limit(size).collect(Collectors.toList());
  }

  protected Configuration getConfiguration() {
    return configuration;
  }

  protected void saveConfiguration() {
    configuration.saveConfiguration();
  }

  protected final void onFlag(Player player) {
    log.debug("Player: {} - Flagged by {}", player.getName(), name);
    BStatsHandler.increaseFlagged();
    Bukkit.getScheduler()
        .runTask(
            AntiAC.getInstance(),
            () ->
                Bukkit.getPluginManager()
                    .callEvent(
                        new PlayerFlaggedEvent(
                            player, clickTracker.getCPSList(player.getUniqueId()), this)));
  }

  protected abstract void onLoad();

  protected abstract void onUnload();

  public abstract boolean check(Player player);
}
